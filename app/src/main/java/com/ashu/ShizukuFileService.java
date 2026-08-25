package com.ashu;

import android.content.Context;

import androidx.annotation.Keep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Runs in Shizuku's ADB-shell process; it is not an Android manifest service. */
public class ShizukuFileService extends IShizukuFileService.Stub {

    public ShizukuFileService() {
    }

    @Keep
    public ShizukuFileService(Context context) {
    }

    @Override
    public int replaceExistingFiles(String sourceDir, String packageName, String[] requiredPaths) {
        try {
            if (!isSupportedPackage(packageName) || requiredPaths == null || requiredPaths.length == 0) {
                return 10;
            }

            File source = new File(sourceDir).getCanonicalFile();
            String normalizedSourcePath = source.getPath()
                    .replace('\\', '/')
                    .toLowerCase(java.util.Locale.ROOT);
            if (!source.isDirectory()
                    || !normalizedSourcePath.contains("/android/data/com.akash.panel/files/")) {
                return 11;
            }

            for (String relativePath : requiredPaths) {
                if (!isSafeRelativePath(relativePath)
                        || !new File(source, relativePath).isFile()) {
                    return 12;
                }
            }

            List<PayloadTarget> targets = findAllExistingPayloadTargets(
                    packageName, requiredPaths, source);
            if (targets.size() != requiredPaths.length) {
                return 14;
            }

            forceStop(packageName);

            int replacedFiles = 0;
            // Every supplied payload file is mandatory. They can live under different
            // versioned/nested bases, so each one is discovered independently.
            for (PayloadTarget target : targets) {
                File sourceFile = new File(source, target.relativePath);
                File targetFile = target.file;
                copyExistingFile(sourceFile, targetFile);
                if (!sameFileContents(sourceFile, targetFile)) {
                    return 13;
                }
                replacedFiles++;
            }
            return replacedFiles == requiredPaths.length ? 0 : 14;
        } catch (Exception e) {
            return 20;
        }
    }

    private List<PayloadTarget> findAllExistingPayloadTargets(
            String packageName, String[] requiredPaths, File source)
            throws IOException {
        List<File> packageDirectories = findPackageDirectories(packageName, source);
        Map<String, File> discoveredTargets = new LinkedHashMap<>();

        for (File packageDirectory : packageDirectories) {
            addExistingTargetsAtBase(
                    new File(packageDirectory, "files"), requiredPaths, discoveredTargets);
            addExistingTargetsAtBase(packageDirectory, requiredPaths, discoveredTargets);
            searchForExistingTargets(packageDirectory, requiredPaths, discoveredTargets);
            if (discoveredTargets.size() == requiredPaths.length) {
                break;
            }
        }

        List<PayloadTarget> targets = new ArrayList<>();
        for (String relativePath : requiredPaths) {
            File target = discoveredTargets.get(relativePath);
            if (target != null) {
                targets.add(new PayloadTarget(relativePath, target));
            }
        }
        return targets;
    }

    private List<File> findPackageDirectories(String packageName, File source) throws IOException {
        List<File> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<File> androidRoots = new ArrayList<>();
        Set<String> seenRoots = new HashSet<>();

        String sourcePath = source.getCanonicalPath().replace('\\', '/');
        String sourcePathLower = sourcePath.toLowerCase(java.util.Locale.ROOT);
        String marker = "/android/data/com.akash.panel/files/";
        int markerIndex = sourcePathLower.indexOf(marker);
        if (markerIndex > 0) {
            String currentStorageRoot = sourcePath.substring(0, markerIndex);
            addAndroidRoot(androidRoots, seenRoots, new File(currentStorageRoot, "Android"));
            addAndroidRoot(androidRoots, seenRoots, new File(currentStorageRoot, "android"));
        }

        File[] commonAndroidRoots = new File[] {
                new File("/storage/emulated/0/Android"),
                new File("/storage/emulated/0/android"),
                new File("/storage/emulated/Android"),
                new File("/storage/emulated/android"),
                new File("/storage/self/primary/Android"),
                new File("/sdcard/Android"),
                new File("/mnt/sdcard/Android")
        };
        for (File root : commonAndroidRoots) {
            addAndroidRoot(androidRoots, seenRoots, root);
        }

        // Include adopted/removable storage volumes when they expose Android data.
        File storageRoot = new File("/storage");
        File[] storageVolumes = storageRoot.listFiles();
        if (storageVolumes != null) {
            for (File volume : storageVolumes) {
                if (volume.isDirectory()) {
                    addAndroidRoot(androidRoots, seenRoots, new File(volume, "Android"));
                    addAndroidRoot(androidRoots, seenRoots, new File(volume, "android"));
                }
            }
        }

        for (File androidRoot : androidRoots) {
            addPackageDirectory(results, seen, new File(androidRoot, "data/" + packageName), packageName);
        }
        return results;
    }

    private void addAndroidRoot(List<File> results, Set<String> seen, File candidate)
            throws IOException {
        if (!candidate.isDirectory()) {
            return;
        }
        File canonical = candidate.getCanonicalFile();
        if (seen.add(canonical.getPath())) {
            results.add(canonical);
        }
    }

    private void addPackageDirectory(List<File> results, Set<String> seen, File candidate,
                                     String packageName)
            throws IOException {
        if (!candidate.isDirectory()) {
            return;
        }
        File canonical = candidate.getCanonicalFile();
        String path = canonical.getPath();
        String normalizedPath = path.replace('\\', '/').toLowerCase(java.util.Locale.ROOT);
        if (packageName.equals(canonical.getName())
                && normalizedPath.contains("/android/data/")
                && seen.add(path)) {
            results.add(canonical);
        }
    }

    private void searchForExistingTargets(File packageDirectory, String[] requiredPaths,
                                          Map<String, File> discoveredTargets)
            throws IOException {
        ArrayDeque<SearchNode> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new SearchNode(packageDirectory.getCanonicalFile(), 0));

        while (!queue.isEmpty()
                && visited.size() < 12000
                && discoveredTargets.size() < requiredPaths.length) {
            SearchNode node = queue.removeFirst();
            String canonicalPath = node.file.getCanonicalPath();
            if (!visited.add(canonicalPath)) {
                continue;
            }

            addExistingTargetsAtBase(node.file, requiredPaths, discoveredTargets);

            if (node.depth >= 10) {
                continue;
            }
            File[] children = node.file.listFiles();
            if (children == null) {
                continue;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    queue.addLast(new SearchNode(child, node.depth + 1));
                }
            }
        }
    }

    private void addExistingTargetsAtBase(File base, String[] requiredPaths,
                                          Map<String, File> discoveredTargets)
            throws IOException {
        if (!base.isDirectory()) {
            return;
        }
        for (String relativePath : requiredPaths) {
            if (discoveredTargets.containsKey(relativePath)) {
                continue;
            }
            File candidate = new File(base, relativePath);
            if (candidate.isFile()) {
                discoveredTargets.put(relativePath, candidate.getCanonicalFile());
            }
        }
    }

    private boolean isSupportedPackage(String packageName) {
        return "com.dts.freefiremax".equals(packageName);
    }

    private boolean isSafeRelativePath(String path) {
        return path != null
                && !path.isEmpty()
                && !path.startsWith("/")
                && !path.contains("..")
                && !path.contains("\\");
    }

    private void forceStop(String packageName) {
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[] { "am", "force-stop", packageName });
            process.waitFor();
        } catch (Exception ignored) {
        }
    }

    private void copyExistingFile(File source, File target) throws IOException {
        if (!source.isFile() || !target.isFile()) {
            throw new IOException("Required existing file is missing");
        }

        try (InputStream input = new FileInputStream(source);
             OutputStream output = new FileOutputStream(target, false)) {
            byte[] buffer = new byte[32768];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        }

        target.setReadable(true, false);
        target.setWritable(true, false);
    }

    private boolean sameFileContents(File source, File target) throws Exception {
        if (!source.isFile() || !target.isFile() || source.length() != target.length()) {
            return false;
        }
        return MessageDigest.isEqual(sha256(source), sha256(target));
    }

    private byte[] sha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[32768];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return digest.digest();
    }

    private static final class SearchNode {
        final File file;
        final int depth;

        SearchNode(File file, int depth) {
            this.file = file;
            this.depth = depth;
        }
    }

    private static final class PayloadTarget {
        final String relativePath;
        final File file;

        PayloadTarget(String relativePath, File file) {
            this.relativePath = relativePath;
            this.file = file;
        }
    }

    @Override
    public void destroy() {
        System.exit(0);
    }
}

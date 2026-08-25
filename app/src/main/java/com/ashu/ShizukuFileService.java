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
import java.util.List;
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

            File target = findBestExistingPayloadBase(packageName, requiredPaths, source);
            if (target == null) {
                return 14;
            }

            forceStop(packageName);

            int replacedFiles = 0;
            // Some MAX versions do not create every optional/training file. Replace and
            // verify every payload file that exists in the best matching data folder.
            for (String relativePath : requiredPaths) {
                File sourceFile = new File(source, relativePath);
                File targetFile = new File(target, relativePath);
                if (!targetFile.isFile()) {
                    continue;
                }
                copyExistingFile(sourceFile, targetFile);
                if (!sameFileContents(sourceFile, targetFile)) {
                    return 13;
                }
                replacedFiles++;
            }
            return replacedFiles > 0 ? 0 : 14;
        } catch (Exception e) {
            return 20;
        }
    }

    private File findBestExistingPayloadBase(String packageName, String[] requiredPaths, File source)
            throws IOException {
        List<File> packageDirectories = findPackageDirectories(packageName, source);
        File bestBase = null;
        int bestCount = 0;

        for (File packageDirectory : packageDirectories) {
            File standardFiles = new File(packageDirectory, "files");
            int standardCount = countExistingPaths(standardFiles, requiredPaths);
            if (standardCount > bestCount) {
                bestBase = standardFiles.getCanonicalFile();
                bestCount = standardCount;
            }

            int packageCount = countExistingPaths(packageDirectory, requiredPaths);
            if (packageCount > bestCount) {
                bestBase = packageDirectory.getCanonicalFile();
                bestCount = packageCount;
            }
        }

        // Also supports vendor/custom layouts such as Android/<package>/xxxxxxx
        // and versioned cache folders nested below the package directory.
        for (File packageDirectory : packageDirectories) {
            SearchResult discovered = searchForBestExistingSet(packageDirectory, requiredPaths);
            if (discovered.count > bestCount) {
                bestBase = discovered.base;
                bestCount = discovered.count;
            }
        }
        return bestCount > 0 ? bestBase : null;
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
            addPackageDirectory(results, seen, new File(androidRoot, packageName), packageName);
            addPackageDirectory(results, seen, new File(androidRoot, "data/" + packageName), packageName);
            addPackageDirectory(results, seen, new File(androidRoot, "media/" + packageName), packageName);
            addPackageDirectory(results, seen, new File(androidRoot, "obb/" + packageName), packageName);

            File[] firstLevel = androidRoot.listFiles();
            if (firstLevel == null) {
                continue;
            }
            for (File child : firstLevel) {
                if (!child.isDirectory()) {
                    continue;
                }
                if (packageName.equals(child.getName())) {
                    addPackageDirectory(results, seen, child, packageName);
                }
                addPackageDirectory(results, seen, new File(child, packageName), packageName);
            }
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
                && normalizedPath.contains("/android/")
                && seen.add(path)) {
            results.add(canonical);
        }
    }

    private SearchResult searchForBestExistingSet(File packageDirectory, String[] requiredPaths)
            throws IOException {
        ArrayDeque<SearchNode> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new SearchNode(packageDirectory.getCanonicalFile(), 0));
        File bestBase = null;
        int bestCount = 0;

        while (!queue.isEmpty() && visited.size() < 12000) {
            SearchNode node = queue.removeFirst();
            String canonicalPath = node.file.getCanonicalPath();
            if (!visited.add(canonicalPath)) {
                continue;
            }

            int existingCount = countExistingPaths(node.file, requiredPaths);
            if (existingCount > bestCount) {
                bestBase = node.file.getCanonicalFile();
                bestCount = existingCount;
                if (bestCount == requiredPaths.length) {
                    return new SearchResult(bestBase, bestCount);
                }
            }

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
        return new SearchResult(bestBase, bestCount);
    }

    private int countExistingPaths(File base, String[] requiredPaths) {
        if (!base.isDirectory()) {
            return 0;
        }
        int count = 0;
        for (String relativePath : requiredPaths) {
            if (new File(base, relativePath).isFile()) {
                count++;
            }
        }
        return count;
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

    private static final class SearchResult {
        final File base;
        final int count;

        SearchResult(File base, int count) {
            this.base = base;
            this.count = count;
        }
    }

    @Override
    public void destroy() {
        System.exit(0);
    }
}

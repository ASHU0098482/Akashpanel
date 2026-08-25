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
            if (!source.isDirectory()
                    || !source.getPath().contains("/Android/data/com.akash.panel/files/")) {
                return 11;
            }

            for (String relativePath : requiredPaths) {
                if (!isSafeRelativePath(relativePath)
                        || !new File(source, relativePath).isFile()) {
                    return 12;
                }
            }

            File target = findExistingPayloadBase(packageName, requiredPaths);
            if (target == null) {
                return 14;
            }

            forceStop(packageName);

            // Replace only files that already exist in the discovered game path.
            for (String relativePath : requiredPaths) {
                copyExistingFile(new File(source, relativePath), new File(target, relativePath));
            }

            for (String relativePath : requiredPaths) {
                File sourceFile = new File(source, relativePath);
                File targetFile = new File(target, relativePath);
                if (!sameFileContents(sourceFile, targetFile)) {
                    return 13;
                }
            }
            return 0;
        } catch (Exception e) {
            return 20;
        }
    }

    private File findExistingPayloadBase(String packageName, String[] requiredPaths)
            throws IOException {
        List<File> packageDirectories = findPackageDirectories(packageName);
        for (File packageDirectory : packageDirectories) {
            File standardFiles = new File(packageDirectory, "files");
            if (hasCompleteExistingSet(standardFiles, requiredPaths)) {
                return standardFiles.getCanonicalFile();
            }
            if (hasCompleteExistingSet(packageDirectory, requiredPaths)) {
                return packageDirectory.getCanonicalFile();
            }
        }

        // Supports paths such as /storage/emulated/0/Android/<package>/xxxxxxx.
        for (File packageDirectory : packageDirectories) {
            File discovered = searchForCompleteExistingSet(packageDirectory, requiredPaths);
            if (discovered != null) {
                return discovered;
            }
        }
        return null;
    }

    private List<File> findPackageDirectories(String packageName) throws IOException {
        List<File> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        File[] androidRoots = new File[] {
                new File("/storage/emulated/0/Android"),
                new File("/storage/emulated/0/android")
        };

        for (File androidRoot : androidRoots) {
            if (!androidRoot.isDirectory()) {
                continue;
            }

            addPackageDirectory(results, seen, new File(androidRoot, packageName));
            addPackageDirectory(results, seen, new File(androidRoot, "data/" + packageName));
            addPackageDirectory(results, seen, new File(androidRoot, "media/" + packageName));
            addPackageDirectory(results, seen, new File(androidRoot, "obb/" + packageName));

            File[] firstLevel = androidRoot.listFiles();
            if (firstLevel == null) {
                continue;
            }
            for (File child : firstLevel) {
                if (!child.isDirectory()) {
                    continue;
                }
                if (packageName.equals(child.getName())) {
                    addPackageDirectory(results, seen, child);
                }
                addPackageDirectory(results, seen, new File(child, packageName));
            }
        }
        return results;
    }

    private void addPackageDirectory(List<File> results, Set<String> seen, File candidate)
            throws IOException {
        if (!candidate.isDirectory()) {
            return;
        }
        File canonical = candidate.getCanonicalFile();
        String path = canonical.getPath();
        if (path.startsWith("/storage/emulated/0/") && seen.add(path)) {
            results.add(canonical);
        }
    }

    private File searchForCompleteExistingSet(File packageDirectory, String[] requiredPaths)
            throws IOException {
        ArrayDeque<SearchNode> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new SearchNode(packageDirectory.getCanonicalFile(), 0));

        while (!queue.isEmpty() && visited.size() < 4000) {
            SearchNode node = queue.removeFirst();
            String canonicalPath = node.file.getCanonicalPath();
            if (!visited.add(canonicalPath)) {
                continue;
            }
            if (hasCompleteExistingSet(node.file, requiredPaths)) {
                return node.file.getCanonicalFile();
            }
            if (node.depth >= 6) {
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
        return null;
    }

    private boolean hasCompleteExistingSet(File base, String[] requiredPaths) {
        if (!base.isDirectory()) {
            return false;
        }
        for (String relativePath : requiredPaths) {
            if (!new File(base, relativePath).isFile()) {
                return false;
            }
        }
        return true;
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

    @Override
    public void destroy() {
        System.exit(0);
    }
}

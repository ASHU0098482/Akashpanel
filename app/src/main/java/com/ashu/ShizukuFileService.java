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

            File target = findTargetFilesDirectory(packageName, source);
            if (target == null) {
                return 14;
            }
            if (!target.exists() && !target.mkdirs()) {
                return 15;
            }

            forceStop(packageName);

            int replacedFiles = 0;
            // Mirror the complete supplied files tree into MAX's standard files folder.
            // Existing files are overwritten; missing payload directories/files are created.
            for (String relativePath : requiredPaths) {
                File sourceFile = new File(source, relativePath);
                File targetFile = new File(target, relativePath);
                copyFileCreatingParents(sourceFile, targetFile);
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

    private File findTargetFilesDirectory(String packageName, File source) throws IOException {
        String sourcePath = source.getCanonicalPath().replace('\\', '/');
        String sourcePathLower = sourcePath.toLowerCase(java.util.Locale.ROOT);
        String marker = "/android/data/com.akash.panel/files/";
        int markerIndex = sourcePathLower.indexOf(marker);
        if (markerIndex > 0) {
            String currentStorageRoot = sourcePath.substring(0, markerIndex);
            File target = new File(currentStorageRoot,
                    "Android/data/" + packageName + "/files").getCanonicalFile();
            String normalizedTarget = target.getPath()
                    .replace('\\', '/')
                    .toLowerCase(java.util.Locale.ROOT);
            String requiredSuffix = "/android/data/" + packageName.toLowerCase(
                    java.util.Locale.ROOT) + "/files";
            if (normalizedTarget.endsWith(requiredSuffix)) {
                return target;
            }
        }

        List<File> packageDirectories = findPackageDirectories(packageName, source);
        if (packageDirectories.isEmpty()) {
            return null;
        }
        return new File(packageDirectories.get(0), "files").getCanonicalFile();
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

    private void copyFileCreatingParents(File source, File target) throws IOException {
        if (!source.isFile()) {
            throw new IOException("Required source file is missing");
        }

        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create target directory");
        }

        try {
            copyWithStreams(source, target);
        } catch (IOException streamError) {
            // Toybox cp is available to the Shizuku shell process and works around
            // vendor-specific FUSE stream failures on some Android builds.
            try {
                Process process = Runtime.getRuntime().exec(new String[] {
                        "cp", "-f", source.getAbsolutePath(), target.getAbsolutePath()
                });
                int exitCode = process.waitFor();
                if (exitCode != 0 || !target.isFile()) {
                    throw streamError;
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw streamError;
            }
        }

        target.setReadable(true, false);
        target.setWritable(true, false);
    }

    private void copyWithStreams(File source, File target) throws IOException {
        try (InputStream input = new FileInputStream(source);
             OutputStream output = new FileOutputStream(target, false)) {
            byte[] buffer = new byte[32768];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        }
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

    @Override
    public void destroy() {
        System.exit(0);
    }
}

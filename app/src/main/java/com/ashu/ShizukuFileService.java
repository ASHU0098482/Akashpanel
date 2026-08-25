package com.ashu;

import android.content.Context;

import androidx.annotation.Keep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Runs in Shizuku's ADB-shell process; it is not an Android manifest service. */
public class ShizukuFileService extends IShizukuFileService.Stub {

    public ShizukuFileService() {
    }

    @Keep
    public ShizukuFileService(Context context) {
    }

    @Override
    public int replaceFiles(String sourceDir, String targetDir, String packageName,
                            String[] requiredPaths) {
        try {
            if (!isSupportedPackage(packageName) || requiredPaths == null || requiredPaths.length == 0) {
                return 10;
            }

            File source = new File(sourceDir).getCanonicalFile();
            File target = new File(targetDir).getCanonicalFile();
            String expectedTarget = "/storage/emulated/0/Android/data/" + packageName + "/files";

            if (!source.isDirectory()
                    || !source.getPath().contains("/Android/data/com.akash.panel/files/")
                    || !expectedTarget.equals(target.getPath())) {
                return 11;
            }

            for (String relativePath : requiredPaths) {
                if (!isSafeRelativePath(relativePath)
                        || !new File(source, relativePath).isFile()) {
                    return 12;
                }
            }

            forceStop(packageName);
            copyDirectory(source, target);

            for (String relativePath : requiredPaths) {
                File sourceFile = new File(source, relativePath);
                File targetFile = new File(target, relativePath);
                if (!targetFile.isFile()
                        || sourceFile.length() <= 0
                        || sourceFile.length() != targetFile.length()) {
                    return 13;
                }
            }
            return 0;
        } catch (Exception e) {
            return 20;
        }
    }

    private boolean isSupportedPackage(String packageName) {
        return "com.dts.freefireth".equals(packageName)
                || "com.dts.freefiremax".equals(packageName);
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

    private void copyDirectory(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists() && !target.mkdirs()) {
                throw new IOException("Cannot create " + target);
            }
            File[] children = source.listFiles();
            if (children == null) {
                throw new IOException("Cannot read " + source);
            }
            for (File child : children) {
                copyDirectory(child, new File(target, child.getName()));
            }
            return;
        }

        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create " + parent);
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

    @Override
    public void destroy() {
        System.exit(0);
    }
}

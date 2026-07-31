package com.ashu;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Runtime.getRuntime().exec("su");
        } catch (Exception e) {
            // Root not found - handled silently
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        startAppFlow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fully terminate process so floating window and drawing loops stop running in background
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void startAppFlow() {
        RemoteConfig.fetchConfig(() -> {
            runOnUiThread(() -> {
                if (!RemoteConfig.isOnline) {
                    showMaintenanceDialog(RemoteConfig.maintenanceMessage);
                    return;
                }

                int localVersion = 1;
                try {
                    localVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (RemoteConfig.remoteVersionCode > localVersion) {
                    showUpdateDialog(RemoteConfig.updateUrl);
                    return;
                }

                if (RemoteConfig.showNotice && RemoteConfig.noticeMessage != null && !RemoteConfig.noticeMessage.isEmpty()) {
                    showNoticeDialog(RemoteConfig.noticeTitle, RemoteConfig.noticeMessage, () -> showFirstSplash());
                } else {
                    showFirstSplash();
                }
            });
        });
    }

    private void showNoticeDialog(String title, String message, Runnable onContinue) {
        String displayTitle = (title != null && !title.isEmpty()) ? title : "📢 Notice";
        new android.app.AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(displayTitle)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK", (d, which) -> {
                d.dismiss();
                if (onContinue != null) {
                    onContinue.run();
                }
            })
            .create()
            .show();
    }

    private void showMaintenanceDialog(String message) {
        new android.app.AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("Under Maintenance")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("EXIT", (d, which) -> {
                finishAffinity();
            })
            .create()
            .show();
    }

    private void showUpdateDialog(final String updateUrl) {
        String msg = (RemoteConfig.noticeMessage != null && !RemoteConfig.noticeMessage.isEmpty()) 
            ? RemoteConfig.noticeMessage + "\n\nTap 'UPDATE NOW' to download and install."
            : "A new update is available. Tap 'UPDATE NOW' to download and install automatically.";
        String title = (RemoteConfig.noticeTitle != null && !RemoteConfig.noticeTitle.isEmpty())
            ? RemoteConfig.noticeTitle : "🔄 Update Available!";
        new android.app.AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("UPDATE NOW", (d, which) -> {
                downloadAndInstallApk(updateUrl);
            })
            .setNegativeButton("EXIT", (d, which) -> {
                finishAffinity();
            })
            .create()
            .show();
    }

    private void downloadAndInstallApk(final String apkUrl) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        progressDialog.setTitle("Downloading Update...");
        progressDialog.setMessage("Please wait while downloading the latest APK update.");
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                java.io.File updatesDir = new java.io.File(getExternalFilesDir(null), "updates");
                if (!updatesDir.exists()) updatesDir.mkdirs();
                java.io.File apkFile = new java.io.File(updatesDir, "VIP_PANEL_update.apk");
                if (apkFile.exists()) apkFile.delete();

                String finalUrl = apkUrl;
                if (finalUrl.contains("?")) {
                    finalUrl += "&t=" + System.currentTimeMillis();
                } else {
                    finalUrl += "?t=" + System.currentTimeMillis();
                }

                java.net.URL url = new java.net.URL(finalUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int status = conn.getResponseCode();
                if (status == java.net.HttpURLConnection.HTTP_MOVED_TEMP
                        || status == java.net.HttpURLConnection.HTTP_MOVED_PERM
                        || status == 307 || status == 308) {
                    String newUrl = conn.getHeaderField("Location");
                    conn = (java.net.HttpURLConnection) new java.net.URL(newUrl).openConnection();
                    conn.setRequestMethod("GET");
                }

                int fileLength = conn.getContentLength();
                java.io.InputStream input = conn.getInputStream();
                java.io.FileOutputStream output = new java.io.FileOutputStream(apkFile);

                byte[] buffer = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (fileLength > 0) {
                        final int progress = (int) (total * 100 / fileLength);
                        runOnUiThread(() -> progressDialog.setProgress(progress));
                    }
                    output.write(buffer, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                conn.disconnect();

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    installApk(apkFile);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showUpdateDialog(apkUrl); // Show dialog again if download fails
                });
            }
        }).start();
    }

    private void installApk(java.io.File apkFile) {
        // Try root silent install first
        try {
            Process process = Runtime.getRuntime().exec("su");
            java.io.OutputStream os = process.getOutputStream();
            // Copy to /data/local/tmp/ to bypass SELinux and namespace read restrictions
            os.write(("cp " + apkFile.getAbsolutePath() + " /data/local/tmp/update.apk\n").getBytes());
            os.write(("chmod 666 /data/local/tmp/update.apk\n").getBytes());
            os.write(("pm install -r /data/local/tmp/update.apk\n").getBytes());
            os.write(("rm /data/local/tmp/update.apk\n").getBytes());
            os.write("exit\n".getBytes());
            os.flush();
            int result = process.waitFor();
            if (result == 0) {
                // Silent install succeeded!
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
                return;
            }
        } catch (Exception ignored) {
        }

        // Fallback to standard installer
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            android.net.Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7+ uses FileProvider
                apkUri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = android.net.Uri.fromFile(apkFile);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Install failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showFirstSplash() {
        // Root container
      ////  LinearLayout layout = new LinearLayout(this);
      //  layout.setOrientation(LinearLayout.VERTICAL);
      //  layout.setGravity(Gravity.CENTER);
      ////  layout.setBackgroundColor(Color.BLACK);

        // GIF view (full screen)
       // ImageView gifView = new ImageView(this);
      //  LinearLayout.LayoutParams gifParams = new LinearLayout.LayoutParams(
      //          LinearLayout.LayoutParams.MATCH_PARENT,
        //        LinearLayout.LayoutParams.MATCH_PARENT
       // );
     //   gifView.setLayoutParams(gifParams);
      //  gifView.setScaleType(ImageView.ScaleType.CENTER_CROP);

       // layout.addView(gifView);
     //   setContentView(layout);

        // Load your GIF from URL
      //  Glide.with(this)
       //         .asGif()
          //      .load("https://media.giphy.com/media/PNlNcLUSK5tbE5a973/giphy.gif") // 🔗 replace with your gif url
        ///        .into(gifView);

        // After 4s → go check permission
        new Handler().postDelayed(this::checkOverlayPermission, 1000);
    }


    private void animateTypewriter(TextView textView, String fullText, int delay) {
        final Handler handler = new Handler();
        final int[] index = {0};

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    textView.setText(fullText.substring(0, index[0]));
                    textView.setShadowLayer(25, 0, 0, Color.CYAN); // neon glow refresh
                    index[0]++;
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission is required!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                return;
            }
        }
        startLogin();
    }

    private void startLogin() {
        new Login(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission denied! Exiting...", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                startLogin();
            }
        }
    }
}

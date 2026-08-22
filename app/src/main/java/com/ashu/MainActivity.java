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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;


public class MainActivity extends Activity {

    public static MainActivity instance;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private static final int INSTALL_UNKNOWN_APPS_REQUEST_CODE = 102;
    private java.io.File pendingInstallApkFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

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
                    downloadAndInstallApk(RemoteConfig.updateUrl);
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

    public void checkForUpdates(final boolean showToastIfUpToDate) {
        RemoteConfig.fetchConfig(() -> {
            runOnUiThread(() -> {
                int localVersion = 1;
                try {
                    localVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (RemoteConfig.remoteVersionCode > localVersion) {
                    downloadAndInstallApk(RemoteConfig.updateUrl);
                } else if (showToastIfUpToDate) {
                    downloadAndInstallApk(RemoteConfig.updateUrl);
                }
            });
        });
    }

    public void showUpdateDialog(final String updateUrl) {
        final String validUpdateUrl = (updateUrl != null && !updateUrl.isEmpty())
            ? updateUrl : "https://raw.githubusercontent.com/ASHU0098482/Akashpanel/main/VIP_PANEL.apk";
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
                downloadAndInstallApk(validUpdateUrl);
            })
            .setNegativeButton("EXIT", (d, which) -> {
                finishAffinity();
            })
            .create()
            .show();
    }

    public void downloadAndInstallApk(final String apkUrl) {
        final String downloadUrl = (apkUrl != null && !apkUrl.isEmpty())
            ? apkUrl : "https://raw.githubusercontent.com/ASHU0098482/Akashpanel/main/VIP_PANEL.apk";
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        progressDialog.setTitle("Auto Updating AKASH PANEL...");
        progressDialog.setMessage("Downloading and applying latest online update...");
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

                String currentUrl = downloadUrl;
                if (currentUrl.contains("?")) {
                    currentUrl += "&t=" + System.currentTimeMillis();
                } else {
                    currentUrl += "?t=" + System.currentTimeMillis();
                }

                java.net.HttpURLConnection conn = null;
                int redirects = 0;
                while (redirects < 10) {
                    java.net.URL url = new java.net.URL(currentUrl);
                    conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
                    conn.setRequestProperty("Accept", "*/*");

                    int status = conn.getResponseCode();
                    if (status == java.net.HttpURLConnection.HTTP_MOVED_TEMP
                            || status == java.net.HttpURLConnection.HTTP_MOVED_PERM
                            || status == java.net.HttpURLConnection.HTTP_SEE_OTHER
                            || status == 307 || status == 308) {
                        String newUrl = conn.getHeaderField("Location");
                        if (newUrl != null && !newUrl.isEmpty()) {
                            currentUrl = newUrl;
                            redirects++;
                            conn.disconnect();
                            continue;
                        }
                    }
                    break;
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

    private void installApk(final java.io.File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            Toast.makeText(this, "Update file not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Try silent root install if root is available
        try {
            Process process = Runtime.getRuntime().exec("su");
            java.io.OutputStream os = process.getOutputStream();
            os.write(("cp " + apkFile.getAbsolutePath() + " /data/local/tmp/update.apk\n").getBytes());
            os.write(("chmod 777 /data/local/tmp/update.apk\n").getBytes());
            os.write(("chcon u:object_r:shell_data_file:s0 /data/local/tmp/update.apk 2>/dev/null\n").getBytes());
            os.write(("pm install -r -d -g /data/local/tmp/update.apk || pm install -r -d -g --user 0 /data/local/tmp/update.apk\n").getBytes());
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

        // 2. Non-root install flow
        pendingInstallApkFile = apkFile;

        // Check Unknown App Install permission on Android 8.0+ for non-root installer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Toast.makeText(this, "Please allow 'Install Unknown Apps' permission to complete update", Toast.LENGTH_LONG).show();
                try {
                    Intent permIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(permIntent, INSTALL_UNKNOWN_APPS_REQUEST_CODE);
                    return; // Wait for user to grant permission and return
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Launch standard package installer
        launchPackageInstaller(apkFile);
    }

    private void launchPackageInstaller(java.io.File apkFile) {
        if (apkFile == null || !apkFile.exists()) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            android.net.Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = android.net.Uri.fromFile(apkFile);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Grant URI read permissions explicitly to package installer handlers
            java.util.List<android.content.pm.ResolveInfo> resolveInfoList = getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfoList != null) {
                for (android.content.pm.ResolveInfo resolveInfo : resolveInfoList) {
                    String pkg = resolveInfo.activityInfo.packageName;
                    grantUriPermission(pkg, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Install failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showFirstSplash() {
        // === ANIMATED SPLASH INTRO (inspired by example video) ===
        // Black fullscreen background
        final android.widget.FrameLayout splashRoot = new android.widget.FrameLayout(this);
        splashRoot.setBackgroundColor(Color.BLACK);
        splashRoot.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));

        // --- PHASE 1: Logo in center, starts small and zooms in ---
        final ImageView logoView = new ImageView(this);
        android.widget.FrameLayout.LayoutParams logoParams = new android.widget.FrameLayout.LayoutParams(
                dpToPx(180), dpToPx(180));
        logoParams.gravity = android.view.Gravity.CENTER;
        logoView.setLayoutParams(logoParams);
        logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logoView.setAlpha(0f);
        logoView.setScaleX(0.15f);
        logoView.setScaleY(0.15f);
        logoView.setImageResource(R.mipmap.ic_launcher);
        splashRoot.addView(logoView);

        // Load logo from remote config or fallback
        String logoUrl = null;
        if (com.ashu.RemoteConfig.logoUrl != null && !com.ashu.RemoteConfig.logoUrl.isEmpty()) {
            logoUrl = com.ashu.RemoteConfig.logoUrl;
            if (logoUrl.contains("?")) {
                logoUrl += "&t=" + System.currentTimeMillis();
            } else {
                logoUrl += "?t=" + System.currentTimeMillis();
            }
        }
        if (logoUrl != null) {
            com.bumptech.glide.Glide.with(this)
                .asBitmap()
                .load(logoUrl)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                    @Override
                    public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource,
                            @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                        android.graphics.Bitmap transparentBitmap = com.ashu.Utils.makeBlackTransparent(resource);
                        logoView.setImageBitmap(transparentBitmap);
                    }
                    @Override
                    public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {}
                });
        }

        // --- PHASE 2: App name text (letter-by-letter) ---
        final TextView splashText = new TextView(this);
        // Get app name from remote config
        String appName = (com.ashu.RemoteConfig.appName != null && !com.ashu.RemoteConfig.appName.isEmpty())
                ? com.ashu.RemoteConfig.appName : "AKASH PANEL";
        splashText.setText("");
        splashText.setTextSize(36);
        splashText.setTextColor(Color.parseColor("#EF4444")); // Red accent
        splashText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        splashText.setGravity(android.view.Gravity.CENTER);
        splashText.setAlpha(0f);
        // Neon glow shadow
        splashText.setShadowLayer(30, 0, 0, Color.parseColor("#EF4444"));
        android.widget.FrameLayout.LayoutParams textParams = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
        textParams.gravity = android.view.Gravity.CENTER;
        textParams.topMargin = dpToPx(130);
        splashText.setLayoutParams(textParams);
        splashText.setLetterSpacing(0.15f);
        splashRoot.addView(splashText);

        // --- PHASE 3: Subtitle glow line ---
        final View glowLine = new View(this);
        android.widget.FrameLayout.LayoutParams glowParams = new android.widget.FrameLayout.LayoutParams(
                0, dpToPx(2));
        glowParams.gravity = android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.CENTER_VERTICAL;
        glowParams.topMargin = dpToPx(185);
        glowLine.setLayoutParams(glowParams);
        glowLine.setBackgroundColor(Color.parseColor("#EF4444"));
        glowLine.setAlpha(0f);
        splashRoot.addView(glowLine);

        setContentView(splashRoot);

        final Handler handler = new Handler();
        final String finalAppName = appName;

        // ====== ANIMATION SEQUENCE ======

        // STEP 1: Logo zoom-in + fade-in (0ms - 800ms)
        handler.postDelayed(() -> {
            logoView.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(800)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.5f))
                .start();
        }, 200);

        // STEP 2: Logo pulse glow effect (800ms - 1400ms)
        handler.postDelayed(() -> {
            logoView.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(300)
                .withEndAction(() -> {
                    logoView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(300)
                        .start();
                })
                .start();
        }, 1100);

        // STEP 3: Logo shrinks up + text starts appearing letter by letter (1500ms+)
        handler.postDelayed(() -> {
            // Move logo up
            logoView.animate()
                .translationY(-dpToPx(60))
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(500)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

            // Start letter-by-letter text reveal
            splashText.setAlpha(1f);
            final int[] charIndex = {0};
            final int letterDelay = 80; // ms per letter
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (charIndex[0] <= finalAppName.length()) {
                        splashText.setText(finalAppName.substring(0, charIndex[0]));
                        // Pulse the glow intensity
                        float glowRadius = 20 + (charIndex[0] % 3) * 10;
                        splashText.setShadowLayer(glowRadius, 0, 0, Color.parseColor("#EF4444"));
                        charIndex[0]++;
                        handler.postDelayed(this, letterDelay);
                    }
                }
            }, 300);
        }, 1600);

        // STEP 4: Glow line expands (after text is fully revealed)
        int textRevealDuration = 1600 + 300 + (appName.length() * 80) + 200;
        handler.postDelayed(() -> {
            glowLine.setAlpha(1f);
            android.animation.ValueAnimator lineAnim = android.animation.ValueAnimator.ofInt(0, dpToPx(200));
            lineAnim.setDuration(400);
            lineAnim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            lineAnim.addUpdateListener(animation -> {
                int val = (int) animation.getAnimatedValue();
                android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) glowLine.getLayoutParams();
                lp.width = val;
                glowLine.setLayoutParams(lp);
            });
            lineAnim.start();
        }, textRevealDuration);

        // STEP 5: Full neon glow pulse on text (peak moment)
        handler.postDelayed(() -> {
            // Intense glow pulse
            android.animation.ValueAnimator glowAnim = android.animation.ValueAnimator.ofFloat(30f, 60f, 30f);
            glowAnim.setDuration(600);
            glowAnim.setRepeatCount(1);
            glowAnim.addUpdateListener(animation -> {
                float radius = (float) animation.getAnimatedValue();
                splashText.setShadowLayer(radius, 0, 0, Color.parseColor("#EF4444"));
            });
            glowAnim.start();
        }, textRevealDuration + 200);

        // STEP 6: Fade out everything and proceed to login (after all animations)
        int totalSplashDuration = textRevealDuration + 1200;
        handler.postDelayed(() -> {
            // Fade out all splash elements
            splashRoot.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(() -> {
                    // Proceed to overlay permission check -> Login
                    checkOverlayPermission();
                })
                .start();
        }, totalSplashDuration);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
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
        } else if (requestCode == INSTALL_UNKNOWN_APPS_REQUEST_CODE) {
            if (pendingInstallApkFile != null && pendingInstallApkFile.exists()) {
                launchPackageInstaller(pendingInstallApkFile);
            }
        }
    }
}

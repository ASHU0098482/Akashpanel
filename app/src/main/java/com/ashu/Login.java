package com.ashu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.widget.LinearLayout;

public class Login {
    private Context context;
    private Utils utils;
    private ProgressBar loadingBar;
    private TextView loadingText;
    private boolean isSettingsVisible = false;

    private LinearLayout rootContainer, card;
    private EditText inputLicense;
    private Button loginButton;
    private TextView title, subtitle;
    private LinearLayout settingsLayout;
    private Switch suToggle;
    private TextView suLabel;
    public native void sendOwnerIDToNative(String ownerId);

    public static Context globalContext;

    private static final String APP_NAME = "vip panel";
    private static final String OWNER_ID = "8Z9qRQ2zph";
    private static final String SECRET = "fddc19ec5be9ebee148b808beaa5dad04f803aac21cf6f4a224a5f832ef97dbd";
    private static final String VERSION = "1.0";
    private static final String API_URL = "https://keyauth.win/api/1.3/";


    static {
        System.loadLibrary("hawdawdawdawda");
    }

    public Login(Context context) {
        Login.globalContext = context;
        this.context = context;
        this.utils = new Utils(context);
        Init(); // Show splash first
    }


    private void Init() {
        showNoticeIfAvailable();
        // === STEP 1: Build the card view and all its children ===
        card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(
                new Utils(context).FixDP(25),
                new Utils(context).FixDP(25),
                new Utils(context).FixDP(25),
                new Utils(context).FixDP(25)
        );

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#1b1b1b"));
        cardBg.setCornerRadius(new Utils(context).FixDP(20));
        card.setBackground(cardBg);

        // Add logo
        final ImageView logoView = new ImageView(context);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                new Utils(context).FixDP(120),
                new Utils(context).FixDP(120)
        );
        logoParams.setMargins(0, 0, 0, new Utils(context).FixDP(15));
        logoView.setLayoutParams(logoParams);
        logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logoView.setImageResource(R.mipmap.ic_launcher);
        card.addView(logoView);

        if (RemoteConfig.logoUrl != null && !RemoteConfig.logoUrl.isEmpty()) {
            
            String logoFetchUrl = RemoteConfig.logoUrl;
            if (logoFetchUrl != null && !logoFetchUrl.isEmpty()) {
                if (logoFetchUrl.contains("?")) {
                    logoFetchUrl += "&t=" + System.currentTimeMillis();
                } else {
                    logoFetchUrl += "?t=" + System.currentTimeMillis();
                }
            }
            com.bumptech.glide.Glide.with(context)
                .asBitmap()
                .load(logoFetchUrl)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                    @Override
                    public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                        android.graphics.Bitmap transparentBitmap = Utils.makeBlackTransparent(resource);
                        logoView.setImageBitmap(transparentBitmap);
                    }
                    @Override
                    public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {
                    }
                });
        }

        // Title with red first word and white rest dynamically from RemoteConfig
        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        titleLayout.setGravity(Gravity.CENTER);

        String remoteAppName = RemoteConfig.appName;
        String firstWord = "VIP";
        String secondWord = "PANEL";
        if (remoteAppName != null && remoteAppName.contains(" ")) {
            int spaceIdx = remoteAppName.indexOf(" ");
            firstWord = remoteAppName.substring(0, spaceIdx);
            secondWord = remoteAppName.substring(spaceIdx + 1);
        } else if (remoteAppName != null && !remoteAppName.isEmpty()) {
            firstWord = remoteAppName;
            secondWord = "";
        }

        TextView titleRed = new TextView(context);
        titleRed.setText(firstWord + "  ");
        titleRed.setTextSize(24);
        titleRed.setTextColor(Color.parseColor("#A855F7"));
        titleRed.setTypeface(null, Typeface.BOLD);

        TextView titleWhite = new TextView(context);
        titleWhite.setText(secondWord);
        titleWhite.setTextSize(24);
        titleWhite.setTextColor(Color.WHITE);
        titleWhite.setTypeface(null, Typeface.BOLD);

        titleLayout.addView(titleRed);
        titleLayout.addView(titleWhite);
        card.addView(titleLayout);

        subtitle = new TextView(context);
        subtitle.setText("FREE FIRE");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, new Utils(context).FixDP(15));
        card.addView(subtitle);

        // Settings Layout (hidden by default)
        settingsLayout = new LinearLayout(context);
        settingsLayout.setOrientation(LinearLayout.VERTICAL);
        settingsLayout.setVisibility(View.GONE);

        LinearLayout suRow = new LinearLayout(context);
        suRow.setOrientation(LinearLayout.HORIZONTAL);
        suRow.setGravity(Gravity.CENTER_VERTICAL);
        suRow.setPadding(0, new Utils(context).FixDP(5), 0, new Utils(context).FixDP(5));

        suLabel = new TextView(context);
        suLabel.setText("ENABLE ROOT BYPASS");
        suLabel.setTypeface(Typeface.DEFAULT_BOLD);
        suLabel.setTextSize(15);
        suLabel.setTextColor(Color.WHITE);
        suLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        suToggle = new Switch(context);
        suToggle.setChecked(isSuRenamed());

        suToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String from = isChecked ? "/system/xbin/su" : "/system/xbin/su1";
            String to = isChecked ? "/system/xbin/su1" : "/system/xbin/su";
            try {
                Process process = Runtime.getRuntime().exec(isChecked ? "su" : "su1");
                process.getOutputStream().write(("mount -o remount,rw /system\n").getBytes());
                process.getOutputStream().write(("mv " + from + " " + to + "\n").getBytes());
                process.getOutputStream().write("exit\n".getBytes());
                process.getOutputStream().flush();
                process.waitFor();
                showToast("BYPASS ROOT " + (isChecked ? "SUCCESSFUL" : "DISABLED"));
            } catch (Exception e) {
                showToast("ROOT FAILED: " + e.getMessage());
            }
        });

        suRow.addView(suLabel);
        suRow.addView(suToggle);
        settingsLayout.addView(suRow);
        card.addView(settingsLayout);

        // License input
        inputLicense = new EditText(context);
        inputLicense.setHint("ENTER LICENSE KEY");
        inputLicense.setTextSize(16);
        inputLicense.setTextColor(Color.WHITE);
        inputLicense.setHintTextColor(Color.GRAY);
        inputLicense.setGravity(Gravity.CENTER);
        inputLicense.setPadding(
                new Utils(context).FixDP(15),
                new Utils(context).FixDP(12),
                new Utils(context).FixDP(15),
                new Utils(context).FixDP(12)
        );
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setCornerRadius(new Utils(context).FixDP(10));
        inputBg.setColor(Color.parseColor("#2c2c2c"));
        inputLicense.setBackground(inputBg);
        card.addView(inputLicense);

        inputLicense.setText(context.getSharedPreferences("ASHUPrefs", Context.MODE_PRIVATE)
                .getString("saved_license", ""));

        // Login button
        loginButton = new Button(context);
        loginButton.setText("LOGIN");
        loginButton.setTextColor(Color.WHITE);
        loginButton.setTextSize(16);
        loginButton.setPadding(
                new Utils(context).FixDP(15),
                new Utils(context).FixDP(15),
                new Utils(context).FixDP(15),
                new Utils(context).FixDP(15)
        );
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#9333EA"));
        btnBg.setCornerRadius(new Utils(context).FixDP(50));
        loginButton.setBackground(btnBg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, new Utils(context).FixDP(15), 0, new Utils(context).FixDP(5));
        loginButton.setLayoutParams(btnParams);
        card.addView(loginButton);

        // Loading indicator
        LinearLayout loadingLayout = new LinearLayout(context);
        loadingLayout.setOrientation(LinearLayout.HORIZONTAL);
        loadingLayout.setGravity(Gravity.CENTER);
        loadingLayout.setPadding(0, 10, 0, 10);

        loadingBar = new ProgressBar(context);
        loadingBar.setVisibility(View.GONE);
        loadingBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#A855F7"), PorterDuff.Mode.SRC_IN);

        loadingText = new TextView(context);
        loadingText.setText("Verifying License Key...");
        loadingText.setTextColor(Color.parseColor("#A855F7"));
        loadingText.setTextSize(14);
        loadingText.setPadding(20, 0, 0, 0);
        loadingText.setVisibility(View.GONE);

        loadingLayout.addView(loadingBar);
        loadingLayout.addView(loadingText);
        card.addView(loadingLayout);

        // === STEP 2: Build the root view and add card exactly ONCE ===
        rootContainer = new LinearLayout(context);
        rootContainer.setOrientation(LinearLayout.VERTICAL);
        rootContainer.setGravity(Gravity.CENTER);
        rootContainer.setBackgroundColor(Color.rgb(23, 23, 23));

        boolean hasBackground = RemoteConfig.backgroundUrl != null && !RemoteConfig.backgroundUrl.isEmpty();

        if (hasBackground) {
            // Use FrameLayout so background image sits behind card
            android.widget.FrameLayout rootFrame = new android.widget.FrameLayout(context);
            rootFrame.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ImageView backgroundView = new ImageView(context);
            backgroundView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            backgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            rootFrame.addView(backgroundView);

            // card params for centering over the background
            android.widget.FrameLayout.LayoutParams cardParams =
                    new android.widget.FrameLayout.LayoutParams(new Utils(context).FixDP(320), ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.gravity = Gravity.CENTER;
            card.setLayoutParams(cardParams);

            GradientDrawable cardBgOver = new GradientDrawable();
            cardBgOver.setColor(Color.parseColor("#E61b1b1b"));
            cardBgOver.setCornerRadius(40);
            card.setBackground(cardBgOver);

            rootFrame.addView(card); // add card ONCE here
            rootContainer.addView(rootFrame);

            com.bumptech.glide.Glide.with(context).load(RemoteConfig.backgroundUrl).into(backgroundView);
        } else {
            card.setLayoutParams(new LinearLayout.LayoutParams(new Utils(context).FixDP(320), ViewGroup.LayoutParams.WRAP_CONTENT));
            rootContainer.addView(card); // add card ONCE here
        }

        ((Activity) context).setContentView(rootContainer);

        // Button listener
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        final String licenseKey = inputLicense.getText().toString().trim();
        if (licenseKey.isEmpty()) {
            showToast("License key required.");
            return;
        }

        if (!RemoteConfig.isOnline) {
            new Handler(Looper.getMainLooper()).post(() -> {
                showNoticeIfAvailable();
                showToast("❌ Access Disabled. Please check update notice.");
            });
            return;
        }

        loginButton.setEnabled(false);
        loadingBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        setStatus("🔄 Connecting to server...", Color.WHITE, true);

        new Thread(() -> {
            try {
                String hwid = getHWID();

                String encodedName = java.net.URLEncoder.encode(
                        RemoteConfig.keyauthAppName != null ? RemoteConfig.keyauthAppName : "vip panel", "UTF-8");
                String encodedSecret = java.net.URLEncoder.encode(
                        RemoteConfig.keyauthSecret != null ? RemoteConfig.keyauthSecret : SECRET, "UTF-8");
                String initUrl = RemoteConfig.keyauthUrl + "?type=init&ver=" + RemoteConfig.keyauthVersion
                        + "&name=" + encodedName + "&ownerid=" + RemoteConfig.keyauthOwnerId
                        + "&secret=" + encodedSecret;
                JSONObject initRes = sendRequest(initUrl);

                if (!initRes.getBoolean("success")) {
                    postError("❌ Init failed: " + initRes.optString("message"));
                    return;
                }

                setStatus("🔐 Verifying license...", Color.WHITE, true);

                String encodedKey = java.net.URLEncoder.encode(licenseKey, "UTF-8");
                String encodedHwid = java.net.URLEncoder.encode(hwid, "UTF-8");
                String loginUrl = RemoteConfig.keyauthUrl + "?type=license&key=" + encodedKey
                        + "&hwid=" + encodedHwid
                        + "&sessionid=" + initRes.getString("sessionid")
                        + "&name=" + encodedName
                        + "&ownerid=" + RemoteConfig.keyauthOwnerId
                        + "&ver=" + RemoteConfig.keyauthVersion;
                JSONObject loginRes = sendRequest(loginUrl);

                if (loginRes.getBoolean("success")) {
                    sendOwnerIDToNative(RemoteConfig.keyauthOwnerId);
                    context.getSharedPreferences("ASHUPrefs", Context.MODE_PRIVATE)
                            .edit().putString("saved_license", licenseKey).apply();

                    new Handler(Looper.getMainLooper()).post(() -> {
                        setStatus("Welcome " + licenseKey + " 👑", Color.WHITE, false);
                        showToast("Welcome " + licenseKey + " 👑");
                        new Menu(context, 1);
                        isSettingsVisible = true;
                        settingsLayout.setVisibility(View.VISIBLE);
                        inputLicense.setVisibility(View.GONE);
                        loginButton.setVisibility(View.GONE);

                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.dts.freefiremax");
                        if (launchIntent == null) {
                            launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.dts.freefireth");
                        }
                        if (launchIntent != null) {
                            context.startActivity(launchIntent);
                        } else {
                            // Fallback: try launching directly via explicit intent
                            try {
                                Intent fallback = new Intent();
                                fallback.setClassName("com.dts.freefiremax", "com.epicgames.ue4.SplashActivity");
                                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(fallback);
                            } catch (Exception e) {
                                showToast("Free Fire Max not found. Please install it.");
                            }
                        }
                    });

                } else {
                    postError("❌ Login failed: " + loginRes.optString("message"));
                }

            } catch (Exception e) {
                postError("❌ Error: " + e.getMessage());
            }
        }).start();
    }

    private void setStatus(String message, int color, boolean showProgress) {
        new Handler(Looper.getMainLooper()).post(() -> {
            loadingText.setText(message);
            loadingText.setTextColor(color);
            loadingBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        });
    }

    private void postError(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            showToast(message);
            loginButton.setEnabled(true);
            loadingBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
        });
    }

    private JSONObject sendRequest(String urlString) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        java.io.InputStream stream = conn.getResponseCode() >= 400
                ? conn.getErrorStream() : conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        String raw = response.toString().trim();
        // If server returned a plain string (not JSON), wrap it as error JSON
        if (!raw.startsWith("{")) {
            return new JSONObject("{\"success\":false,\"message\":\"Server error: " + raw + "\"}");
        }
        return new JSONObject(raw);
    }

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    public static void showToastFromNative(final Context context, final String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    private String getHWID() {
        String rawHwid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (rawHwid == null || rawHwid.isEmpty()) {
            rawHwid = "defaultandroidid12345";
        }
        String combined = rawHwid + "-vip-panel-hwid-secure";
        return combined.substring(0, Math.max(20, combined.length()));
    }

    private boolean isSuRenamed() {
        return !new java.io.File("/system/xbin/su").exists();
    }

    private void showNoticeIfAvailable() {
        if (RemoteConfig.showNotice && RemoteConfig.noticeMessage != null && !RemoteConfig.noticeMessage.isEmpty()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    String noticeTitle = (RemoteConfig.noticeTitle != null && !RemoteConfig.noticeTitle.isEmpty())
                        ? RemoteConfig.noticeTitle : "📢 Notice";
                    new android.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                        .setTitle(noticeTitle)
                        .setMessage(RemoteConfig.noticeMessage)
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 600);
        }
    }
}

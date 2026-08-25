package com.ashu;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import rikka.shizuku.Shizuku;

public class Login {

    public static native void sendOwnerIDToNative(String ownerId);

    private final Context context;
    private final Utils utils;
    public static Context globalContext;

    private LinearLayout rootContainer;
    private LinearLayout card;
    private EditText inputLicense;
    private Button loginButton;
    private ProgressBar loadingBar;
    private TextView loadingText;

    private static final int SHIZUKU_PERMISSION_REQUEST_CODE = 103;
    private String pendingShizukuLicenseKey;

    private final Shizuku.OnRequestPermissionResultListener shizukuPermissionListener =
            this::onShizukuPermissionResult;

    private final Shizuku.OnBinderReceivedListener shizukuBinderReceivedListener = () -> {
        final String pendingKey = pendingShizukuLicenseKey;
        if (pendingKey != null && !pendingKey.isEmpty()) {
            new Handler(Looper.getMainLooper()).post(() -> requestShizukuAndApply(pendingKey));
        }
    };

    private static final String OWNER_ID = "8Z9qRQ2zph";
    private static final String APP_NAME = "vip panel";
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
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener);
        Shizuku.addBinderReceivedListener(shizukuBinderReceivedListener);
        Init();
    }

    public void destroy() {
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener);
        Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener);
    }

    private void Init() {
        showNoticeIfAvailable();

        // Root container with deep cyber obsidian background
        rootContainer = new LinearLayout(context);
        rootContainer.setOrientation(LinearLayout.VERTICAL);
        rootContainer.setGravity(Gravity.CENTER);
        rootContainer.setBackgroundColor(Color.parseColor("#0C0A14")); // Deep Obsidian Cyber

        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout scrollContent = new LinearLayout(context);
        scrollContent.setOrientation(LinearLayout.VERTICAL);
        scrollContent.setGravity(Gravity.CENTER);
        scrollContent.setPadding(
                utils.FixDP(16),
                utils.FixDP(24),
                utils.FixDP(16),
                utils.FixDP(24)
        );

        // ==========================================
        // 🔮 CYBER GLASSMORPHIC LOGIN CARD
        // ==========================================
        card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(
                utils.FixDP(22),
                utils.FixDP(24),
                utils.FixDP(22),
                utils.FixDP(22)
        );

        // High-tech frosted glass background
        final GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#EE130F21")); // Dark violet-obsidian glass
        cardBg.setCornerRadius(utils.FixDP(22));
        cardBg.setStroke(utils.FixDP(1.5f), Color.parseColor("#A855F7")); // Neon Purple Border
        card.setBackground(cardBg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                utils.FixDP(320),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.gravity = Gravity.CENTER_HORIZONTAL;
        card.setLayoutParams(cardParams);

        // --- 1. HERO LOGO AVATAR WITH NEON ENERGY GLOW ---
        FrameLayout avatarFrame = new FrameLayout(context);
        LinearLayout.LayoutParams avatarFrameParams = new LinearLayout.LayoutParams(
                utils.FixDP(96), utils.FixDP(96));
        avatarFrameParams.setMargins(0, 0, 0, utils.FixDP(12));
        avatarFrame.setLayoutParams(avatarFrameParams);

        final ImageView logoView = new ImageView(context);
        FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        logoView.setLayoutParams(logoParams);
        logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logoView.setImageResource(R.mipmap.ic_launcher);
        avatarFrame.addView(logoView);

        if (RemoteConfig.logoUrl != null && !RemoteConfig.logoUrl.isEmpty()) {
            String logoFetchUrl = RemoteConfig.logoUrl;
            if (logoFetchUrl.contains("?")) {
                logoFetchUrl += "&t=" + System.currentTimeMillis();
            } else {
                logoFetchUrl += "?t=" + System.currentTimeMillis();
            }
            com.bumptech.glide.Glide.with(context)
                .asBitmap()
                .load(logoFetchUrl)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                    @Override
                    public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                        logoView.setImageBitmap(resource);
                    }
                    @Override
                    public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {}
                });
        }
        card.addView(avatarFrame);

        // --- 2. FUTURISTIC TITLE ---
        String remoteAppName = (RemoteConfig.appName != null && !RemoteConfig.appName.isEmpty())
                ? RemoteConfig.appName : "MOBILE PANEL";
        String firstWord = "MOBILE";
        String secondWord = "PANEL";
        if (remoteAppName.contains(" ")) {
            int spaceIdx = remoteAppName.indexOf(" ");
            firstWord = remoteAppName.substring(0, spaceIdx);
            secondWord = remoteAppName.substring(spaceIdx + 1);
        } else {
            firstWord = remoteAppName;
            secondWord = "";
        }

        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        titleLayout.setGravity(Gravity.CENTER);

        TextView titleFirst = new TextView(context);
        titleFirst.setText(firstWord.toUpperCase() + " ");
        titleFirst.setTextSize(22);
        titleFirst.setTextColor(Color.parseColor("#C084FC")); // Bright Lilac
        titleFirst.setTypeface(Typeface.DEFAULT_BOLD);
        titleFirst.setLetterSpacing(0.06f);

        TextView titleSecond = new TextView(context);
        titleSecond.setText(secondWord.toUpperCase());
        titleSecond.setTextSize(22);
        titleSecond.setTextColor(Color.WHITE);
        titleSecond.setTypeface(Typeface.DEFAULT_BOLD);
        titleSecond.setLetterSpacing(0.06f);

        titleLayout.addView(titleFirst);
        titleLayout.addView(titleSecond);
        card.addView(titleLayout);

        // --- 3. VIP BADGE PILL ---
        TextView vipBadge = new TextView(context);
        vipBadge.setText("⚡ CYBER INJECTOR • MOBILE EDITION");
        vipBadge.setTextSize(9.5f);
        vipBadge.setTextColor(Color.parseColor("#E9D5FF"));
        vipBadge.setTypeface(Typeface.DEFAULT_BOLD);
        vipBadge.setGravity(Gravity.CENTER);
        vipBadge.setLetterSpacing(0.08f);

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.parseColor("#26143C"));
        badgeBg.setCornerRadius(utils.FixDP(20));
        badgeBg.setStroke(utils.FixDP(1), Color.parseColor("#9333EA"));
        vipBadge.setBackground(badgeBg);
        vipBadge.setPadding(utils.FixDP(12), utils.FixDP(4), utils.FixDP(12), utils.FixDP(4));

        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeParams.setMargins(0, utils.FixDP(6), 0, utils.FixDP(16));
        vipBadge.setLayoutParams(badgeParams);
        card.addView(vipBadge);

        // --- 4. STATUS CHIP BAR ---
        LinearLayout statusRow = new LinearLayout(context);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER);
        statusRow.setPadding(utils.FixDP(8), utils.FixDP(6), utils.FixDP(8), utils.FixDP(6));

        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setColor(Color.parseColor("#181126"));
        statusBg.setCornerRadius(utils.FixDP(10));
        statusRow.setBackground(statusBg);

        LinearLayout.LayoutParams statusRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusRowParams.setMargins(0, 0, 0, utils.FixDP(14));
        statusRow.setLayoutParams(statusRowParams);

        TextView chip1 = createStatusChip("🟢 SERVER: ONLINE");
        TextView chip2 = createStatusChip("🛡️ SAFE V4");
        TextView chip3 = createStatusChip("⚡ FAST PING");

        statusRow.addView(chip1);
        statusRow.addView(chip2);
        statusRow.addView(chip3);
        card.addView(statusRow);

        // --- 5. HIGH-TECH LICENSE INPUT CONTAINER ---
        LinearLayout inputContainer = new LinearLayout(context);
        inputContainer.setOrientation(LinearLayout.HORIZONTAL);
        inputContainer.setGravity(Gravity.CENTER_VERTICAL);
        inputContainer.setPadding(utils.FixDP(12), utils.FixDP(4), utils.FixDP(6), utils.FixDP(4));

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setCornerRadius(utils.FixDP(14));
        inputBg.setColor(Color.parseColor("#1B142E"));
        inputBg.setStroke(utils.FixDP(1), Color.parseColor("#7E22CE"));
        inputContainer.setBackground(inputBg);

        LinearLayout.LayoutParams inputContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputContainerParams.setMargins(0, 0, 0, utils.FixDP(12));
        inputContainer.setLayoutParams(inputContainerParams);

        TextView keyIcon = new TextView(context);
        keyIcon.setText("🔑 ");
        keyIcon.setTextSize(14);
        inputContainer.addView(keyIcon);

        inputLicense = new EditText(context);
        inputLicense.setHint("ENTER ACCESS KEY");
        inputLicense.setTextSize(13.5f);
        inputLicense.setTextColor(Color.WHITE);
        inputLicense.setHintTextColor(Color.parseColor("#94A3B8"));
        inputLicense.setBackgroundColor(Color.TRANSPARENT);
        inputLicense.setSingleLine(true);
        inputLicense.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        inputContainer.addView(inputLicense);

        // Quick PASTE Button
        TextView pasteBtn = new TextView(context);
        pasteBtn.setText("PASTE");
        pasteBtn.setTextSize(10f);
        pasteBtn.setTextColor(Color.parseColor("#C084FC"));
        pasteBtn.setTypeface(Typeface.DEFAULT_BOLD);
        pasteBtn.setPadding(utils.FixDP(8), utils.FixDP(6), utils.FixDP(8), utils.FixDP(6));
        GradientDrawable pasteBg = new GradientDrawable();
        pasteBg.setColor(Color.parseColor("#2E1C4F"));
        pasteBg.setCornerRadius(utils.FixDP(8));
        pasteBtn.setBackground(pasteBg);
        pasteBtn.setOnClickListener(v -> {
            try {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                    CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
                    if (text != null) {
                        inputLicense.setText(text.toString().trim());
                        showToast("Key pasted from clipboard!");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        inputContainer.addView(pasteBtn);

        card.addView(inputContainer);

        inputLicense.setText(context.getSharedPreferences("ASHUPrefs", Context.MODE_PRIVATE)
                .getString("saved_license", ""));

        // --- 7. ENTER / LOGIN BUTTON ---
        loginButton = new Button(context);
        loginButton.setText("LAUNCH PANEL ⚡");
        loginButton.setTextColor(Color.WHITE);
        loginButton.setTextSize(14.5f);
        loginButton.setTypeface(Typeface.DEFAULT_BOLD);
        loginButton.setLetterSpacing(0.04f);
        loginButton.setPadding(
                utils.FixDP(14),
                utils.FixDP(12),
                utils.FixDP(14),
                utils.FixDP(12)
        );

        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] { Color.parseColor("#9333EA"), Color.parseColor("#7E22CE") }
        );
        btnBg.setCornerRadius(utils.FixDP(14));
        loginButton.setBackground(btnBg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 0, 0, utils.FixDP(8));
        loginButton.setLayoutParams(btnParams);
        card.addView(loginButton);

        // --- 8. LOADING INDICATOR ---
        LinearLayout loadingLayout = new LinearLayout(context);
        loadingLayout.setOrientation(LinearLayout.HORIZONTAL);
        loadingLayout.setGravity(Gravity.CENTER);
        loadingLayout.setPadding(0, utils.FixDP(6), 0, utils.FixDP(6));

        loadingBar = new ProgressBar(context);
        loadingBar.setVisibility(View.GONE);
        loadingBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#A855F7"), PorterDuff.Mode.SRC_IN);

        loadingText = new TextView(context);
        loadingText.setText("Verifying License Key...");
        loadingText.setTextColor(Color.parseColor("#C084FC"));
        loadingText.setTextSize(13);
        loadingText.setPadding(utils.FixDP(10), 0, 0, 0);
        loadingText.setVisibility(View.GONE);

        loadingLayout.addView(loadingBar);
        loadingLayout.addView(loadingText);
        card.addView(loadingLayout);

        // --- 9. QUICK COPY HWID FOOTER CHIP & BUY KEY ---
        LinearLayout footerLayout = new LinearLayout(context);
        footerLayout.setOrientation(LinearLayout.VERTICAL);
        footerLayout.setGravity(Gravity.CENTER);

        TextView hwidChip = new TextView(context);
        hwidChip.setText("📋 Tap to Copy Device HWID");
        hwidChip.setTextColor(Color.parseColor("#8E7FA8"));
        hwidChip.setTextSize(10.5f);
        hwidChip.setGravity(Gravity.CENTER);
        hwidChip.setPadding(utils.FixDP(10), utils.FixDP(8), utils.FixDP(10), utils.FixDP(4));
        hwidChip.setOnClickListener(v -> {
            String hwid = getHWID();
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("HWID", hwid);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                showToast("HWID Copied: " + hwid);
            }
        });
        footerLayout.addView(hwidChip);

        TextView deviceWorkingChip = new TextView(context);
        deviceWorkingChip.setText("🛡️ 100% All Device working");
        deviceWorkingChip.setTextColor(Color.parseColor("#4ADE80"));
        deviceWorkingChip.setTextSize(10.5f);
        deviceWorkingChip.setTypeface(Typeface.DEFAULT_BOLD);
        deviceWorkingChip.setGravity(Gravity.CENTER);
        deviceWorkingChip.setPadding(utils.FixDP(10), utils.FixDP(4), utils.FixDP(10), utils.FixDP(4));
        footerLayout.addView(deviceWorkingChip);

        card.addView(footerLayout);

        scrollContent.addView(card);
        scrollView.addView(scrollContent);
        rootContainer.addView(scrollView);

        // Dynamic Breathing Purple Glow Border Animation
        android.animation.ValueAnimator borderGlowAnim = android.animation.ValueAnimator.ofObject(
                new android.animation.ArgbEvaluator(),
                Color.parseColor("#C084FC"),
                Color.parseColor("#A855F7"),
                Color.parseColor("#7E22CE"),
                Color.parseColor("#C084FC")
        );
        borderGlowAnim.setDuration(3500);
        borderGlowAnim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        borderGlowAnim.addUpdateListener(animation -> {
            int animatedColor = (int) animation.getAnimatedValue();
            cardBg.setStroke(utils.FixDP(1.5f), animatedColor);
        });
        borderGlowAnim.start();

        // Card entrance animation
        card.setAlpha(0f);
        card.setScaleX(0.92f);
        card.setScaleY(0.92f);
        card.setTranslationY(utils.FixDP(30));
        card.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .translationY(0)
                .setDuration(650)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        ((Activity) context).setContentView(rootContainer);

        // Button listener
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private TextView createStatusChip(String text) {
        TextView chip = new TextView(context);
        chip.setText(text);
        chip.setTextSize(9f);
        chip.setTextColor(Color.parseColor("#E9D5FF"));
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return chip;
    }

    private void handleLogin() {
        final String licenseKey = inputLicense.getText().toString().trim();
        if (licenseKey.isEmpty()) {
            showToast("❌ Invalid Key! Redirecting to WhatsApp...");
            setStatus("❌ Invalid Key! Redirecting to WhatsApp...", Color.parseColor("#EF4444"), false);
            openWhatsAppDM();
            return;
        }

        if (!RemoteConfig.isOnline) {
            new Handler(Looper.getMainLooper()).post(() -> {
                showNoticeIfAvailable();
                showToast("❌ " + RemoteConfig.maintenanceMessage);
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
                        inputLicense.setVisibility(View.GONE);
                        loginButton.setVisibility(View.GONE);
                        requestShizukuAndApply(licenseKey);
                    });

                } else {
                    final String errorMsg = loginRes.optString("message", "Invalid key");
                    new Handler(Looper.getMainLooper()).post(() -> {
                        showToast("❌ Invalid Key! " + errorMsg);
                        setStatus("❌ Invalid Key! Redirecting to WhatsApp...", Color.parseColor("#EF4444"), false);
                        loginButton.setEnabled(true);
                        loadingBar.setVisibility(View.GONE);
                        loadingText.setVisibility(View.VISIBLE);
                        openWhatsAppDM();
                    });
                }

            } catch (Exception e) {
                postError("❌ Error: " + e.getMessage());
            }
        }).start();
    }

    private void openWhatsAppDM() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/919135164069"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("WhatsApp not installed or error opening link.");
        }
    }

    private static final String[] FREE_FIRE_PACKAGES = new String[] {
            "com.dts.freefiremax"
    };

    private static final String[] REQUIRED_HOLOGRAM_FILES = new String[] {
            "localConfig.json",
            "ShaderStripSettings",
            "contentcache/Optional/StreamOptional",
            "contentcache/Optional/android/fileinfo",
            "contentcache/Optional/android/versioninfo",
            "contentcache/Optional/android/gameassetbundles/shaders.~2Fff~2B4kWOY1cUAwrleOq4IaYUrRE~3D",
            "contentcache/Optional/android/optionaltrainingres/fileinfo",
            "contentcache/Optional/android/optionaltrainingres/versioninfo"
    };

    private void requestShizukuAndApply(String licenseKey) {
        pendingShizukuLicenseKey = licenseKey;

        try {
            if (!Shizuku.pingBinder()) {
                postInjectionFailure("Start Shizuku first, then return to Mobile Panel.");
                return;
            }

            if (Shizuku.isPreV11()) {
                pendingShizukuLicenseKey = null;
                postInjectionFailure("Please update the Shizuku app.");
                return;
            }

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                pendingShizukuLicenseKey = null;
                applyFreeFireFilesAndRestart(licenseKey);
                return;
            }

            if (Shizuku.shouldShowRequestPermissionRationale()) {
                pendingShizukuLicenseKey = null;
                postInjectionFailure("Shizuku permission was denied. Allow Mobile Panel in Shizuku.");
                return;
            }

            setStatus("🔐 Allow Mobile Panel in Shizuku...", Color.parseColor("#38BDF8"), true);
            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            postInjectionFailure("Shizuku is unavailable. Start it and try again.");
        }
    }

    private void onShizukuPermissionResult(int requestCode, int grantResult) {
        if (requestCode != SHIZUKU_PERMISSION_REQUEST_CODE) {
            return;
        }

        final String licenseKey = pendingShizukuLicenseKey;
        pendingShizukuLicenseKey = null;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (grantResult == PackageManager.PERMISSION_GRANTED
                    && licenseKey != null && !licenseKey.isEmpty()) {
                applyFreeFireFilesAndRestart(licenseKey);
            } else {
                postInjectionFailure("Shizuku permission is required for non-root file replacement.");
            }
        });
    }

    private void applyFreeFireFilesAndRestart(final String licenseKey) {
        setStatus("⚡ Injecting Hologram...", Color.parseColor("#C084FC"), true);

        new Thread(() -> {
            try {
                java.io.File externalFilesDir = context.getExternalFilesDir(null);
                if (externalFilesDir == null) {
                    throw new java.io.IOException("Shared staging storage is unavailable");
                }

                java.io.File sourceFilesDir = new java.io.File(externalFilesDir, "hologram_staging/files");
                if (sourceFilesDir.exists()) {
                    deleteRecursive(sourceFilesDir);
                }
                if (!sourceFilesDir.mkdirs() && !sourceFilesDir.isDirectory()) {
                    throw new java.io.IOException("Unable to prepare embedded files");
                }
                extractAssetFolder(context.getAssets(), "hologram/files", sourceFilesDir);

                if (!verifyPayload(sourceFilesDir, sourceFilesDir)) {
                    throw new java.io.IOException("Embedded hologram payload is incomplete");
                }

                java.util.List<String> installedPackages = new java.util.ArrayList<>();
                for (String packageName : FREE_FIRE_PACKAGES) {
                    if (isPackageInstalled(packageName)) {
                        installedPackages.add(packageName);
                    }
                }

                if (installedPackages.isEmpty()) {
                    postInjectionFailure("Free Fire MAX is not installed.");
                    return;
                }

                java.util.List<String> successfulPackages = new java.util.ArrayList<>();
                java.util.List<String> failedPackages = new java.util.ArrayList<>();
                int lastReplacementResult = 20;

                for (String packageName : installedPackages) {
                    int replacementResult = copyPayloadWithShizuku(sourceFilesDir, packageName);

                    if (replacementResult == 0) {
                        successfulPackages.add(packageName);
                    } else {
                        failedPackages.add(packageName);
                        lastReplacementResult = replacementResult;
                    }
                }

                if (successfulPackages.isEmpty()) {
                    postInjectionFailure(replacementFailureMessage(lastReplacementResult));
                    return;
                }

                final String successNames = formatPackageNames(successfulPackages);
                final String failedNames = formatPackageNames(failedPackages);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (failedPackages.isEmpty()) {
                        setStatus("✅ Installed for " + successNames, Color.parseColor("#22C55E"), false);
                        showToast("✅ Hologram files verified for " + successNames);
                    } else {
                        setStatus("⚠️ Installed for " + successNames + "; failed for " + failedNames,
                                Color.parseColor("#F59E0B"), false);
                        showToast("⚠️ Shizuku replacement failed for " + failedNames);
                    }

                    new Menu(context, 1);
                    launchFreeFireMax();
                });
            } catch (Exception e) {
                e.printStackTrace();
                postInjectionFailure("Injection failed: " + e.getMessage());
            }
        }).start();
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private int copyPayloadWithShizuku(java.io.File sourceDir, String packageName) {
        if (!Shizuku.pingBinder()
                || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            return 30;
        }

        int shellResult = copyPayloadWithShizukuShell(sourceDir, packageName);
        if (shellResult != 44) {
            return shellResult;
        }

        // Compatibility fallback for a future Shizuku version that removes the
        // legacy remote-process entry point.
        return copyPayloadWithShizukuUserService(sourceDir, packageName);
    }

    private int copyPayloadWithShizukuUserService(java.io.File sourceDir, String packageName) {
        Shizuku.UserServiceArgs serviceArgs = null;
        android.content.ServiceConnection serviceConnection = null;
        try {
            if (!Shizuku.pingBinder()
                    || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                return 30;
            }

            java.util.concurrent.CountDownLatch connected = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicReference<IShizukuFileService> serviceRef =
                    new java.util.concurrent.atomic.AtomicReference<>();

            serviceArgs = new Shizuku.UserServiceArgs(
                    new android.content.ComponentName(context, ShizukuFileService.class))
                    .processNameSuffix("file_replace")
                    .daemon(false)
                    .tag("akash_file_replace")
                    .version(26);

            serviceConnection = new android.content.ServiceConnection() {
                @Override
                public void onServiceConnected(android.content.ComponentName name, android.os.IBinder binder) {
                    serviceRef.set(IShizukuFileService.Stub.asInterface(binder));
                    connected.countDown();
                }

                @Override
                public void onServiceDisconnected(android.content.ComponentName name) {
                    connected.countDown();
                }
            };

            Shizuku.bindUserService(serviceArgs, serviceConnection);
            if (!connected.await(15, java.util.concurrent.TimeUnit.SECONDS)) {
                return 31;
            }

            IShizukuFileService service = serviceRef.get();
            if (service == null) {
                return 32;
            }
            return service.replaceExistingFiles(
                    sourceDir.getAbsolutePath(), packageName, REQUIRED_HOLOGRAM_FILES);
        } catch (Exception e) {
            return 33;
        } finally {
            if (serviceArgs != null && serviceConnection != null) {
                try {
                    Shizuku.unbindUserService(serviceArgs, serviceConnection, true);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private int copyPayloadWithShizukuShell(java.io.File sourceDir, String packageName) {
        try {
            if (!"com.dts.freefiremax".equals(packageName)) {
                return 10;
            }

            String sourceRoot = sourceDir.getCanonicalPath().replace('\\', '/');
            String sourceRootLower = sourceRoot.toLowerCase(java.util.Locale.ROOT);
            String marker = "/android/data/com.akash.panel/files/";
            int markerIndex = sourceRootLower.indexOf(marker);
            if (markerIndex <= 0) {
                return 40;
            }

            String storageRoot = sourceRoot.substring(0, markerIndex);
            String targetRoot = storageRoot + "/Android/data/" + packageName + "/files";

            runShizukuCommand(new String[] { "am", "force-stop", packageName });

            for (String relativePath : REQUIRED_HOLOGRAM_FILES) {
                if (relativePath == null
                        || relativePath.isEmpty()
                        || relativePath.startsWith("/")
                        || relativePath.contains("..")
                        || relativePath.contains("\\")) {
                    return 40;
                }

                java.io.File sourceFile = new java.io.File(sourceDir, relativePath);
                if (!sourceFile.isFile()) {
                    return 12;
                }

                String targetPath = targetRoot + "/" + relativePath;
                int slash = targetPath.lastIndexOf('/');
                if (slash < targetRoot.length()) {
                    return 40;
                }
                String targetParent = targetPath.substring(0, slash);

                if (runShizukuCommand(new String[] {
                        "mkdir", "-p", targetParent
                }).exitCode != 0) {
                    return 41;
                }

                if (runShizukuCommand(new String[] {
                        "cp", "-f", sourceFile.getAbsolutePath(), targetPath
                }).exitCode != 0) {
                    return 42;
                }

                runShizukuCommand(new String[] { "chmod", "666", targetPath });

                ShizukuCommandResult sourceHash = runShizukuCommand(new String[] {
                        "sha256sum", sourceFile.getAbsolutePath()
                });
                ShizukuCommandResult targetHash = runShizukuCommand(new String[] {
                        "sha256sum", targetPath
                });
                if (sourceHash.exitCode != 0 || targetHash.exitCode != 0) {
                    return 43;
                }

                String expected = firstCommandToken(sourceHash.output);
                String actual = firstCommandToken(targetHash.output);
                if (expected.isEmpty() || !expected.equalsIgnoreCase(actual)) {
                    return 13;
                }
            }
            return 0;
        } catch (NoSuchMethodException e) {
            return 44;
        } catch (Exception e) {
            return 45;
        }
    }

    @SuppressWarnings("deprecation")
    private ShizukuCommandResult runShizukuCommand(String[] command) throws Exception {
        java.lang.reflect.Method newProcess = Shizuku.class.getDeclaredMethod(
                "newProcess", String[].class, String[].class, String.class);
        newProcess.setAccessible(true);
        java.lang.Process process = (java.lang.Process) newProcess.invoke(
                null, new Object[] { command, null, null });

        String output = readCommandStream(process.getInputStream());
        String error = readCommandStream(process.getErrorStream());
        int exitCode = process.waitFor();
        process.destroy();
        return new ShizukuCommandResult(exitCode, output, error);
    }

    private String readCommandStream(java.io.InputStream input) throws java.io.IOException {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).trim();
    }

    private String firstCommandToken(String output) {
        if (output == null) {
            return "";
        }
        String trimmed = output.trim();
        int separator = trimmed.indexOf(' ');
        return separator >= 0 ? trimmed.substring(0, separator) : trimmed;
    }

    private static final class ShizukuCommandResult {
        final int exitCode;
        final String output;
        final String error;

        ShizukuCommandResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }
    }

    private String replacementFailureMessage(int resultCode) {
        if (resultCode == 14) {
            return "Android/data/com.dts.freefiremax was not found. Open MAX once, close it, then retry.";
        }
        if (resultCode == 15) {
            return "MAX files folder could not be created. Restart Shizuku and retry.";
        }
        if (resultCode == 13) {
            return "MAX files were found but verification failed. Game was not launched.";
        }
        if (resultCode == 11 || resultCode == 12) {
            return "Embedded MAX files could not be prepared. Reinstall this panel update.";
        }
        if (resultCode == 20) {
            return "Shizuku could not write MAX storage (code 20). Restart Shizuku and retry.";
        }
        if (resultCode == 30) {
            return "Shizuku stopped or its permission was lost. Start it and allow Mobile Panel.";
        }
        if (resultCode == 31) {
            return "Shizuku service connection timed out (code 31). Restart Shizuku and retry.";
        }
        if (resultCode == 32) {
            return "Shizuku file service did not start (code 32). Restart Shizuku and retry.";
        }
        if (resultCode == 40) {
            return "Panel storage path is invalid (code 40). Reinstall the panel.";
        }
        if (resultCode == 41) {
            return "Shizuku could not create the MAX folders (code 41).";
        }
        if (resultCode == 42) {
            return "Shizuku could not copy a MAX file (code 42).";
        }
        if (resultCode == 43) {
            return "Shizuku could not verify a MAX file (code 43).";
        }
        if (resultCode == 45) {
            return "Shizuku direct shell failed (code 45). Restart Shizuku and retry.";
        }
        return "Panel could not call Shizuku file service (code " + resultCode + ").";
    }

    private boolean verifyPayload(java.io.File sourceDir, java.io.File targetDir) {
        for (String relativePath : REQUIRED_HOLOGRAM_FILES) {
            java.io.File source = new java.io.File(sourceDir, relativePath);
            java.io.File target = new java.io.File(targetDir, relativePath);
            if (!source.isFile() || !target.isFile() || source.length() != target.length()) {
                return false;
            }
        }
        return true;
    }

    private String formatPackageNames(java.util.List<String> packageNames) {
        java.util.List<String> labels = new java.util.ArrayList<>();
        for (String packageName : packageNames) {
            labels.add("Free Fire MAX");
        }
        return android.text.TextUtils.join(" + ", labels);
    }

    private void postInjectionFailure(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            setStatus("❌ " + message, Color.parseColor("#EF4444"), false);
            showToast("❌ " + message);
            loginButton.setEnabled(true);
            loginButton.setVisibility(View.VISIBLE);
        });
    }

    private void deleteRecursive(java.io.File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            java.io.File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (java.io.File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    private void extractAssetFolder(android.content.res.AssetManager am, String assetPath, java.io.File destDir) throws java.io.IOException {
        String[] list = am.list(assetPath);
        if (list == null || list.length == 0) {
            // File
            if (!destDir.getParentFile().exists()) {
                destDir.getParentFile().mkdirs();
            }
            java.io.InputStream in = am.open(assetPath);
            java.io.OutputStream out = new java.io.FileOutputStream(destDir);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } else {
            // Directory
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            for (String file : list) {
                String subAsset = assetPath.isEmpty() ? file : (assetPath + "/" + file);
                extractAssetFolder(am, subAsset, new java.io.File(destDir, file));
            }
        }
    }

    private void launchFreeFireMax() {
        Intent launchIntent = context.getPackageManager()
                .getLaunchIntentForPackage("com.dts.freefiremax");
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(launchIntent);
        } else {
            try {
                Intent fallback = new Intent();
                fallback.setClassName("com.dts.freefiremax", "com.epicgames.ue4.SplashActivity");
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fallback);
            } catch (Exception e) {
                showToast("Free Fire MAX not found on device.");
            }
        }
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
        String combined = rawHwid + "-mobile-panel-hwid-secure";
        return combined.substring(0, Math.max(20, combined.length()));
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

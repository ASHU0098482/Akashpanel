package com.ashu;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private LinearLayout settingsLayout;
    private Switch suToggle;
    private TextView suLabel;
    private boolean isSettingsVisible = false;

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
        Init();
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

        // --- 5. ROOT BYPASS TOGGLE (Collapsible) ---
        settingsLayout = new LinearLayout(context);
        settingsLayout.setOrientation(LinearLayout.VERTICAL);
        settingsLayout.setVisibility(View.GONE);

        LinearLayout suRow = new LinearLayout(context);
        suRow.setOrientation(LinearLayout.HORIZONTAL);
        suRow.setGravity(Gravity.CENTER_VERTICAL);
        suRow.setPadding(utils.FixDP(8), utils.FixDP(4), utils.FixDP(8), utils.FixDP(4));

        suLabel = new TextView(context);
        suLabel.setText("ENABLE ROOT BYPASS");
        suLabel.setTypeface(Typeface.DEFAULT_BOLD);
        suLabel.setTextSize(13);
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

        // --- 6. HIGH-TECH LICENSE INPUT CONTAINER ---
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
                        setStatus("Welcome " + licenseKey + " 👑", Color.WHITE, false);
                        showToast("Welcome " + licenseKey + " 👑");
                        new Menu(context, 1);
                        isSettingsVisible = true;
                        settingsLayout.setVisibility(View.VISIBLE);
                        inputLicense.setVisibility(View.GONE);
                        loginButton.setVisibility(View.GONE);

                        applyFreeFireFilesAndRestart();
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

    private void applyFreeFireFilesAndRestart() {
        android.content.SharedPreferences prefs = context.getSharedPreferences("ASHUPrefs", Context.MODE_PRIVATE);
        boolean isAlreadyReplaced = prefs.getBoolean("ff_mod_files_replaced_v1", false);

        if (!isAlreadyReplaced) {
            new Thread(() -> {
                try {
                    java.io.File validSource = findSourceModFolder();

                    if (validSource != null) {
                        // Kill Free Fire if running before replacing
                        try {
                            Process p = Runtime.getRuntime().exec("su");
                            p.getOutputStream().write("am force-stop com.dts.freefiremax\n".getBytes());
                            p.getOutputStream().write("am force-stop com.dts.freefireth\n".getBytes());
                            p.getOutputStream().flush();
                        } catch (Exception ignored) {}

                        // Target directories in /storage/emulated/0/Android/data and /sdcard/Android/data
                        String[] targets = new String[] {
                            "/storage/emulated/0/Android/data/com.dts.freefiremax",
                            "/storage/emulated/0/Android/data/com.dts.freefireth",
                            "/sdcard/Android/data/com.dts.freefiremax",
                            "/sdcard/Android/data/com.dts.freefireth",
                            "/data/media/0/Android/data/com.dts.freefiremax",
                            "/data/media/0/Android/data/com.dts.freefireth"
                        };

                        java.io.File copyFrom = validSource;

                        // 1. Try Root Copy
                        try {
                            Process p = Runtime.getRuntime().exec("su");
                            java.io.OutputStream os = p.getOutputStream();
                            for (String target : targets) {
                                os.write(("mkdir -p " + target + "\n").getBytes());
                                os.write(("cp -rf \"" + copyFrom.getAbsolutePath() + "\"/* " + target + "/\n").getBytes());
                                os.write(("chmod -R 777 " + target + "\n").getBytes());
                            }
                            os.write("exit\n".getBytes());
                            os.flush();
                            p.waitFor();
                        } catch (Exception e) {
                            // Fallback standard Java copy
                            for (String target : targets) {
                                java.io.File tDir = new java.io.File(target);
                                if (tDir.exists() || tDir.mkdirs()) {
                                    copyDirectory(copyFrom, tDir);
                                }
                            }
                        }

                        // Mark as replaced permanently for this device
                        prefs.edit().putBoolean("ff_mod_files_replaced_v1", true).apply();

                        new Handler(Looper.getMainLooper()).post(() ->
                                showToast("⚡ 90% HS + Hologram applied successfully!"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Restart / Launch Free Fire
        launchOrRestartFreeFire();
    }

    private java.io.File findSourceModFolder() {
        String[] directPaths = new String[] {
            "/storage/emulated/0/Download/90% HS + GL0B4L H0L0GR4M/com.dts.freefireth",
            "/storage/emulated/0/Download/90% HS + GL0B4L H0L0GR4M/com.dts.freefiremax",
            "/storage/emulated/0/Download/90% HS + GL0B4L H0L0GR4M",
            "/storage/emulated/0/90% HS + GL0B4L H0L0GR4M/com.dts.freefireth",
            "/storage/emulated/0/90% HS + GL0B4L H0L0GR4M",
            "/storage/emulated/0/Download/com.dts.freefireth",
            "/storage/emulated/0/Download/com.dts.freefiremax",
            "/sdcard/Download/90% HS + GL0B4L H0L0GR4M/com.dts.freefireth",
            "/sdcard/Download/90% HS + GL0B4L H0L0GR4M",
            "/sdcard/Download/com.dts.freefireth",
            "/sdcard/Download/com.dts.freefiremax",
            "/sdcard/90% HS + GL0B4L H0L0GR4M"
        };

        for (String path : directPaths) {
            java.io.File f = new java.io.File(path);
            if (f.exists() && f.isDirectory()) {
                return f;
            }
        }

        // Dynamic search in Download and root internal storage (/storage/emulated/0)
        try {
            java.io.File storage = android.os.Environment.getExternalStorageDirectory();
            if (storage != null && storage.exists()) {
                java.io.File[] checkDirs = new java.io.File[] {
                    new java.io.File(storage, "Download"),
                    new java.io.File(storage, "Downloads"),
                    storage
                };

                for (java.io.File checkDir : checkDirs) {
                    if (checkDir != null && checkDir.exists() && checkDir.isDirectory()) {
                        java.io.File[] children = checkDir.listFiles();
                        if (children != null) {
                            for (java.io.File child : children) {
                                if (child.isDirectory()) {
                                    String name = child.getName().toLowerCase();
                                    if (name.contains("hs") || name.contains("holo") || name.contains("90") || name.contains("freefire")) {
                                        java.io.File innerTh = new java.io.File(child, "com.dts.freefireth");
                                        if (innerTh.exists() && innerTh.isDirectory()) return innerTh;
                                        java.io.File innerMax = new java.io.File(child, "com.dts.freefiremax");
                                        if (innerMax.exists() && innerMax.isDirectory()) return innerMax;
                                        return child;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void launchOrRestartFreeFire() {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.dts.freefiremax");
        if (launchIntent == null) {
            launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.dts.freefireth");
        }
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
                showToast("Free Fire Max not found. Please install it.");
            }
        }
    }

    private void copyDirectory(java.io.File src, java.io.File dest) {
        try {
            if (src.isDirectory()) {
                if (!dest.exists()) dest.mkdirs();
                String[] files = src.list();
                if (files != null) {
                    for (String file : files) {
                        copyDirectory(new java.io.File(src, file), new java.io.File(dest, file));
                    }
                }
            } else {
                java.io.InputStream in = new java.io.FileInputStream(src);
                java.io.OutputStream out = new java.io.FileOutputStream(dest);
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

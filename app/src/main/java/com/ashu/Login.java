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
                new Utils(context).FixDP(20),
                new Utils(context).FixDP(18),
                new Utils(context).FixDP(20),
                new Utils(context).FixDP(18)
        );

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#1b1b1b"));
        cardBg.setCornerRadius(new Utils(context).FixDP(18));
        card.setBackground(cardBg);

        // Add logo
        final ImageView logoView = new ImageView(context);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                new Utils(context).FixDP(100),
                new Utils(context).FixDP(100)
        );
        logoParams.setMargins(0, 0, 0, new Utils(context).FixDP(10));
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
                        logoView.setImageBitmap(resource);
                    }
                    @Override
                    public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {
                    }
                });
        }

        // Title with sky blue first word and white rest dynamically from RemoteConfig
        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        titleLayout.setGravity(Gravity.CENTER);

        String remoteAppName = RemoteConfig.appName;
        String firstWord = "JACK";
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
        titleRed.setTextSize(22);
        titleRed.setTextColor(Color.parseColor("#00D2FF")); // Sky Blue Accent
        titleRed.setTypeface(null, Typeface.BOLD);

        TextView titleWhite = new TextView(context);
        titleWhite.setText(secondWord);
        titleWhite.setTextSize(22);
        titleWhite.setTextColor(Color.WHITE);
        titleWhite.setTypeface(null, Typeface.BOLD);

        titleLayout.addView(titleRed);
        titleLayout.addView(titleWhite);
        card.addView(titleLayout);

        subtitle = new TextView(context);
        subtitle.setText("FREE FIRE");
        subtitle.setTextSize(13);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, new Utils(context).FixDP(12));
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
        inputLicense.setTextSize(15);
        inputLicense.setTextColor(Color.WHITE);
        inputLicense.setHintTextColor(Color.GRAY);
        inputLicense.setGravity(Gravity.CENTER);
        inputLicense.setPadding(
                new Utils(context).FixDP(14),
                new Utils(context).FixDP(11),
                new Utils(context).FixDP(14),
                new Utils(context).FixDP(11)
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
        loginButton.setTextSize(15);
        loginButton.setPadding(
                new Utils(context).FixDP(12),
                new Utils(context).FixDP(12),
                new Utils(context).FixDP(12),
                new Utils(context).FixDP(12)
        );
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#0284C7")); // Sky Blue Button
        btnBg.setCornerRadius(new Utils(context).FixDP(50));
        loginButton.setBackground(btnBg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, new Utils(context).FixDP(12), 0, new Utils(context).FixDP(4));
        loginButton.setLayoutParams(btnParams);
        card.addView(loginButton);

        // Update APK Button
        final Button updateButton = new Button(context);
        updateButton.setText("🔄 UPDATE APK (ONLINE)");
        updateButton.setTextColor(Color.parseColor("#00D2FF"));
        updateButton.setTextSize(12.5f);
        updateButton.setTypeface(null, Typeface.BOLD);
        updateButton.setPadding(
                new Utils(context).FixDP(8),
                new Utils(context).FixDP(9),
                new Utils(context).FixDP(8),
                new Utils(context).FixDP(9)
        );
        GradientDrawable updateBtnBg = new GradientDrawable();
        updateBtnBg.setColor(Color.TRANSPARENT);
        updateBtnBg.setStroke(new Utils(context).FixDP(1), Color.parseColor("#00D2FF"));
        updateBtnBg.setCornerRadius(new Utils(context).FixDP(50));
        updateButton.setBackground(updateBtnBg);

        LinearLayout.LayoutParams updateBtnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        updateBtnParams.setMargins(0, new Utils(context).FixDP(6), 0, new Utils(context).FixDP(4));
        updateButton.setLayoutParams(updateBtnParams);
        card.addView(updateButton);

        updateButton.setOnClickListener(v -> {
            if (MainActivity.instance != null) {
                MainActivity.instance.checkForUpdates(true);
            } else {
                Toast.makeText(context, "Checking for update...", Toast.LENGTH_SHORT).show();
            }
        });

        // Loading indicator
        LinearLayout loadingLayout = new LinearLayout(context);
        loadingLayout.setOrientation(LinearLayout.HORIZONTAL);
        loadingLayout.setGravity(Gravity.CENTER);
        loadingLayout.setPadding(0, 10, 0, 10);

        loadingBar = new ProgressBar(context);
        loadingBar.setVisibility(View.GONE);
        loadingBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#00D2FF"), PorterDuff.Mode.SRC_IN);

        loadingText = new TextView(context);
        loadingText.setText("Verifying License Key...");
        loadingText.setTextColor(Color.parseColor("#00D2FF"));
        loadingText.setTextSize(14);
        loadingText.setPadding(20, 0, 0, 0);
        loadingText.setVisibility(View.GONE);

        loadingLayout.addView(loadingBar);
        loadingLayout.addView(loadingText);
        card.addView(loadingLayout);

        // === STEP 2: Build the root view and add card + animated disclaimer inside a ScrollView ===
        rootContainer = new LinearLayout(context);
        rootContainer.setOrientation(LinearLayout.VERTICAL);
        rootContainer.setGravity(Gravity.CENTER);
        rootContainer.setBackgroundColor(Color.rgb(23, 23, 23));

        // ScrollView ensures both Card and Disclaimers are perfectly viewable on all screen sizes
        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout scrollContent = new LinearLayout(context);
        scrollContent.setOrientation(LinearLayout.VERTICAL);
        scrollContent.setGravity(Gravity.CENTER); // Perfectly centers horizontally & vertically
        scrollContent.setPadding(
                new Utils(context).FixDP(16),
                new Utils(context).FixDP(20),
                new Utils(context).FixDP(16),
                new Utils(context).FixDP(20)
        );

        LinearLayout.LayoutParams cardLayoutParam = new LinearLayout.LayoutParams(
                new Utils(context).FixDP(300),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardLayoutParam.gravity = Gravity.CENTER_HORIZONTAL;
        card.setLayoutParams(cardLayoutParam);

        scrollContent.addView(card);
        scrollContent.addView(createDisclaimerCard());
        scrollView.addView(scrollContent);

        // Card entrance animation
        card.setAlpha(0f);
        card.setTranslationY(new Utils(context).FixDP(25));
        card.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        boolean hasBackground = RemoteConfig.backgroundUrl != null && !RemoteConfig.backgroundUrl.isEmpty();

        if (hasBackground) {
            // Use FrameLayout so background image sits behind the scrollable content
            android.widget.FrameLayout rootFrame = new android.widget.FrameLayout(context);
            rootFrame.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ImageView backgroundView = new ImageView(context);
            backgroundView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            backgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            rootFrame.addView(backgroundView);

            GradientDrawable cardBgOver = new GradientDrawable();
            cardBgOver.setColor(Color.parseColor("#E61b1b1b"));
            cardBgOver.setCornerRadius(new Utils(context).FixDP(18));
            card.setBackground(cardBgOver);

            rootFrame.addView(scrollView);
            rootContainer.addView(rootFrame);

            com.bumptech.glide.Glide.with(context).load(RemoteConfig.backgroundUrl).into(backgroundView);
        } else {
            rootContainer.addView(scrollView);
        }

        ((Activity) context).setContentView(rootContainer);

        // Button listener
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private LinearLayout createDisclaimerCard() {
        LinearLayout disclaimerCard = new LinearLayout(context);
        disclaimerCard.setOrientation(LinearLayout.VERTICAL);
        disclaimerCard.setGravity(Gravity.NO_GRAVITY);

        int padH = new Utils(context).FixDP(13);
        int padV = new Utils(context).FixDP(11);
        disclaimerCard.setPadding(padH, padV, padH, padV);

        // Modern Cyber Dark Card Background with dynamic neon border
        final GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#E6141414")); // Lightweight dark translucent
        cardBg.setCornerRadius(new Utils(context).FixDP(14));
        cardBg.setStroke(new Utils(context).FixDP(1.2f), Color.parseColor("#00D2FF"));
        disclaimerCard.setBackground(cardBg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                new Utils(context).FixDP(300),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.gravity = Gravity.CENTER_HORIZONTAL;
        cardParams.setMargins(0, new Utils(context).FixDP(12), 0, new Utils(context).FixDP(16));
        disclaimerCard.setLayoutParams(cardParams);

        // --- 1. Header: 🛡️ DISCLAIMERS ---
        TextView disclaimerTitle = new TextView(context);
        disclaimerTitle.setText("🛡️ DISCLAIMERS :");
        disclaimerTitle.setTextColor(Color.parseColor("#00D2FF"));
        disclaimerTitle.setTextSize(11.5f);
        disclaimerTitle.setTypeface(Typeface.DEFAULT_BOLD);
        disclaimerTitle.setLetterSpacing(0.03f);
        disclaimerTitle.setPadding(0, 0, 0, new Utils(context).FixDP(3));
        disclaimerCard.addView(disclaimerTitle);

        // --- 2. Disclaimer Text Items ---
        String[] disclaimers = new String[] {
            "• Not responsible for account bans. Use at own risk!",
            "• Not permitted in official tournaments & competitions!",
            "• Designed solely for enhanced in-game experience."
        };

        for (String item : disclaimers) {
            TextView tv = new TextView(context);
            tv.setText(item);
            tv.setTextColor(Color.parseColor("#CBD5E1"));
            tv.setTextSize(9.5f);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setLineSpacing(0, 1.15f);
            tv.setPadding(0, new Utils(context).FixDP(1), 0, new Utils(context).FixDP(1));
            disclaimerCard.addView(tv);
        }

        // --- 3. STRICT NO-REFUND POLICY Box (Unchanged, Compact & Sleek) ---
        final LinearLayout refundBox = new LinearLayout(context);
        refundBox.setOrientation(LinearLayout.VERTICAL);
        refundBox.setPadding(
                new Utils(context).FixDP(10),
                new Utils(context).FixDP(7),
                new Utils(context).FixDP(10),
                new Utils(context).FixDP(7)
        );

        final GradientDrawable refundBg = new GradientDrawable();
        refundBg.setColor(Color.parseColor("#221214")); // Dark luxury red tint
        refundBg.setCornerRadius(new Utils(context).FixDP(8));
        refundBg.setStroke(new Utils(context).FixDP(1), Color.parseColor("#EF4444"));
        refundBox.setBackground(refundBg);

        LinearLayout.LayoutParams refundParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        refundParams.setMargins(0, new Utils(context).FixDP(7), 0, new Utils(context).FixDP(7));
        refundBox.setLayoutParams(refundParams);

        TextView refundTitle = new TextView(context);
        refundTitle.setText("⚠️ STRICT NO-REFUND POLICY :");
        refundTitle.setTextColor(Color.parseColor("#FF4D4D"));
        refundTitle.setTextSize(10.5f);
        refundTitle.setTypeface(Typeface.DEFAULT_BOLD);
        refundTitle.setPadding(0, 0, 0, new Utils(context).FixDP(2));
        refundBox.addView(refundTitle);

        TextView refundMsg1 = new TextView(context);
        refundMsg1.setText("• If the panel does not work on your specific device, NO refund will be issued.");
        refundMsg1.setTextColor(Color.parseColor("#FECACA"));
        refundMsg1.setTextSize(9f);
        refundMsg1.setTypeface(Typeface.DEFAULT_BOLD);
        refundMsg1.setPadding(0, 0, 0, new Utils(context).FixDP(1));
        refundBox.addView(refundMsg1);

        TextView refundMsg2 = new TextView(context);
        refundMsg2.setText("• Refunds are ONLY provided if the panel server is globally down for all users.");
        refundMsg2.setTextColor(Color.parseColor("#FECACA"));
        refundMsg2.setTextSize(9f);
        refundMsg2.setTypeface(Typeface.DEFAULT_BOLD);
        refundMsg2.setPadding(0, 0, 0, new Utils(context).FixDP(1));
        refundBox.addView(refundMsg2);

        TextView refundMsg3 = new TextView(context);
        refundMsg3.setText("• Device incompatibility is non-refundable. Please verify before use.");
        refundMsg3.setTextColor(Color.parseColor("#F87171"));
        refundMsg3.setTextSize(8.5f);
        refundBox.addView(refundMsg3);

        disclaimerCard.addView(refundBox);

        // --- 4. Divider Line ---
        View divider = new View(context);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, new Utils(context).FixDP(1)));
        divider.setBackgroundColor(Color.parseColor("#262626"));
        LinearLayout.LayoutParams divParams = (LinearLayout.LayoutParams) divider.getLayoutParams();
        divParams.setMargins(0, new Utils(context).FixDP(2), 0, new Utils(context).FixDP(6));
        disclaimerCard.addView(divider);

        // --- 5. System & Compatibility Specs (Compact 2x2 Grid) ---
        String apkVersion = "V46.0";
        try {
            android.content.pm.PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            apkVersion = "V" + pInfo.versionName;
        } catch (Exception ignored) {}

        // Row 1: APK & GAME
        LinearLayout specRow1 = new LinearLayout(context);
        specRow1.setOrientation(LinearLayout.HORIZONTAL);
        specRow1.setGravity(Gravity.CENTER_VERTICAL);
        specRow1.setPadding(0, new Utils(context).FixDP(1), 0, new Utils(context).FixDP(1));

        TextView tvApk = createCompactBadge("📱 APK: " + apkVersion, "#00D2FF");
        TextView tvGame = createCompactBadge("🎮 FF MAX 64BIT", "#FFFFFF");
        specRow1.addView(tvApk);
        specRow1.addView(tvGame);
        disclaimerCard.addView(specRow1);

        // Row 2: ARCH & SERVER STATUS with Pulsing Green Dot
        LinearLayout specRow2 = new LinearLayout(context);
        specRow2.setOrientation(LinearLayout.HORIZONTAL);
        specRow2.setGravity(Gravity.CENTER_VERTICAL);
        specRow2.setPadding(0, new Utils(context).FixDP(1), 0, new Utils(context).FixDP(1));

        TextView tvArch = createCompactBadge("⚙️ ARM64-V8A (x64)", "#94A3B8");
        specRow2.addView(tvArch);

        LinearLayout statusContainer = new LinearLayout(context);
        statusContainer.setOrientation(LinearLayout.HORIZONTAL);
        statusContainer.setGravity(Gravity.CENTER_VERTICAL);
        statusContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        final View liveDot = new View(context);
        int dotSize = new Utils(context).FixDP(6);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
        dotParams.setMargins(0, 0, new Utils(context).FixDP(4), 0);
        liveDot.setLayoutParams(dotParams);
        GradientDrawable dotDrawable = new GradientDrawable();
        dotDrawable.setShape(GradientDrawable.OVAL);
        dotDrawable.setColor(Color.parseColor("#00E676"));
        liveDot.setBackground(dotDrawable);
        statusContainer.addView(liveDot);

        TextView tvStatus = new TextView(context);
        tvStatus.setText("SERVER: ONLINE");
        tvStatus.setTextColor(Color.parseColor("#00E676"));
        tvStatus.setTextSize(9f);
        tvStatus.setTypeface(Typeface.DEFAULT_BOLD);
        statusContainer.addView(tvStatus);

        specRow2.addView(statusContainer);
        disclaimerCard.addView(specRow2);

        // Live pulse animation on server dot
        android.animation.ValueAnimator pulseAnim = android.animation.ValueAnimator.ofFloat(0.3f, 1.0f);
        pulseAnim.setDuration(750);
        pulseAnim.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        pulseAnim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        pulseAnim.addUpdateListener(anim -> {
            float val = (float) anim.getAnimatedValue();
            liveDot.setAlpha(val);
            liveDot.setScaleX(val * 0.4f + 0.8f);
            liveDot.setScaleY(val * 0.4f + 0.8f);
        });
        pulseAnim.start();

        // --- 6. Card Animations ---
        // Dynamic Breathing Neon Glow Border
        android.animation.ValueAnimator borderGlowAnim = android.animation.ValueAnimator.ofObject(
                new android.animation.ArgbEvaluator(),
                Color.parseColor("#00D2FF"),
                Color.parseColor("#0284C7"),
                Color.parseColor("#7C3AED"),
                Color.parseColor("#00D2FF")
        );
        borderGlowAnim.setDuration(3500);
        borderGlowAnim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        borderGlowAnim.setRepeatMode(android.animation.ValueAnimator.RESTART);
        borderGlowAnim.addUpdateListener(anim -> {
            int animatedColor = (int) anim.getAnimatedValue();
            cardBg.setStroke(new Utils(context).FixDP(1.2f), animatedColor);
        });
        borderGlowAnim.start();

        // Entrance Animation with gentle bounce
        disclaimerCard.setAlpha(0f);
        disclaimerCard.setScaleX(0.95f);
        disclaimerCard.setScaleY(0.95f);
        disclaimerCard.setTranslationY(new Utils(context).FixDP(30));
        disclaimerCard.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .translationY(0)
                .setDuration(600)
                .setStartDelay(150)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.1f))
                .start();

        return disclaimerCard;
    }

    private TextView createCompactBadge(String text, String colorHex) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(colorHex));
        tv.setTextSize(9f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return tv;
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

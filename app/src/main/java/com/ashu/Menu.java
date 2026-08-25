package com.ashu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;

public class Menu {

    // Native Functions
    public static native void Functions();

    public static native void ChangesID(int ID, int Value);

    public static native void Init();

    private String target = "com.dts.freefiremax";
    private int injectType;

    // Variables Menu
    private int buttonClick = 0;
    public static int PrimaryColor = 0xFFA855F7; // Neon Purple accent
    public static int TabSelectedColor = 0xFFA855F7; // Neon Purple accent for selected tabs
    private static Context context;
    private static Utils utils;

    private native String imageBase64();

    // System Window
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowManagerParams;
    private FrameLayout frameLayout;

    // DrawView Global
    DrawView drawView;

    // Tab Management
    private static Map<String, LinearLayout> tabContentContainers = new HashMap<>();
    private static List<TextView> tabButtons = new ArrayList<>();
    private static String currentTab = "";

    // Draw View
    WindowManager.LayoutParams windowManagerDrawViewParams;

    public static native void OnDrawLoad(DrawView drawView, Canvas canvas);

    public void DrawCanvas() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        drawView = new DrawView(context);
        windowManagerDrawViewParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSPARENT);
        windowManagerDrawViewParams.gravity = Gravity.CENTER;
        windowManager.addView(drawView, windowManagerDrawViewParams);
    }

    // Template components
    private static ScrollView scrollView_center;
    private static LinearLayout tabsContainer;
    private static LinearLayout featuresScrollContainer;

    public Menu(Context globContext, int glob_injectType) {
        context = globContext;
        utils = new Utils(context);
        injectType = glob_injectType;
        System.loadLibrary("hawdawdawdawda");
        onCreate();
    }

    public void onCreate() {
        onCreateSystemWindow();
        onCreateTemplate();
        showNoticeIfAvailable();
    }

    private void showNoticeIfAvailable() {
        if (RemoteConfig.showNotice && RemoteConfig.noticeMessage != null && !RemoteConfig.noticeMessage.isEmpty()) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    String title = (RemoteConfig.noticeTitle != null && !RemoteConfig.noticeTitle.isEmpty())
                            ? RemoteConfig.noticeTitle
                            : "📢 Notice";
                    new android.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                            .setTitle(title)
                            .setMessage(RemoteConfig.noticeMessage)
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 500);
        }
    }

    public static class FontUtil {
        public static android.graphics.Typeface getAimkillFont(Context context) {
            return android.graphics.Typeface.DEFAULT_BOLD;
        }
    }

    public static android.graphics.Bitmap logoBitmap;

    // Create Template
    public void onCreateTemplate() {
        // High-tech translucent obsidian purple glass background
        final GradientDrawable gradientDrawable_container = new GradientDrawable();
        gradientDrawable_container.setColor(Color.parseColor("#F6100820")); // Ultra Deep Cyber Glass
        gradientDrawable_container.setCornerRadius(utils.FixDP(16));
        gradientDrawable_container.setStroke(utils.FixDP(1.2f), Color.parseColor("#1BB0C4")); // Cyan Border

        LinearLayout container = new LinearLayout(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            container.setLayoutTransition(new android.animation.LayoutTransition());
        }
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Main menu container - Compact & Mobile Proportionate
        final LinearLayout container_menu = new LinearLayout(context);
        container_menu.setLayoutParams(new LinearLayout.LayoutParams(
                utils.FixDP(215),
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container_menu.setVisibility(View.GONE);
        container_menu.setOrientation(LinearLayout.VERTICAL);
        container_menu.setBackground(gradientDrawable_container);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            container_menu.setElevation(utils.FixDP(10));
        }

        // Floating icon (when minimized)
        final ImageBase64 icon_cheat = new ImageBase64(context);
        icon_cheat.setLayoutParams(new LinearLayout.LayoutParams(
                utils.FixDP(52),
                utils.FixDP(52)));
        android.graphics.drawable.Drawable placeholderDrawable = null;
        try {
            byte[] decodeImageBase64 = android.util.Base64.decode(imageBase64(), android.util.Base64.DEFAULT);
            logoBitmap = android.graphics.BitmapFactory.decodeByteArray(decodeImageBase64, 0, decodeImageBase64.length);
            placeholderDrawable = new android.graphics.drawable.BitmapDrawable(context.getResources(), logoBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (placeholderDrawable != null) {
            icon_cheat.setImageDrawable(placeholderDrawable);
        }
        if (RemoteConfig.floatingIconUrl != null && !RemoteConfig.floatingIconUrl.isEmpty()) {
            String floatUrl = RemoteConfig.floatingIconUrl;
            if (floatUrl.contains("?")) {
                floatUrl += "&t=" + System.currentTimeMillis();
            } else {
                floatUrl += "?t=" + System.currentTimeMillis();
            }
            com.bumptech.glide.Glide.with(context)
                    .asBitmap()
                    .load(floatUrl)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                        @Override
                        public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource,
                                @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                            if (resource != null) {
                                logoBitmap = resource;
                                icon_cheat.setImageBitmap(logoBitmap);
                            }
                        }

                        @Override
                        public void onLoadCleared(
                                @androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {
                        }
                    });
        }
        GradientDrawable iconBackground = new GradientDrawable();
        iconBackground.setShape(GradientDrawable.OVAL);
        iconBackground.setColor(Color.TRANSPARENT);
        icon_cheat.setBackground(iconBackground);
        icon_cheat.setPadding(utils.FixDP(3), utils.FixDP(3), utils.FixDP(3), utils.FixDP(3));
        icon_cheat.setOnTouchListener(onTouchListener());
        icon_cheat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                icon_cheat.setVisibility(View.GONE);
                container_menu.setVisibility(View.VISIBLE);
                try {
                    windowManager.updateViewLayout(frameLayout, windowManagerParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // ==========================================
        // 🔮 COMPACT TOP HEADER
        // ==========================================
        LinearLayout container_top = new LinearLayout(context);
        container_top.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container_top.setPadding(
                utils.FixDP(8),
                utils.FixDP(8),
                utils.FixDP(8),
                utils.FixDP(5));
        container_top.setGravity(Gravity.CENTER);
        container_top.setOrientation(LinearLayout.VERTICAL);

        // Circular Logo with Glowing Ring Frame
        FrameLayout logoFrame = new FrameLayout(context);
        LinearLayout.LayoutParams logoFrameParams = new LinearLayout.LayoutParams(
                utils.FixDP(38), utils.FixDP(38));
        logoFrameParams.setMargins(0, 0, 0, utils.FixDP(3));
        logoFrame.setLayoutParams(logoFrameParams);

        ImageBase64 icon_menu = new ImageBase64(context);
        FrameLayout.LayoutParams iconMenuParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        icon_menu.setLayoutParams(iconMenuParams);
        if (placeholderDrawable != null) {
            icon_menu.setImageDrawable(placeholderDrawable);
        }
        if (RemoteConfig.floatingIconUrl != null && !RemoteConfig.floatingIconUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(context)
                    .load(RemoteConfig.floatingIconUrl)
                    .placeholder(placeholderDrawable)
                    .error(placeholderDrawable)
                    .into(icon_menu);
        }
        logoFrame.addView(icon_menu);
        container_top.addView(logoFrame);

        // Menu Header Title
        TextView menuTitle = new TextView(context);
        String name = (RemoteConfig.appName != null && !RemoteConfig.appName.isEmpty())
                ? RemoteConfig.appName.toUpperCase() : "MOBILE PANEL";
        menuTitle.setText(name);
        menuTitle.setTextSize(11.5f);
        menuTitle.setTextColor(Color.WHITE);
        menuTitle.setTypeface(Typeface.DEFAULT_BOLD);
        menuTitle.setLetterSpacing(0.06f);
        menuTitle.setGravity(Gravity.CENTER);
        container_top.addView(menuTitle);

        // Compact Status Tag
        TextView statusTag = new TextView(context);
        statusTag.setText("● READY • v9.0");
        statusTag.setTextSize(7.5f);
        statusTag.setTextColor(Color.parseColor("#4ADE80")); // Neon Green
        statusTag.setTypeface(Typeface.DEFAULT_BOLD);
        statusTag.setLetterSpacing(0.06f);
        statusTag.setGravity(Gravity.CENTER);
        statusTag.setPadding(0, utils.FixDP(1), 0, 0);
        container_top.addView(statusTag);

        // Divider Line
        View headerDivider = new View(context);
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, utils.FixDP(1));
        divParams.setMargins(utils.FixDP(8), utils.FixDP(5), utils.FixDP(8), 0);
        headerDivider.setLayoutParams(divParams);
        headerDivider.setBackgroundColor(Color.parseColor("#2A1A45"));
        container_top.addView(headerDivider);

        // Tabs container
        HorizontalScrollView tabsScrollView = new HorizontalScrollView(context);
        tabsScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                utils.FixDP(32)));
        tabsScrollView.setHorizontalScrollBarEnabled(false);

        tabsContainer = new LinearLayout(context);
        tabsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setPadding(utils.FixDP(4), 0, utils.FixDP(4), 0);
        tabsScrollView.addView(tabsContainer);

        // Center section: Compact Mobile Scrollable Height (160dp)
        final LinearLayout container_center = new LinearLayout(context);
        container_center.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                utils.FixDP(160)));
        container_center.setGravity(Gravity.CENTER);

        // Scroll view for features
        scrollView_center = new ScrollView(context);
        scrollView_center.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView_center.setPadding(utils.FixDP(6), utils.FixDP(2), utils.FixDP(6), utils.FixDP(2));
        scrollView_center.setVerticalScrollBarEnabled(false);

        // Container for all feature tabs
        featuresScrollContainer = new LinearLayout(context);
        featuresScrollContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        featuresScrollContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView_center.addView(featuresScrollContainer);

        // Progress bar
        final ProgressBar progressBar = new ProgressBar(context);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                utils.FixDP(36),
                utils.FixDP(36)));
        progressBar.getIndeterminateDrawable().setColorFilter(PrimaryColor, PorterDuff.Mode.SRC_IN);

        // ==========================================
        // 🔮 BOTTOM SECTION (ALWAYS VISIBLE BUTTON)
        // ==========================================
        LinearLayout container_bottom = new LinearLayout(context);
        container_bottom.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container_bottom.setPadding(
                utils.FixDP(8),
                utils.FixDP(4),
                utils.FixDP(8),
                utils.FixDP(6));
        container_bottom.setOrientation(LinearLayout.VERTICAL);
        container_bottom.setGravity(Gravity.CENTER);

        // High-tech Cyber Gradient Button
        GradientDrawable gradientDrawable_inject_close = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] { Color.parseColor("#9333EA"), Color.parseColor("#6B21A8") }
        );
        gradientDrawable_inject_close.setCornerRadius(utils.FixDP(10));
        gradientDrawable_inject_close.setStroke(utils.FixDP(1f), Color.parseColor("#C084FC"));
        RippleDrawable rippleDrawable = new RippleDrawable(
                ColorStateList.valueOf(0x44FFFFFF),
                gradientDrawable_inject_close,
                null);

        final Button inject_close = new Button(context);
        inject_close.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                utils.FixDP(34)));
        inject_close.setPadding(0, 0, 0, 0);
        inject_close.setText("[ ⚡ INJECT CORE ]");
        inject_close.setTextSize(11f);
        inject_close.setTextColor(0xFFFFFFFF);
        inject_close.setTypeface(Typeface.DEFAULT_BOLD);
        inject_close.setLetterSpacing(0.06f);
        inject_close.setBackground(rippleDrawable);

        inject_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonClick == 0) {
                    Toast.makeText(context, "Initializing cheat core...", Toast.LENGTH_SHORT).show();

                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Init(); // Call JNI initializers
                                Functions(); // Call JNI builder to construct the switches & menus!
                            } catch (UnsatisfiedLinkError e) {
                                e.printStackTrace();
                            }
                            progressBar.setVisibility(View.GONE);
                            inject_close.setText("[ ✕ HIDE HUD ]");
                            container_center.removeAllViews();
                            container_center.addView(scrollView_center);
                            buttonClick++;
                            Toast.makeText(context, "✅ Injection successful!", Toast.LENGTH_SHORT).show();
                        }
                    }, 600);

                } else if (buttonClick >= 1) {
                    icon_cheat.setVisibility(View.VISIBLE);
                    container_menu.setVisibility(View.GONE);
                    try {
                        windowManager.updateViewLayout(frameLayout, windowManagerParams);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Add all views to their respective containers
        frameLayout.addView(container);
        container.addView(icon_cheat);
        container.addView(container_menu);

        container_menu.addView(container_top);
        container_menu.addView(container_center);
        container_center.addView(progressBar);

        container_menu.addView(container_bottom);
        container_bottom.addView(inject_close);
    }

    // Create System Window
    public void onCreateSystemWindow() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        frameLayout.setOnTouchListener(onTouchListener());
        frameLayout.setAlpha(0.96f);

        windowManagerParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                        WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSPARENT);
        windowManagerParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowManagerParams.x = 40;
        windowManagerParams.y = 60;

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DrawCanvas();
        windowManager.addView(frameLayout, windowManagerParams);
    }

    // OnTouchListener for menu dragging
    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            private static final int TOUCH_MOVE_THRESHOLD = 8;
            private int x;
            private int y;
            private int initialX;
            private int initialY;
            private boolean isMoving = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        initialX = x;
                        initialY = y;
                        isMoving = false;
                        frameLayout.setAlpha(0.85f);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();

                        int totalMoveX = Math.abs(nowX - initialX);
                        int totalMoveY = Math.abs(nowY - initialY);

                        if (!isMoving && (totalMoveX > TOUCH_MOVE_THRESHOLD || totalMoveY > TOUCH_MOVE_THRESHOLD)) {
                            isMoving = true;
                        }

                        if (isMoving) {
                            int movedX = nowX - x;
                            int movedY = nowY - y;
                            x = nowX;
                            y = nowY;
                            windowManagerParams.x = windowManagerParams.x + movedX;
                            windowManagerParams.y = windowManagerParams.y + movedY;
                            windowManager.updateViewLayout(frameLayout, windowManagerParams);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            v.performClick();
                        }
                        frameLayout.setAlpha(0.96f);
                        return true;

                    default:
                        break;
                }
                return false;
            }
        };
    }

    // ==========================================
    // 🔮 COMPACT CYBER HUD WIDGET BUILDERS
    // ==========================================

    public static void addTab(final String tabName) {
        final boolean isFirstTab = tabButtons.isEmpty();

        final TextView tabButton = new TextView(context);
        tabButton.setVisibility(View.GONE);
        tabButtons.add(tabButton);

        if (tabsContainer != null) {
            tabsContainer.addView(tabButton);
        }

        LinearLayout tabContent = new LinearLayout(context);
        tabContent.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tabContent.setOrientation(LinearLayout.VERTICAL);
        tabContent.setPadding(utils.FixDP(2), utils.FixDP(1), utils.FixDP(2), utils.FixDP(1));
        tabContent.setVisibility(isFirstTab ? View.VISIBLE : View.GONE);

        tabContentContainers.put(tabName, tabContent);
        featuresScrollContainer.addView(tabContent);

        if (isFirstTab)
            currentTab = tabName;
    }

    private static void selectTab(String tabName) {
        if (tabName.equals(currentTab))
            return;

        for (Map.Entry<String, LinearLayout> entry : tabContentContainers.entrySet()) {
            entry.getValue().setVisibility(entry.getKey().equals(tabName) ? View.VISIBLE : View.GONE);
        }

        currentTab = tabName;
    }

    /**
     * Add a category heading (Compact Cyber Bracket Header)
     */
    public static void addCategory(String name) {
        if (currentTab.isEmpty() || !tabContentContainers.containsKey(currentTab)) {
            return;
        }

        LinearLayout categoryRow = new LinearLayout(context);
        categoryRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                utils.FixDP(24)));
        categoryRow.setOrientation(LinearLayout.HORIZONTAL);
        categoryRow.setGravity(Gravity.CENTER_VERTICAL);
        categoryRow.setPadding(utils.FixDP(8), 0, utils.FixDP(8), 0);

        GradientDrawable catBg = new GradientDrawable();
        catBg.setColor(Color.parseColor("#20133A"));
        catBg.setCornerRadius(utils.FixDP(6));
        catBg.setStroke(utils.FixDP(1), Color.parseColor("#7E22CE"));
        categoryRow.setBackground(catBg);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) categoryRow.getLayoutParams();
        params.setMargins(0, utils.FixDP(5), 0, utils.FixDP(3));
        categoryRow.setLayoutParams(params);

        TextView iconTag = new TextView(context);
        iconTag.setText("⚡ ");
        iconTag.setTextSize(9.5f);
        categoryRow.addView(iconTag);

        TextView textView = new TextView(context);
        textView.setText("// " + name.toUpperCase() + " //");
        textView.setTextSize(9.5f);
        textView.setTextColor(Color.parseColor("#E9D5FF"));
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLetterSpacing(0.06f);
        categoryRow.addView(textView);

        tabContentContainers.get(currentTab).addView(categoryRow);
    }

    /**
     * Add a switch to the current tab (Compact Cyber HUD Card Tile)
     */
    public static void addSwitch(String name, final int ID) {
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(utils.FixDP(8), utils.FixDP(5), utils.FixDP(8), utils.FixDP(5));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Cyber Tile background
        final GradientDrawable tileBg = new GradientDrawable();
        tileBg.setColor(Color.parseColor("#170F2B"));
        tileBg.setCornerRadius(utils.FixDP(8));
        tileBg.setStroke(utils.FixDP(1), Color.parseColor("#2C1B4A"));
        linearLayout.setBackground(tileBg);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        params.setMargins(0, utils.FixDP(2), 0, utils.FixDP(2));
        linearLayout.setLayoutParams(params);

        // Left Column: Feature Title + Status Tag
        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        final TextView titleView = new TextView(context);
        titleView.setText(name);
        titleView.setTextSize(10.5f);
        titleView.setTextColor(Color.parseColor("#CBD5E1"));
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(titleView);

        final TextView statusTag = new TextView(context);
        statusTag.setText("[ OFF ]");
        statusTag.setTextSize(7.5f);
        statusTag.setTextColor(Color.parseColor("#64748B"));
        statusTag.setTypeface(Typeface.DEFAULT_BOLD);
        statusTag.setLetterSpacing(0.04f);
        textCol.addView(statusTag);

        linearLayout.addView(textCol);

        final SwitchStyle switchStyle = new SwitchStyle(context);
        switchStyle.setLayoutParams(new LinearLayout.LayoutParams(utils.FixDP(36), utils.FixDP(19)));

        switchStyle.setOnCheckedChangeListener(new SwitchStyle.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchStyle view, boolean isChecked) {
                ChangesID(ID, 0);

                if (isChecked) {
                    titleView.setTextColor(Color.WHITE);
                    statusTag.setText("[ ACTIVE ⚡ ]");
                    statusTag.setTextColor(Color.parseColor("#C084FC"));
                    tileBg.setColor(Color.parseColor("#22143D"));
                    tileBg.setStroke(utils.FixDP(1f), Color.parseColor("#A855F7"));
                } else {
                    titleView.setTextColor(Color.parseColor("#CBD5E1"));
                    statusTag.setText("[ OFF ]");
                    statusTag.setTextColor(Color.parseColor("#64748B"));
                    tileBg.setColor(Color.parseColor("#170F2B"));
                    tileBg.setStroke(utils.FixDP(1), Color.parseColor("#2C1B4A"));
                }
            }
        });

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchStyle.setChecked(!switchStyle.isChecked());
            }
        });

        linearLayout.addView(switchStyle);
        tabContentContainers.get(currentTab).addView(linearLayout);
    }

    public static void addSeekBar(final String name, int value, int max, final String type, final int ID) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(utils.FixDP(8), utils.FixDP(5), utils.FixDP(8), utils.FixDP(5));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        GradientDrawable tileBg = new GradientDrawable();
        tileBg.setColor(Color.parseColor("#170F2B"));
        tileBg.setCornerRadius(utils.FixDP(8));
        tileBg.setStroke(utils.FixDP(1), Color.parseColor("#2C1B4A"));
        linearLayout.setBackground(tileBg);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        params.setMargins(0, utils.FixDP(2), 0, utils.FixDP(2));
        linearLayout.setLayoutParams(params);

        final TextView textView = new TextView(context);
        textView.setText(name.concat(": ") + value + type);
        textView.setTextSize(10f);
        textView.setTextColor(0xFFFFFFFF);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        if (type.equals("Color")) {
            if (value == 0) {
                textView.setText(Html.fromHtml(name + ": <font color='#ffffff'>" + "White" + "</font>"));
            } else if (value == 1) {
                textView.setText(Html.fromHtml(name + ": <font color='#00FF00'>" + "Green" + "</font>"));
            } else if (value == 2) {
                textView.setText(Html.fromHtml(name + ": <font color='#0000FF'>" + "Blue" + "</font>"));
            } else if (value == 3) {
                textView.setText(Html.fromHtml(name + ": <font color='#FF0000'>" + "Red" + "</font>"));
            } else if (value == 4) {
                textView.setText(Html.fromHtml(name + ": <font color='#000000'>" + "Black" + "</font>"));
            } else if (value == 5) {
                textView.setText(Html.fromHtml(name + ": <font color='#FFFF00'>" + "Yellow" + "</font>"));
            } else if (value == 6) {
                textView.setText(Html.fromHtml(name + ": <font color='#00FFFF'>" + "Cyan" + "</font>"));
            } else if (value == 7) {
                textView.setText(Html.fromHtml(name + ": <font color='#FF00FF'>" + "Magenta" + "</font>"));
            } else if (value == 8) {
                textView.setText(Html.fromHtml(name + ": <font color='#808080'>" + "Gray" + "</font>"));
            } else if (value == 9) {
                textView.setText(Html.fromHtml(name + ": <font color='#A020F0'>" + "Purple" + "</font>"));
            }
        } else if (type.equals("BoxType")) {
            if (value == 0) {
                textView.setText(name.concat(": Stroke"));
            } else if (value == 1) {
                textView.setText(name.concat(": Filled"));
            } else if (value == 2) {
                textView.setText(name.concat(": Rounded"));
            }
        } else if (type.equals("LineType")) {
            if (value == 0) {
                textView.setText(name.concat(": Top"));
            } else if (value == 1) {
                textView.setText(name.concat(": Center"));
            } else if (value == 2) {
                textView.setText(name.concat(": Bottom"));
            }
        }

        SeekBar seekBar = new SeekBar(context);
        seekBar.getThumb().setColorFilter(PrimaryColor, PorterDuff.Mode.SRC_IN);
        seekBar.getProgressDrawable().setColorFilter(PrimaryColor, PorterDuff.Mode.SRC_IN);
        seekBar.setProgress(value);
        seekBar.setMax(max);
        if (type.equals("Color")) {
            seekBar.setMax(9);
        } else if (type.equals("BoxType")) {
            seekBar.setMax(2);
        } else if (type.equals("LineType")) {
            seekBar.setMax(2);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (type.equals("Color")) {
                    if (i == 0) {
                        textView.setText(Html.fromHtml(name + ": <font color='#ffffff'>" + "White" + "</font>"));
                    } else if (i == 1) {
                        textView.setText(Html.fromHtml(name + ": <font color='#00FF00'>" + "Green" + "</font>"));
                    } else if (i == 2) {
                        textView.setText(Html.fromHtml(name + ": <font color='#0000FF'>" + "Blue" + "</font>"));
                    } else if (i == 3) {
                        textView.setText(Html.fromHtml(name + ": <font color='#FF0000'>" + "Red" + "</font>"));
                    } else if (i == 4) {
                        textView.setText(Html.fromHtml(name + ": <font color='#000000'>" + "Black" + "</font>"));
                    } else if (i == 5) {
                        textView.setText(Html.fromHtml(name + ": <font color='#FFFF00'>" + "Yellow" + "</font>"));
                    } else if (i == 6) {
                        textView.setText(Html.fromHtml(name + ": <font color='#00FFFF'>" + "Cyan" + "</font>"));
                    } else if (i == 7) {
                        textView.setText(Html.fromHtml(name + ": <font color='#FF00FF'>" + "Magenta" + "</font>"));
                    } else if (i == 8) {
                        textView.setText(Html.fromHtml(name + ": <font color='#808080'>" + "Gray" + "</font>"));
                    } else if (i == 9) {
                        textView.setText(Html.fromHtml(name + ": <font color='#A020F0'>" + "Purple" + "</font>"));
                    }
                } else if (type.equals("BoxType")) {
                    if (i == 0) {
                        textView.setText(name.concat(": Stroke"));
                    } else if (i == 1) {
                        textView.setText(name.concat(": Filled"));
                    } else if (i == 2) {
                        textView.setText(name.concat(": Corner"));
                    }
                } else if (type.equals("LineType")) {
                    if (i == 0) {
                        textView.setText(name.concat(": Top"));
                    } else if (i == 1) {
                        textView.setText(name.concat(": Center"));
                    } else if (i == 2) {
                        textView.setText(name.concat(": Bottom"));
                    }
                } else {
                    textView.setText(name.concat(": ") + i + type);
                }

                ChangesID(ID, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        tabContentContainers.get(currentTab).addView(linearLayout);
    }

    // Injection methods
    private boolean InjectX86(String Lib) {
        try {
            String injector = context.getApplicationInfo().nativeLibraryDir + File.separator + "libupakul.so";
            String payload_source = context.getApplicationInfo().nativeLibraryDir + File.separator + Lib;
            String payload_dest = "/data/local/" + Lib;
            String payload_dest2 = "/data/local/libifuhiufoi.so";

            Shell.su("cp " + payload_source + " " + payload_dest).exec();
            Shell.su("cp " + injector + " " + payload_dest2).exec();
            Shell.su("su -c chmod 777 " + payload_dest).exec();
            Shell.su("su -c chmod 777 " + payload_dest2).exec();
            Shell.su("su -c " + payload_dest2 + " " + target + " " + payload_dest).exec();
            Shell.su("rm -f " + payload_dest).exec();
            Shell.su("rm -f " + payload_dest2).exec();
            Functions();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean InjectX32(String Lib) {
        try {
            String injector = context.getApplicationInfo().nativeLibraryDir + File.separator + "libinjectMobile.so";
            String payload_source = context.getApplicationInfo().nativeLibraryDir + File.separator + Lib;
            String payload_dest = "/data/local/" + Lib;
            String payload_dest2 = "/data/local/libinject.so";

            Shell.su("cp " + payload_source + " " + payload_dest).exec();
            Shell.su("cp " + injector + " " + payload_dest2).exec();
            Shell.su("su -c chmod 755 " + payload_dest).exec();
            Shell.su("su -c chmod 777 " + payload_dest2).exec();
            Shell.su("su -c " + payload_dest2 + " -f -n " + target + " -so " + payload_dest + " --hide-memory").exec();
            Shell.su("rm -f " + payload_dest).exec();
            Shell.su("rm -f " + payload_dest2).exec();
            Functions();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}

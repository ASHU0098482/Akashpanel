package com.ashu;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteConfig {
    // ==========================================
    // REPLACE THIS URL WITH YOUR JSON FILE URL!
    // ==========================================
    public static final String CONFIG_URL = "https://raw.githubusercontent.com/ASHU0098482/Akashpanel/main/config.json";

    public static boolean isOnline = true;
    public static String maintenanceMessage = "Mobile Panel is currently active.";
    public static String appName = "Mobile Panel";

    public static boolean showNotice = false;
    public static String noticeTitle = "";
    public static String noticeMessage = "";

    public static String keyauthOwnerId = "8Z9qRQ2zph";
    public static String keyauthAppName = "vip panel"; // The name registered in KeyAuth dashboard
    public static String keyauthSecret = "fddc19ec5be9ebee148b808beaa5dad04f803aac21cf6f4a224a5f832ef97dbd";
    public static String keyauthVersion = "1.0";
    public static String keyauthUrl = "https://keyauth.win/api/1.3/";

    public static int remoteVersionCode = 13;
    public static String updateUrl = "";

    public static boolean showWebsiteBanner = false;

    // Remote customizable UI assets
    public static String logoUrl = "https://raw.githubusercontent.com/ASHU0098482/Akashpanel/main/jack_logo.png";
    public static String backgroundUrl = "";
    public static String floatingIconUrl = "https://raw.githubusercontent.com/ASHU0098482/Akashpanel/main/jack_logo_small.png";

    public static void fetchConfig(Runnable onComplete) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            boolean success = false;
            
            // Endpoints: 1. GitHub API (Instant 0s cache), 2. Direct Raw GitHub, 3. jsDelivr
            String[] urlsToTry = new String[] {
                "https://api.github.com/repos/ASHU0098482/Akashpanel/contents/config.json?t=" + System.currentTimeMillis(),
                CONFIG_URL + "?t=" + System.currentTimeMillis() + "&rnd=" + (int)(Math.random() * 100000),
                "https://cdn.jsdelivr.net/gh/ASHU0098482/Akashpanel@main/config.json?t=" + System.currentTimeMillis()
            };

            for (String currentUrlStr : urlsToTry) {
                try {
                    URL url = new URL(currentUrlStr);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setUseCaches(false);
                    conn.setDefaultUseCaches(false);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
                    conn.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                    conn.setRequestProperty("Pragma", "no-cache");
                    conn.setRequestProperty("Expires", "0");
                    conn.setRequestProperty("Accept", "application/json");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        
                        JSONObject json = new JSONObject(response.toString().trim());

                        // Handle GitHub API base64 response if content field is present
                        if (json.has("content") && json.has("encoding") && "base64".equalsIgnoreCase(json.optString("encoding"))) {
                            String encodedContent = json.getString("content").replace("\n", "").replace("\r", "");
                            byte[] decodedBytes = android.util.Base64.decode(encodedContent, android.util.Base64.DEFAULT);
                            json = new JSONObject(new String(decodedBytes, "UTF-8").trim());
                        }
                        
                        // Parse values
                        String status = json.optString("status", "maintenance");
                        
                        isOnline = status.equalsIgnoreCase("online");
                        maintenanceMessage = json.optString("maintenance_message", "APK under maintenance. Contact Akash.");
                        appName = json.optString("app_name", "Mobile Panel");
                        
                        showNotice = json.optBoolean("show_notice", false);
                        noticeTitle = json.optString("notice_title", "");
                        noticeMessage = json.optString("notice_message", "");
                        
                        keyauthOwnerId = json.optString("keyauth_owner_id", "8Z9qRQ2zph");
                        keyauthAppName = json.optString("keyauth_app_name", "vip panel");
                        keyauthSecret = json.optString("keyauth_secret", "fddc19ec5be9ebee148b808beaa5dad04f803aac21cf6f4a224a5f832ef97dbd");
                        keyauthVersion = json.optString("keyauth_version", "1.0");
                        keyauthUrl = json.optString("keyauth_url", "https://keyauth.win/api/1.3/");
                        
                        remoteVersionCode = json.optInt("apk_version_code", 1);
                        updateUrl = json.optString("apk_update_url", "");

                        logoUrl = json.optString("logo_url", "");
                        backgroundUrl = json.optString("background_url", "");
                        floatingIconUrl = json.optString("floating_icon_url", "");

                        success = true;
                        break; // Successfully fetched!
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (reader != null) reader.close();
                        if (conn != null) conn.disconnect();
                    } catch (Exception ignored) {}
                }
            }

            if (!success) {
                // If fetch fails, keep maintenance mode active
                isOnline = false;
            }

            // Invoke callback
            if (onComplete != null) {
                onComplete.run();
            }
        }).start();
    }
}

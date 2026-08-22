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
    public static final String CONFIG_URL = "https://raw.githubusercontent.com/ASHU0098482/status/main/config.json";

    public static boolean isOnline = true;
    public static String maintenanceMessage = "JACK PANEL is currently active.";
    public static String appName = "JACK PANEL";

    public static boolean showNotice = false;
    public static String noticeTitle = "";
    public static String noticeMessage = "";

    public static String keyauthOwnerId = "8Z9qRQ2zph";
    public static String keyauthAppName = "vip panel"; // The name registered in KeyAuth dashboard
    public static String keyauthSecret = "fddc19ec5be9ebee148b808beaa5dad04f803aac21cf6f4a224a5f832ef97dbd";
    public static String keyauthVersion = "1.0";
    public static String keyauthUrl = "https://keyauth.win/api/1.3/";

    public static int remoteVersionCode = 47;
    public static String updateUrl = "";

    public static boolean showWebsiteBanner = false;

    // Remote customizable UI assets
    public static String logoUrl = "https://raw.githubusercontent.com/ASHU0098482/status/main/jack_logo.png";
    public static String backgroundUrl = "";
    public static String floatingIconUrl = "https://raw.githubusercontent.com/ASHU0098482/status/main/jack_logo_small.png";

    public static void fetchConfig(Runnable onComplete) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            boolean success = false;
            
            // Use direct GitHub raw with fresh timestamp to avoid any caching
            String[] urlsToTry = new String[] {
                CONFIG_URL + "?t=" + System.currentTimeMillis() + "&rnd=" + (int)(Math.random() * 100000),
                CONFIG_URL + "?nocache=" + System.currentTimeMillis()
            };

            for (String currentUrlStr : urlsToTry) {
                try {
                    URL url = new URL(currentUrlStr);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setUseCaches(false);
                    conn.setDefaultUseCaches(false);
                    conn.setConnectTimeout(6000);
                    conn.setReadTimeout(6000);
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
                        
                        // Parse values
                        String status = json.optString("status", "online");
                        
                        isOnline = status.equalsIgnoreCase("online");
                        maintenanceMessage = json.optString("maintenance_message", "APK is currently under maintenance.");
                        appName = json.optString("app_name", "JACK PANEL");
                        
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
                // Keep online state if fetch fails to avoid blocking users
                isOnline = true;
            }

            // Invoke callback
            if (onComplete != null) {
                onComplete.run();
            }
        }).start();
    }
}

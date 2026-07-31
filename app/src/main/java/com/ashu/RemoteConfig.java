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
    public static String maintenanceMessage = "VIP Panel is currently active.";
    public static String appName = "VIP PANEL";

    public static boolean showNotice = false;
    public static String noticeTitle = "";
    public static String noticeMessage = "";

    public static String keyauthOwnerId = "8Z9qRQ2zph";
    public static String keyauthAppName = "FAKE APK"; // The name registered in KeyAuth dashboard
    public static String keyauthSecret = "4051f82b3d1667c9aad01c19afc42794f3a9de891498a0588b74c1b3abc01908";
    public static String keyauthVersion = "1.0";
    public static String keyauthUrl = "https://keyauth.win/api/1.3/";

    public static int remoteVersionCode = 16;
    public static String updateUrl = "";

    // Remote customizable UI assets
    public static String logoUrl = "https://raw.githubusercontent.com/ASHU0098482/status/main/skull_logo.png";
    public static String backgroundUrl = "";
    public static String floatingIconUrl = "";

    public static void fetchConfig(Runnable onComplete) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(CONFIG_URL + "?t=" + System.currentTimeMillis());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
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
                    appName = json.optString("app_name", "VIP PANEL");
                    
                    showNotice = json.optBoolean("show_notice", false);
                    noticeTitle = json.optString("notice_title", "");
                    noticeMessage = json.optString("notice_message", "");
                    
                    keyauthOwnerId = json.optString("keyauth_owner_id", "8Z9qRQ2zph");
                    keyauthAppName = json.optString("keyauth_app_name", "FAKE APK");
                    keyauthSecret = json.optString("keyauth_secret", "4051f82b3d1667c9aad01c19afc42794f3a9de891498a0588b74c1b3abc01908");
                    keyauthVersion = json.optString("keyauth_version", "1.0");
                    keyauthUrl = json.optString("keyauth_url", "https://keyauth.win/api/1.3/");
                    
                    remoteVersionCode = json.optInt("apk_version_code", 1);
                    updateUrl = json.optString("apk_update_url", "");

                    logoUrl = json.optString("logo_url", "");
                    backgroundUrl = json.optString("background_url", "");
                    floatingIconUrl = json.optString("floating_icon_url", "");
                }
            } catch (Exception e) {
                // On error (e.g., no internet connection), default to online state
                isOnline = true;
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (conn != null) conn.disconnect();
                } catch (Exception ignored) {}
                
                // Invoke callback
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }).start();
    }
}

#include <list>
#include <vector>
#include <pthread.h>
#include <cstring>
#include <jni.h>
#include <unistd.h>
#include <fstream>
#include <iostream>
#include <dlfcn.h>
#include "Tools/Includes/Logger.h"
#include "Tools/Includes/obfuscate.h"
#include "Tools/Includes/Utils.h"

#include "Tools/SOCKET/client.h"
#include "Tools/SOCKET/IncludeClient.h"
#include "Widgets/ImportWidgets.h"
#include "Tools/DrawTools/Draw.h"
#include <chrono>
extern "C"
JNIEXPORT jstring JNICALL
Java_com_ashu_Menu_imageBase64(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(OBFUSCATE("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"));
}

struct {
    bool enableESP = false;
    bool AimSilent = false;
    bool AimSilent360 = false;
    bool autoswitch = false;

    bool downaimkill = false;
    bool resetguest = false;
    bool telekill = false;
    bool Aimkilltpv2 = false;
    bool Aimkillrotate = false;
    bool Aimkillrotatev2 = false;
    bool Aimkillrotatev3 = false;
    bool Aimkilltp = false;
    bool downplayer = false;
    bool highjump = false;
    bool medikitrun = false;
    bool ultraswitch = false;
    bool speedhackjoy = false;
    bool speedrun = false;
    bool cameraup = false;
    bool wallHack = false;
    bool teleportcar = false;
    bool doublegun = false;
    bool upplayerx = false;
    bool telehack = false;
    bool aimbody = false;
    bool TeleBeta = false;
    bool climbup = false;
    float FlyUp = 0.0f;
    int FlySpeed = 0;
    float vehicle_y = 0.0f;
    float vehicle_unY = 0.0f;
} MasterBool;

struct {
    bool enableAimbot = false;
    bool aimbotShoot = false;
    bool aimbotScope = false;
    bool Aimkill = false;
    bool Aimkill360 = false;
    bool teleprt = false;

    bool Aimkillrage = false;
    bool aimbot = false;
    bool SilentAim = false;
    bool aimbotbody = false;
    bool UnlimitedAmmo = false;
    bool norecoil = false;
    float aimbotFOV = 0.0f;
    float aimbotSmoothness = 20.0f;
    float speedValue = 0.0f;
    bool speedHack = false;
} pAimbotPlayer;

struct {
    bool espLine = false;
    bool espBox = false;
    bool espInfo = false;
    bool espHealth = false;
    bool DISC = false;
    bool espDrawFov = false;
    bool espTracker = false;
    bool espLineTracker = false;
    Color espColor = Color::White();
    Color espnameColor = Color::White();
    bool espNickName = false;
    int lineType = 0;
    int boxType = 0;
} pEspPlayer;

struct {
    bool speedHack = false;
    bool undergroundCatapult = false;
    bool catapultDistance = false;
} pMemoryTools;

using namespace std;

std::string LoggedInOwnerID = "";

bool showAnimation = false;
long long animationStartTime = 0;

int frameCount = 0;
float fpsValue = 0.0f;
long long lastFpsTime = 0;

long long getCurrentTimeMs() {
    return std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::system_clock::now().time_since_epoch()
    ).count();
}

struct FeatureNotification {
    char name[64];
    bool enabled;
    long long startTime;
    bool active;
} currentNotification;

void showNotification(const char* name, bool enabled) {
    strcpy(currentNotification.name, name);
    currentNotification.enabled = enabled;
    currentNotification.startTime = getCurrentTimeMs();
    currentNotification.active = true;
}

// Called from Java to send owner ID

// Show toast from native
void ShowErrorToast(JNIEnv* env, const char* message) {
    jclass cls = env->FindClass("com/ashu/Login");
    if (!cls) return;

    jmethodID method = env->GetStaticMethodID(cls, "showToastFromNative", "(Landroid/content/Context;Ljava/lang/String;)V");
    if (!method) return;

    jfieldID contextField = env->GetStaticFieldID(cls, "globalContext", "Landroid/content/Context;");
    jobject context = env->GetStaticObjectField(cls, contextField);
    if (!context) return;

    jstring jMessage = env->NewStringUTF(message);
    env->CallStaticVoidMethod(cls, method, context, jMessage);
    env->DeleteLocalRef(jMessage);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_ashu_Login_sendOwnerIDToNative(JNIEnv* env, jobject, jstring ownerId) {
    const char *nativeStr = env->GetStringUTFChars(ownerId, 0);
    LoggedInOwnerID = std::string(nativeStr);
    env->ReleaseStringUTFChars(ownerId, nativeStr);

    LOGD("🔒 Owner ID received from Java: %s", LoggedInOwnerID.c_str());

    if (LoggedInOwnerID == "8Z9qRQ2zph") {
    } else {
        LOGD("❌ Blocked features for unknown owner ID: %s", LoggedInOwnerID.c_str());
    }
}



extern "C"
JNIEXPORT void JNICALL
Java_com_ashu_Menu_Init(JNIEnv *env, jclass thiz) {
    startClient();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ashu_Menu_Functions(JNIEnv *env, jclass clazz) {
    Widget widget = Widget(env);
    widget.Tab("Functions");
    widget.Tab1("Functions");
    // ---------------- Aim Features ----------------
    widget.Category(OBFUSCATE("Aimbot Features"));
    widget.Switch(OBFUSCATE("Activate All"), 102);
    widget.Switch(OBFUSCATE("Silent Aim"), 103);
    widget.Switch(OBFUSCATE("Drag Headshot"), 1055);
    widget.Switch(OBFUSCATE("Sniper Auto Aim"), 500);
    widget.Switch(OBFUSCATE("Up Player"), 20);
    widget.Switch(OBFUSCATE("Show Fov"), 16);
    widget.SeekBar(OBFUSCATE("Adjust Headshot Rate"), 0, 100, "%", 104);

    // ---------------- ESP Features ----------------
    widget.Category(OBFUSCATE("ESP"));
    widget.Switch(OBFUSCATE("ESP Line"), 1);
    widget.Switch(OBFUSCATE("ESP Box"), 2);
    widget.Switch(OBFUSCATE("ESP Name"), 4);
    widget.Switch(OBFUSCATE("ESP Health"), 9);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_ashu_Menu_ChangesID(JNIEnv *env, jclass clazz, jint id, jint value) {
    switch (id) {


        case 102: { // ENABLE ALL
            if (LoggedInOwnerID.find("8Z9qRQ2zph") != std::string::npos) {

                pAimbotPlayer.enableAimbot = !pAimbotPlayer.enableAimbot;
                SendFeatuere(101, pAimbotPlayer.enableAimbot);
                MasterBool.enableESP = !MasterBool.enableESP;
                SendFeatuere(3, MasterBool.enableESP);

                MasterBool.ultraswitch = !MasterBool.ultraswitch;
                SendFeatuere(212, MasterBool.ultraswitch);

                if (pAimbotPlayer.enableAimbot) {
                    showAnimation = true;
                    animationStartTime = getCurrentTimeMs();
                } else {
                    showAnimation = false;
                }
                showNotification("Activate All", pAimbotPlayer.enableAimbot);
            } else {
                ShowErrorToast(env, "JACK PANEL ALWAYS ON TOP ✅.   ");
                LOGD("❌ BLOCKED: Owner ID mismatch! Found: %s", LoggedInOwnerID.c_str());
            }
            break;
        }


        case 103:
            pAimbotPlayer.Aimkill = !pAimbotPlayer.Aimkill;
            SendFeatuere(103, pAimbotPlayer.Aimkill);
            showNotification("Silent Aim", pAimbotPlayer.Aimkill);
            break;
        case 1055:
            pAimbotPlayer.Aimkill360 = !pAimbotPlayer.Aimkill360;
            SendFeatuere(1055, pAimbotPlayer.Aimkill360);
            showNotification("Drag Headshot", pAimbotPlayer.Aimkill360);
            break;
        case 7581:
            pAimbotPlayer.Aimkillrage = !pAimbotPlayer.Aimkillrage;
            SendFeatuere(7581, pAimbotPlayer.Aimkillrage);
            break;
        case 104:
            pAimbotPlayer.aimbotFOV = value;
            SendFOV(104, value);
            break;

        case 105:
            pAimbotPlayer.aimbot = !pAimbotPlayer.aimbot;
            SendFeatuere(105, pAimbotPlayer.aimbot);
            break;

        case 106:
            pAimbotPlayer.norecoil = !pAimbotPlayer.norecoil;
            SendFeatuere(106, pAimbotPlayer.norecoil);
            break;

        case 107:
            pAimbotPlayer.aimbotSmoothness = value;
            SendFOV(107, value);
            break;
        case 1044:
            MasterBool.FlyUp = value;
            SendFOV(1044, value);
            break;
        case 1043:
            MasterBool.FlySpeed = value;
            SendFOV(1043, value);
            break;
        case 108:
            pAimbotPlayer.aimbotbody = !pAimbotPlayer.aimbotbody;
            SendFeatuere(108, pAimbotPlayer.aimbotbody);
            break;

        case 109:
            pAimbotPlayer.speedHack = !pAimbotPlayer.speedHack;
            SendFeatuere(109, pAimbotPlayer.speedHack);
            break;
        case 147:
            pAimbotPlayer.UnlimitedAmmo = !pAimbotPlayer.UnlimitedAmmo;
            SendFeatuere(147, pAimbotPlayer.UnlimitedAmmo);
            break;
        case 110:
            pAimbotPlayer.speedValue = value;
            SendFOV(110, value);
            break;

        case 111:
            pAimbotPlayer.SilentAim = !pAimbotPlayer.SilentAim;
            SendFeatuere(111, pAimbotPlayer.SilentAim);
            break;
        case 550:
            pAimbotPlayer.teleprt = !pAimbotPlayer.teleprt;
            SendFeatuere(550, pAimbotPlayer.teleprt);
            break;

        case 1:
            pEspPlayer.espLine = !pEspPlayer.espLine;
            SendFeatuere(3, pEspPlayer.espLine || pEspPlayer.espBox || pEspPlayer.espNickName || pEspPlayer.espHealth);
            showNotification("ESP Line", pEspPlayer.espLine);
            break;

        case 2:
            pEspPlayer.espBox = !pEspPlayer.espBox;
            SendFeatuere(3, pEspPlayer.espLine || pEspPlayer.espBox || pEspPlayer.espNickName || pEspPlayer.espHealth);
            showNotification("ESP Box", pEspPlayer.espBox);
            break;

        case 3:
            pEspPlayer.espHealth = !pEspPlayer.espHealth;
            SendFeatuere(3, pEspPlayer.espLine || pEspPlayer.espBox || pEspPlayer.espNickName || pEspPlayer.espHealth);
            showNotification("ESP Health", pEspPlayer.espHealth);
            break;
        case 33333:
            pEspPlayer.DISC = !pEspPlayer.DISC;
            break;

        case 4:
            pEspPlayer.espNickName = !pEspPlayer.espNickName;
            SendFeatuere(3, pEspPlayer.espLine || pEspPlayer.espBox || pEspPlayer.espNickName || pEspPlayer.espHealth);
            showNotification("ESP Name", pEspPlayer.espNickName);
            break;

        case 5:
            if (value == 0) {
                pEspPlayer.espColor = Color::White();
            } else if (value == 1) {
                pEspPlayer.espColor = Color::Green();
            } else if (value == 2) {
                pEspPlayer.espColor = Color::Blue();
            } else if (value == 3) {
                pEspPlayer.espColor = Color::Red();
            } else if (value == 4) {
                pEspPlayer.espColor = Color::Black();
            } else if (value == 5) {
                pEspPlayer.espColor = Color::Yellow();
            } else if (value == 6) {
                pEspPlayer.espColor = Color::Cyan();
            } else if (value == 7) {
                pEspPlayer.espColor = Color::Magenta();
            } else if (value == 8) {
                pEspPlayer.espColor = Color::Gray();
            } else if (value == 9) {
                pEspPlayer.espColor = Color::Purple();
            }
            break;

        case 6:
            if (value == 0) {
                pEspPlayer.lineType = value;
            } else if (value == 1) {
                pEspPlayer.lineType = value;
            } else if (value == 2) {
                pEspPlayer.lineType = value;
            }
            break;

        case 7:
            if (value == 0) {
                pEspPlayer.boxType = value;
            } else if (value == 1) {
                pEspPlayer.boxType = value;
            } else if (value == 2) {
                pEspPlayer.boxType = value;
            }
            break;

        case -2:
            pMemoryTools.catapultDistance = !pMemoryTools.catapultDistance;
            SendFeatuere(5, pMemoryTools.catapultDistance);
            break;

        case 16:
            pEspPlayer.espDrawFov = !pEspPlayer.espDrawFov;
            showNotification("Show Fov", pEspPlayer.espDrawFov);
            break;

        case 9:
            pEspPlayer.espHealth = !pEspPlayer.espHealth;
            SendFeatuere(3, pEspPlayer.espLine || pEspPlayer.espBox || pEspPlayer.espNickName || pEspPlayer.espHealth);
            showNotification("ESP Health", pEspPlayer.espHealth);
            break;
        case 14:
            pEspPlayer.espTracker = !pEspPlayer.espTracker;
            break;
        case 144:
            pEspPlayer.espLineTracker = !pEspPlayer.espTracker;
            break;

        case 10:
            MasterBool.ultraswitch = !MasterBool.ultraswitch;
            SendFeatuere(10, MasterBool.ultraswitch);
            break;
        case 11:
            MasterBool.highjump = !MasterBool.highjump;
            SendFeatuere(11, MasterBool.highjump);
            break;
        case 12:
            MasterBool.resetguest = !MasterBool.resetguest;
            SendFeatuere(12, MasterBool.resetguest);
            break;

        case 13:
            MasterBool.medikitrun = !MasterBool.medikitrun;
            SendFeatuere(13, MasterBool.medikitrun);
            break;
        case 1111:
            MasterBool.cameraup = !MasterBool.cameraup;
            SendFeatuere(1111, MasterBool.cameraup);
            break;
        case 15:
            MasterBool.speedhackjoy = !MasterBool.speedhackjoy;
            SendFeatuere(15, MasterBool.speedhackjoy);
            break;
        case 17:
            MasterBool.doublegun = !MasterBool.doublegun;
            SendFeatuere(17, MasterBool.doublegun);
            break;
        case 166:
            MasterBool.wallHack = !MasterBool.wallHack;
            SendFeatuere(166, MasterBool.wallHack);
            break;
        case 19:
            MasterBool.telehack = !MasterBool.telehack;
            SendFeatuere(19, MasterBool.telehack);
            break;
        case 20:
            MasterBool.upplayerx = !MasterBool.upplayerx;
            SendFeatuere(20, MasterBool.upplayerx);
            showNotification("Up Player", MasterBool.upplayerx);
            break;
        case 21:
            MasterBool.aimbody = !MasterBool.aimbody;
            SendFeatuere(21, MasterBool.aimbody);
            break;
        case 22:
            MasterBool.AimSilent = !MasterBool.AimSilent;
            SendFeatuere(22, MasterBool.AimSilent);
            break;
        case 1056:
            MasterBool.AimSilent360 = !MasterBool.AimSilent360;
            SendFeatuere(1056, MasterBool.AimSilent360);
            break;
        case 500:
            MasterBool.Aimkilltp = !MasterBool.Aimkilltp;
            SendFeatuere(500, MasterBool.Aimkilltp);
            showNotification("Sniper Auto Aim", MasterBool.Aimkilltp);
            break;
        case 501:
            MasterBool.Aimkilltpv2 = !MasterBool.Aimkilltpv2;
            SendFeatuere(501, MasterBool.Aimkilltpv2);
            break;
        case 502:
            MasterBool.Aimkillrotate = !MasterBool.Aimkillrotate;
            SendFeatuere(502, MasterBool.Aimkillrotate);
            break;
        case 503:
            MasterBool.Aimkillrotatev2 = !MasterBool.Aimkillrotatev2;
            SendFeatuere(503, MasterBool.Aimkillrotatev2);
            break;
        case 504:
            MasterBool.downaimkill = !MasterBool.downaimkill;
            SendFeatuere(504, MasterBool.downaimkill);
            break;
        case 505:
            MasterBool.autoswitch = !MasterBool.autoswitch;
            SendFeatuere(505, MasterBool.autoswitch);
            break;

        case 506:
            MasterBool.Aimkillrotatev3 = !MasterBool.Aimkillrotatev3;
            SendFeatuere(506, MasterBool.Aimkillrotatev3);
            break;
        case 507:
            MasterBool.speedrun = !MasterBool.speedrun;
            SendFeatuere(507, MasterBool.speedrun);
            break;
        case 508:
            MasterBool.TeleBeta = !MasterBool.TeleBeta;
            SendFeatuere(508, MasterBool.TeleBeta);
            showNotification("Sniper Auto Aim", MasterBool.TeleBeta);
            break;
        case 509:
            MasterBool.climbup = !MasterBool.climbup;
            SendFeatuere(509, MasterBool.climbup);
            break;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ashu_Menu_OnDrawLoad(JNIEnv *env, jclass clazz, jobject draw_view, jobject canvas) {
    DrawView draw = DrawView(env, draw_view, canvas);




    if (draw.isValid()) {
        // Real-time FPS Calculation and Drawing in Bottom-Left Corner
        long long currentTime = getCurrentTimeMs();
        frameCount++;
        if (currentTime - lastFpsTime >= 1000) {
            fpsValue = frameCount * 1000.0f / (currentTime - lastFpsTime);
            frameCount = 0;
            lastFpsTime = currentTime;
        }

        char fpsText[32];
        sprintf(fpsText, "FPS- %.0f", fpsValue);
        Vector2 fpsPos(30.0f, (float)draw.getHeight() - 40.0f);
        draw.DrawText(Color(0, 0, 0, 200), fpsText, Vector2(fpsPos.X + 2.0f, fpsPos.Y + 2.0f), 30.0f);
        draw.DrawText(Color(255, 184, 0, 255), fpsText, fpsPos, 30.0f);

        if (pEspPlayer.espDrawFov) {
            // Draw a White circle with thicker line (4.0 thickness) at center of screen.
            // Radius scales dynamically with "Adjust Headshot Rate" (pAimbotPlayer.aimbotFOV: 0-100)
            float radius = 50.0f + (pAimbotPlayer.aimbotFOV * 4.0f);
            // Draw glowing outer layers in White
            draw.DrawCircle(Color(255, 255, 255, 35), 8.0f, Vector2(draw.getWidth() / 2, draw.getHeight() / 2), radius + 2.0f);
            draw.DrawCircle(Color(255, 255, 255, 75), 5.0f, Vector2(draw.getWidth() / 2, draw.getHeight() / 2), radius + 1.0f);
            // Main circle in White
            draw.DrawCircle(Color(255, 255, 255, 255), 4.0f, Vector2(draw.getWidth() / 2, draw.getHeight() / 2), radius);
        }

        // --- Premium Intro Animation ---
        if (showAnimation) {
            long long elapsed = getCurrentTimeMs() - animationStartTime;

            if (elapsed < 1200) {
                // Phase 1: Full black screen, center displays the skull logo
                draw.DrawBlackScreen(255);
                draw.DrawLogo(draw.getWidth() / 2, draw.getHeight() / 2, 180.0f, 180.0f, 255.0f);
            }
            else if (elapsed < 2400) {
                // Phase 2: Full black screen, logo disappears, bold golden text "JACK PANEL" at center
                draw.DrawBlackScreen(255);
                // Center bold text (size 70.0f)
                draw.DrawText(Color(0, 0, 0, 200), "JACK PANEL", Vector2(draw.getWidth() / 2 + 2, draw.getHeight() / 2 + 2), 70.0f);
                draw.DrawText(Color(255, 184, 0, 255), "JACK PANEL", Vector2(draw.getWidth() / 2, draw.getHeight() / 2), 70.0f);
            }
            else if (elapsed < 3800) {
                // Phase 3: Black screen fades to transparent, and "JACK PANEL" text slides from center to final top position
                float progress = (elapsed - 2400) / 1400.0f; // 0.0 to 1.0
                if (progress > 1.0f) progress = 1.0f;

                int screenAlpha = (int)(255 * (1.0f - progress));
                draw.DrawBlackScreen(screenAlpha);

                // Smoothly interpolate Y position from center to top (Y = 120)
                float startY = draw.getHeight() / 2.0f;
                float endY = 120.0f;
                float currentY = startY + (endY - startY) * progress;

                // Smoothly interpolate size from 70 to 45
                float currentSize = 70.0f + (45.0f - 70.0f) * progress;

                // Color interpolates to Golden
                Color currentColor = Color(255, 184, 0, 255);

                draw.DrawText(Color(0, 0, 0, 200), "JACK PANEL", Vector2(draw.getWidth() / 2 + 2, currentY + 2), currentSize);
                draw.DrawText(currentColor, "JACK PANEL", Vector2(draw.getWidth() / 2, currentY), currentSize);
            }
            else {
                // Animation finished!
                showAnimation = false;
            }
        }

        // Show "JACK PANEL" overlay when Activate All is ON and animation is not running
        if (pAimbotPlayer.enableAimbot && !showAnimation) {
            Vector2 welcomePos(draw.getWidth() / 2, 120);
            draw.DrawText(Color(0, 0, 0, 200), "JACK PANEL", Vector2(welcomePos.X + 2, welcomePos.Y + 2), 45.0f);
            draw.DrawText(Color(255, 184, 0, 255), "JACK PANEL", welcomePos, 45.0f);
        }

        if (pAimbotPlayer.enableAimbot) {
            Response response = getData(draw.getWidth(), draw.getHeight());

            if (response.Success) {
            for (int i = 0; i < response.PlayerCount; ++i) {
                PlayerData data = response.Players[i];

                Vector3 HeadLoc = data.headPosition;
                Vector3 PesLoc = data.bottomPlayerPosition;

                if (HeadLoc.Z < -1) continue;
                if (PesLoc.Z < -1) continue;

                float distance = data.distance;
                float health = data.health;
                bool IsCaido = data.isDieing;

                // Limit scale for ESP
                float scale = std::max(0.5f, std::min(1.0f, 500.0f / distance));

                // Calculate player box dimensions
                float boxHeight = abs(HeadLoc.Y - PesLoc.Y) * scale;
                float boxWidth = boxHeight * 0.50f;

                // Adjust position for head alignment
                Rect PlayerRect(HeadLoc.X - (boxWidth / 2), draw.getHeight() - HeadLoc.Y, boxWidth, boxHeight);

                if (pEspPlayer.espLine) {
                    Vector2 lineStart;
                    Vector2 lineEnd;

                    if (pEspPlayer.lineType == 0) {
                        lineStart = Vector2(draw.getWidth() / 2, 0);
                        lineEnd = Vector2(HeadLoc.X, draw.getHeight() - HeadLoc.Y);
                    } else if (pEspPlayer.lineType == 1) {
                        lineStart = Vector2(draw.getWidth() / 2, draw.getHeight() / 2);
                        lineEnd = Vector2(HeadLoc.X, draw.getHeight() - HeadLoc.Y);
                    } else if (pEspPlayer.lineType == 2) {
                        lineStart = Vector2(draw.getWidth() / 2, draw.getHeight());
                        lineEnd = Vector2(PesLoc.X, draw.getHeight() - PesLoc.Y);
                    }

                    if (IsCaido) {
                        draw.DrawLine(Color::Red(), 3.5f, lineStart, lineEnd);
                    } else {
                        draw.DrawLine(pEspPlayer.espColor, 3.5f, lineStart, lineEnd);
                    }
                }

                if (pEspPlayer.espBox) {
                    if (IsCaido) {
                        if (pEspPlayer.boxType == 0) {
                            // Draw red glowing border
                            Rect glowRect(PlayerRect.x - 1, PlayerRect.y - 1, PlayerRect.w + 2, PlayerRect.h + 2);
                            draw.DrawBox(Color(255, 0, 0, 45), 5.5f, glowRect);
                            draw.DrawBox(Color::Red(), 2.5f, PlayerRect);
                        } else if (pEspPlayer.boxType == 1) {
                            draw.DrawBox3D(Color::Red(), 2.5f, PlayerRect, 10);
                        } else if (pEspPlayer.boxType == 2) {
                            draw.DrawCornerBox(Color::Red(), 2.5f, PlayerRect, 4, 4);
                        }
                    } else {
                        if (pEspPlayer.boxType == 0) {
                            // Draw customizable color glowing border
                            Rect glowRect(PlayerRect.x - 1, PlayerRect.y - 1, PlayerRect.w + 2, PlayerRect.h + 2);
                            Color glowColor = pEspPlayer.espColor;
                            glowColor.a = 45;
                            draw.DrawBox(glowColor, 5.5f, glowRect);
                            draw.DrawBox(pEspPlayer.espColor, 2.5f, PlayerRect);
                        } else if (pEspPlayer.boxType == 1) {
                            draw.DrawBox3D(pEspPlayer.espColor, 2.5f, PlayerRect, 10);
                        } else if (pEspPlayer.boxType == 2) {
                            draw.DrawCornerBox(pEspPlayer.espColor, 2.5f, PlayerRect, 4, 4);
                        }
                    }
                }

                // ======= Draw Nickname =======
                if(pEspPlayer.espNickName)
                {
                    if (!IsCaido) {

                        Vector2 namePos(HeadLoc.X, draw.getHeight() - HeadLoc.Y - 20);
                        std::string playerName = data.name;
                        draw.DrawTextWithShadow(pEspPlayer.espColor, playerName.c_str(), namePos, 16, Vector2(2, 2), 0.5f);

                    }
                }
                // ======= Draw Distance =======
                if (pEspPlayer.DISC) {
                    float centerX = draw.getWidth() / 2.0f;
                    float centerY = draw.getHeight() / 2.0f;

                    if (!IsCaido) {
                        Vector2 namePos(PlayerRect.x + (PlayerRect.w / 2), PlayerRect.y - (5.0f * scale));
                        namePos.X -= (strlen(data.name) * 2.5f * scale);
                        float textSize = 12.0f * scale;
                        Vector2 shadowOffset(1.0f, 1.0f);
                        char distanceText[32];
                        sprintf(distanceText, "%dm", static_cast<int>(data.distance));

                        // Calculate centered position for distance text
                        float textWidth = strlen(distanceText) * 6.0f * scale; // Approximate text width
                        Vector2 distancePos(
                                PlayerRect.x + (PlayerRect.w / 2) - (textWidth / 2),
                                PlayerRect.y + PlayerRect.h + (18.0f * scale) // Increased from 12.0f to 18.0f to move it further down
                        );

                        // Draw black border
                        draw.DrawTextWithShadow(Color(0, 0, 0, 255), distanceText, Vector2(distancePos.X - 1, distancePos.Y), textSize, shadowOffset, 2.0f);
                        draw.DrawTextWithShadow(Color(0, 0, 0, 255), distanceText, Vector2(distancePos.X + 1, distancePos.Y), textSize, shadowOffset, 2.0f);
                        draw.DrawTextWithShadow(Color(0, 0, 0, 255), distanceText, Vector2(distancePos.X, distancePos.Y - 1), textSize, shadowOffset, 2.0f);
                        draw.DrawTextWithShadow(Color(0, 0, 0, 255), distanceText, Vector2(distancePos.X, distancePos.Y + 1), textSize, shadowOffset, 2.0f);

                        // Draw main text
                        draw.DrawTextWithShadow(Color(255, 255, 255, 255), distanceText, distancePos, textSize, shadowOffset, 2.0f);
                    }
                }





                // ======= Health Bar =======
                if (pEspPlayer.espHealth && !IsCaido) {
                    Vector2 healthBarPos(PlayerRect.x - 5.0f * scale, PlayerRect.y);
                    float healthBarHeight = boxHeight;
                    draw.DrawVerticalHealthBar(healthBarPos, healthBarHeight, 200.0f, data.health);
                }
            }
        } else {
            // Draw simulated mock players for testing preview when not connected to daemon
            int simulatedCount = 2;
            for (int i = 0; i < simulatedCount; ++i) {
                float headX, headY, bottomX, bottomY, distance, health;
                const char* name;

                if (i == 0) {
                    headX = draw.getWidth() * 0.70f;
                    headY = draw.getHeight() * 0.40f;
                    bottomX = draw.getWidth() * 0.70f;
                    bottomY = draw.getHeight() * 0.65f;
                    distance = 45.0f;
                    health = 200.0f;
                    name = "Training BOT 1";
                } else {
                    headX = draw.getWidth() * 0.30f;
                    headY = draw.getHeight() * 0.30f;
                    bottomX = draw.getWidth() * 0.30f;
                    bottomY = draw.getHeight() * 0.75f;
                    distance = 15.0f;
                    health = 100.0f;
                    name = "Training BOT 2";
                }

                float scale = std::max(0.5f, std::min(1.0f, 500.0f / distance));
                float boxHeight = abs(headY - bottomY) * scale;
                float boxWidth = boxHeight * 0.50f;

                Rect PlayerRect(headX - (boxWidth / 2), headY, boxWidth, boxHeight);

                if (pEspPlayer.espLine) {
                    Vector2 lineStart;
                    Vector2 lineEnd(headX, headY);

                    if (pEspPlayer.lineType == 0) {
                        lineStart = Vector2(draw.getWidth() / 2, 0);
                    } else if (pEspPlayer.lineType == 1) {
                        lineStart = Vector2(draw.getWidth() / 2, draw.getHeight() / 2);
                    } else {
                        lineStart = Vector2(draw.getWidth() / 2, draw.getHeight());
                        lineEnd = Vector2(bottomX, bottomY);
                    }

                    draw.DrawLine(pEspPlayer.espColor, 3.5f, lineStart, lineEnd);
                }

                if (pEspPlayer.espBox) {
                    if (pEspPlayer.boxType == 0) {
                        // Draw customizable color glowing border
                        Rect glowRect(PlayerRect.x - 1, PlayerRect.y - 1, PlayerRect.w + 2, PlayerRect.h + 2);
                        Color glowColor = pEspPlayer.espColor;
                        glowColor.a = 45;
                        draw.DrawBox(glowColor, 5.5f, glowRect);
                        draw.DrawBox(pEspPlayer.espColor, 2.5f, PlayerRect);
                    } else if (pEspPlayer.boxType == 1) {
                        draw.DrawBox3D(pEspPlayer.espColor, 2.5f, PlayerRect, 10);
                    } else if (pEspPlayer.boxType == 2) {
                        draw.DrawCornerBox(pEspPlayer.espColor, 2.5f, PlayerRect, 4, 4);
                    }
                }

                if (pEspPlayer.espNickName) {
                    Vector2 namePos(headX, headY - 20);
                    draw.DrawTextWithShadow(pEspPlayer.espColor, name, namePos, 16, Vector2(2, 2), 0.5f);
                }

                if (pEspPlayer.espHealth) {
                    Vector2 healthBarPos(PlayerRect.x - 5.0f * scale, PlayerRect.y);
                    float healthBarHeight = boxHeight;
                    draw.DrawVerticalHealthBar(healthBarPos, healthBarHeight, 200.0f, health);
                }
            }
        }
    }


        // --- Premium Bottom-Right Notification Toast ---
        if (currentNotification.active) {
            long long elapsed = currentTime - currentNotification.startTime;
            if (elapsed < 2500) {
                int alpha = 255;
                if (elapsed < 300) {
                    alpha = (int)(255 * (elapsed / 300.0f));
                } else if (elapsed > 2200) {
                    alpha = (int)(255 * ((2500 - elapsed) / 300.0f));
                }
                if (alpha < 0) alpha = 0;
                if (alpha > 255) alpha = 255;

                float toastWidth = 360.0f;
                float toastHeight = 85.0f;
                float toastX = (float)draw.getWidth() - toastWidth - 30.0f;
                float toastY = (float)draw.getHeight() - toastHeight - 50.0f;

                if (elapsed < 300) {
                    float progress = elapsed / 300.0f;
                    toastX = (float)draw.getWidth() - (toastWidth + 30.0f) * progress;
                } else if (elapsed > 2200) {
                    float progress = (2500 - elapsed) / 300.0f;
                    toastX = (float)draw.getWidth() - (toastWidth + 30.0f) * progress;
                }

                // 1. Draw card background (semi-transparent dark)
                draw.DrawFilledRectinfo(Color(20, 20, 20, (int)(alpha * 0.92f)), Rect(toastX, toastY, toastWidth, toastHeight));

                // 2. Draw card outline box (thin red border for gaming aesthetic)
                Color outlineColor = Color(204, 0, 0, alpha);
                draw.DrawBox(outlineColor, 1.5f, Rect(toastX, toastY, toastWidth, toastHeight));

                // 3. Draw thick left accent stripe
                draw.DrawFilledRectinfo(outlineColor, Rect(toastX, toastY, 6.0f, toastHeight));

                // 4. Draw Bell Emoji 🔔 on the left
                draw.DrawText(Color(255, 255, 255, alpha), "🔔", Vector2(toastX + 35.0f, toastY + 53.0f), 28.0f);

                // 5. Draw Title: "SYSTEM ALERT" in bold red (left-aligned)
                draw.DrawTextLeft(Color(0, 0, 0, (int)(alpha * 0.8f)), "SYSTEM ALERT", Vector2(toastX + 70.0f + 1.0f, toastY + 28.0f + 1.0f), 12.0f);
                draw.DrawTextLeft(outlineColor, "SYSTEM ALERT", Vector2(toastX + 70.0f, toastY + 28.0f), 12.0f);

                // 6. Draw Status Message: "[Feature] Enabled/Disabled" (left-aligned)
                char statusText[96];
                if (currentNotification.enabled) {
                    sprintf(statusText, "%s Enabled", currentNotification.name);
                } else {
                    sprintf(statusText, "%s Disabled", currentNotification.name);
                }

                Color textColor = currentNotification.enabled ? Color::Green() : Color::Red();
                textColor.a = alpha;

                draw.DrawTextLeft(Color(0, 0, 0, (int)(alpha * 0.8f)), statusText, Vector2(toastX + 70.0f + 1.0f, toastY + 58.0f + 1.0f), 18.0f);
                draw.DrawTextLeft(textColor, statusText, Vector2(toastX + 70.0f, toastY + 58.0f), 18.0f);
            } else {
                currentNotification.active = false;
            }
        }

        // ESP Line Tracker and Name Tracker removed as per user request

    }
}
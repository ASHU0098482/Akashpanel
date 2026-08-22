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
                ShowErrorToast(env, "MOBILE PANEL ALWAYS ON TOP ✅.   ");
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
                // Phase 2: Full black screen, logo disappears, bold purple text "MOBILE PANEL" at center
                draw.DrawBlackScreen(255);
                // Center bold text (size 70.0f)
                draw.DrawText(Color(0, 0, 0, 200), "MOBILE PANEL", Vector2(draw.getWidth() / 2 + 2, draw.getHeight() / 2 + 2), 70.0f);
                draw.DrawText(Color(168, 85, 247, 255), "MOBILE PANEL", Vector2(draw.getWidth() / 2, draw.getHeight() / 2), 70.0f);
            }
            else if (elapsed < 3800) {
                // Phase 3: Black screen fades to transparent, and "MOBILE PANEL" text slides from center to final top position
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

                // Color is Vibrant Purple
                Color currentColor = Color(168, 85, 247, 255);

                draw.DrawText(Color(0, 0, 0, 200), "MOBILE PANEL", Vector2(draw.getWidth() / 2 + 2, currentY + 2), currentSize);
                draw.DrawText(currentColor, "MOBILE PANEL", Vector2(draw.getWidth() / 2, currentY), currentSize);
            }
            else {
                // Animation finished!
                showAnimation = false;
            }
        }

        // Show "MOBILE PANEL" overlay when Activate All is ON and animation is not running
        if (pAimbotPlayer.enableAimbot && !showAnimation) {
            Vector2 welcomePos(draw.getWidth() / 2, 120);
            draw.DrawText(Color(0, 0, 0, 200), "MOBILE PANEL", Vector2(welcomePos.X + 2, welcomePos.Y + 2), 45.0f);
            draw.DrawText(Color(168, 85, 247, 255), "MOBILE PANEL", welcomePos, 45.0f);
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
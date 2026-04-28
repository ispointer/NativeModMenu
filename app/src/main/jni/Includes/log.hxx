/*
 * Created by aantik
 * 4/27/2026
 *
 *   ⋆    ႔ ႔
 *     ᠸ^ ^ ⸝⸝
 *       |、˜〵
 *      じしˍ,)⁐̤ᐷ
 *
 * Fox Mode 🍺
 */


#include <jni.h>
#include <android/log.h>

enum LogType {
    oDEBUG = 3,
    oERROR = 6,
    oINFO  = 4,
    oWARN  = 5
};

#define TAG OBFUSCATE("Mod_Menu")

#define LOGD(...) ((void)__android_log_print(oDEBUG, TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(oERROR, TAG, __VA_ARGS__))
#define LOGI(...) ((void)__android_log_print(oINFO,  TAG, __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(oWARN,  TAG, __VA_ARGS__))


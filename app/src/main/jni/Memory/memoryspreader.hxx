#ifndef EXTERNALLGL_MEMORYSPREADER_HXX
#define EXTERNALLGL_MEMORYSPREADER_HXX


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


#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <sys/system_properties.h>

inline jobject cxxx(JNIEnv *env) {
    jclass activityThread = env->FindClass(OBFUSCATE("android/app/ActivityThread"));
    jmethodID currentActivityThread = env->GetStaticMethodID(activityThread,OBFUSCATE("currentActivityThread"),OBFUSCATE("()Landroid/app/ActivityThread;"));
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);
    jmethodID getApplication = env->GetMethodID(activityThread,OBFUSCATE("getApplication"),OBFUSCATE("()Landroid/app/Application;"));
    return env->CallObjectMethod(at, getApplication);
}

inline bool extractAsset(JNIEnv *env, jobject context, const char* assetName, const char* outPath) {
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getAssetsMethod = env->GetMethodID(contextClass,OBFUSCATE("getAssets"),OBFUSCATE("()Landroid/content/res/AssetManager;"));
    jobject assetManagerObj = env->CallObjectMethod(context, getAssetsMethod);
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManagerObj);
    AAsset* asset = AAssetManager_open(mgr, assetName, AASSET_MODE_BUFFER);
    if (asset == nullptr) {
        LOGE("Missing asset: %s", assetName);
        return false;
    }

    const size_t size = AAsset_getLength(asset);
    void* buffer = malloc(size);
    AAsset_read(asset, buffer, size);
    AAsset_close(asset);

    FILE* f = fopen(outPath, "wb");
    if (f == nullptr) {
        LOGE("Failed to open output path: %s", outPath);
        free(buffer);
        return false;
    }

    fwrite(buffer, 1, size, f);
    fclose(f);
    free(buffer);
    return true;
}

inline void runServerDaemon(JNIEnv *env) {
    jobject context = cxxx(env);
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getFilesDir = env->GetMethodID(contextClass,OBFUSCATE("getFilesDir"),OBFUSCATE("()Ljava/io/File;"));
    jobject fileObj = env->CallObjectMethod(context, getFilesDir);
    jclass fileClass = env->FindClass(OBFUSCATE("java/io/File"));
    jmethodID getAbsolutePath = env->GetMethodID(fileClass,OBFUSCATE("getAbsolutePath"),OBFUSCATE("()Ljava/lang/String;"));
    jstring pathStr = (jstring)env->CallObjectMethod(fileObj, getAbsolutePath);
    const char* filesPath = env->GetStringUTFChars(pathStr, 0);
    char localPath[512];
    sprintf(localPath, "%s/server_lgl", filesPath);
    char rmCmd[600];
    sprintf(rmCmd, "su -c \"rm %s\"", localPath);
    system(rmCmd);

    char abi[PROP_VALUE_MAX] = {0};
    __system_property_get("ro.product.cpu.abi", abi);
    LOGD("Device primary ABI: %s", abi);

    bool extracted = false;
    if (strstr(abi, "arm64-v8a")) {
        extracted = extractAsset(env, context, "server_arm64", localPath);
    } else if (strstr(abi, "x86_64")) {
        extracted = extractAsset(env, context, "server_x86_64", localPath);
    } else if (strstr(abi, "x86")) {
        extracted = extractAsset(env, context, "server_x86", localPath);
    } else {
        extracted = extractAsset(env, context, "server_arm", localPath);
    }

    env->ReleaseStringUTFChars(pathStr, filesPath);
    if (!extracted) {
        LOGE("No packaged server executable matched ABI: %s", abi);
        return;
    }

    char cmd[1024];
    system("su -c setenforce 0");
    system("su -c pkill -f server_lgl");
    sprintf(cmd, "su -c \"chmod 777 %s; %s > /dev/null 2>&1 &\"", localPath, localPath);
    system(cmd);
}

#endif // EXTERNALLGL_MEMORYSPREADER_HXX

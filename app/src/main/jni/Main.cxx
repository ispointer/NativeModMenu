#include <list>
#include <vector>
#include <string.h>
#include <pthread.h>
#include <thread>
#include <cstring>
#include <jni.h>
#include <unistd.h>
#include <fstream>
#include <iostream>
#include <dlfcn.h>
#include <cstdio>
#include <cstdlib>
#include "Includes/log.hxx"
#include "Includes/obfuscate.h"


/**
 * Hill Climb Racing
 * 1.68.1
 * com.fingersoft.hillclimb
 */
#define target_lib "libgame.so"
#define target_pkg "com.fingersoft.hillclimb"

#include "Memory/memoryspreader.hxx"
#include "Memory/macro.hxx"
#include "Menu/Setup.hxx"

/**
 * @example of all hooks
 */

void coinhook();
void coinhook() {
    const int read = Read<int>(0x80C5B0);
    LOGD("Read value ->  %d", read);
}
void coinhookwrite(int coin);
void coinhookwrite(int coin) {
    const bool write = Write<int>(0x80C5B0, coin);
    LOGD("Write value -> (%d)", coin);
}

jobjectArray GetFeatureList(JNIEnv *env, jobject context) {
    static bool serverStarted = false;
    if (!serverStarted) {
        runServerDaemon(env);
        serverStarted = true;
    }

    jobjectArray ret;
    const char *features[] = {
            OBFUSCATE("Category_External Cheat"),
            OBFUSCATE("1_Button_Read Value"),
            OBFUSCATE("2_InputValue_Write Value")
    };

    int Total_Feature = RevDex(features);
    ret = env->NewObjectArray(Total_Feature,env->FindClass(OBFUSCATE("java/lang/String")),env->NewStringUTF(""));

    for (int i = 0; i < Total_Feature; i++) {
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(features[i]));
    }

    return ret;
}

void Changes(JNIEnv *env, jclass clazz, jobject obj,
             jint featNum, jstring featName, jint value,
             jboolean boolean, jstring str) {

    switch (featNum) {
        case 1:
            coinhook();
            break;
        case 2:
            coinhookwrite(value);
            break;
    }
}
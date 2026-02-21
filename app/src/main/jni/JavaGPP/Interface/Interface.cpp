//
// Created by aantik on 2/1/2026.
//
// Interface.cpp

#include "Interface.h"
#include "OreoOrMore.h"
#include <cctype>

JavaVM *antik = nullptr;
JNIEnv *antikYt = nullptr;


namespace InterfaceMethods {
    void *Icon = nullptr;
    void *IconWebViewData = nullptr;
    void *getFeatureList = nullptr;
    void *settingsList = nullptr;
    void *Changes = nullptr;
    void *setTitleText = nullptr;
    void *setHeadingText = nullptr;
};
jclass menuClass;


static jobjectArray HEX(JNIEnv* env, const std::string& hex) {
    int len = hex.length() / 2;
    jbyteArray arr = env->NewByteArray(len);
    jbyte* buf = env->GetByteArrayElements(arr, nullptr);
    for (int i = 0; i < len; i++) {
        char h = hex[i * 2];
        char l = hex[i * 2 + 1];
        int hi = isdigit(h) ? h - '0' : tolower(h) - 'a' + 10;
        int lo = isdigit(l) ? l - '0' : tolower(l) - 'a' + 10;
        buf[i] = (jbyte)((hi << 4) | lo);
    }
    env->ReleaseByteArrayElements(arr, buf, 0);
    jclass bbCls = env->FindClass("java/nio/ByteBuffer");
    jmethodID wrap = env->GetStaticMethodID(bbCls,"wrap","([B)Ljava/nio/ByteBuffer;");
    jobject bb = env->CallStaticObjectMethod(bbCls, wrap, arr);
    return env->NewObjectArray(1, bbCls, bb);
}

static void RegisterMethods(JNIEnv *env) {

    JNINativeMethod NativeMethodsClassMethods[] = {
            {"Icon", "()Ljava/lang/String;", InterfaceMethods::Icon},
            {"IconWebViewData", "()Ljava/lang/String;", InterfaceMethods::IconWebViewData},
            {"getFeatureList", "()[Ljava/lang/String;", InterfaceMethods::getFeatureList},
            {"settingsList", "()[Ljava/lang/String;", InterfaceMethods::settingsList},
            {"Changes", "(Landroid/content/Context;ILjava/lang/String;IZLjava/lang/String;)V", InterfaceMethods::Changes},
            {"setTitleText", "(Landroid/widget/TextView;)V", InterfaceMethods::setTitleText},
            {"setHeadingText", "(Landroid/widget/TextView;)V", InterfaceMethods::setHeadingText},
    };

    env->RegisterNatives(menuClass, NativeMethodsClassMethods,sizeof(NativeMethodsClassMethods) / sizeof(NativeMethodsClassMethods[0]));
}
static void loadDex(JNIEnv* env, jobject context) {
    jobjectArray buffers = HEX(env, OreoOrMore);
    if (!buffers) return;
    jclass classLoaderClass = env->FindClass("java/lang/ClassLoader");
    jmethodID getSystemClassLoader = env->GetStaticMethodID(classLoaderClass,"getSystemClassLoader","()Ljava/lang/ClassLoader;");
    jobject parentCl = env->CallStaticObjectMethod(classLoaderClass, getSystemClassLoader);
    jclass imCls = env->FindClass("dalvik/system/InMemoryDexClassLoader");
    jmethodID ctor = env->GetMethodID(imCls,"<init>","([Ljava/nio/ByteBuffer;Ljava/lang/ClassLoader;)V");
    jobject dexLoader = env->NewObject(imCls, ctor, buffers, parentCl);
    jmethodID loadClass = env->GetMethodID(imCls,"loadClass","(Ljava/lang/String;)Ljava/lang/Class;");
    jstring clsName = env->NewStringUTF("uk.lgl.modmenu.FloatingModMenu");
    menuClass = (jclass) env->CallObjectMethod(dexLoader, loadClass, clsName);
    if (!menuClass) return;
    menuClass = (jclass) env->NewGlobalRef(menuClass);
    RegisterMethods(env);
    jmethodID antikMethod = env->GetStaticMethodID(menuClass,"CreateMenu","(Landroid/content/Context;)V");
    if (!antikMethod) return;
    env->CallStaticVoidMethod(menuClass, antikMethod, context);
}

void binJava() {
    if (antik->AttachCurrentThread(&antikYt, nullptr) != JNI_OK) return;
    jclass atCls = antikYt->FindClass("android/app/ActivityThread");
    jmethodID curApp = antikYt->GetStaticMethodID(atCls,"currentApplication","()Landroid/app/Application;");
    jobject app = antikYt->CallStaticObjectMethod(atCls, curApp);
    if (!app) return;
    loadDex(antikYt, app);
}



//
// Created by aantik on 2/1/2026.
//
// Interface.h
#ifndef NATIVE_LGL_INTERFACE_H
#define NATIVE_LGL_INTERFACE_H


#include <jni.h>
#include <string>

extern JavaVM *antik;
extern JNIEnv *antikYt;

namespace InterfaceMethods {
    extern void *Icon;
    extern void *IconWebViewData;
    extern void *getFeatureList;
    extern void *settingsList;
    extern void *Changes;
    extern void *setTitleText;
    extern void *setHeadingText;
}

void binJava();


#endif //NATIVE_LGL_INTERFACE_H

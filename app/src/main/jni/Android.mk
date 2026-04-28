LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := mem
LOCAL_CFLAGS := -w -s -Wno-error=format-security -fvisibility=hidden -fpermissive -fexceptions
LOCAL_CPPFLAGS := -w -s -Wno-error=format-security -fvisibility=hidden -Werror -std=c++17
LOCAL_CPPFLAGS += -Wno-error=c++11-narrowing -fpermissive -Wall -fexceptions
LOCAL_LDFLAGS += -Wl,--gc-sections,--strip-all,-llog
LOCAL_LDLIBS := -llog -landroid -lEGL -lGLESv2
LOCAL_ARM_MODE := arm
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_SRC_FILES := Main.cxx \
                   Socket/socket.cxx \
                   Socket/Memory/Memory.cxx
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := server
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_CPPFLAGS := -std=c++17
LOCAL_LDLIBS := -llog
LOCAL_SRC_FILES := Socket/main.cxx \
                   Socket/socket.cxx \
                   Socket/Memory/Memory.cxx
include $(BUILD_EXECUTABLE)

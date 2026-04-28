#ifndef EXTERNALLGL_MACRO_HXX
#define EXTERNALLGL_MACRO_HXX

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

#include <stdint.h>
#include "Socket/socket.hxx"


template<typename T>
inline T Read(uintptr_t offset) {
    T result{};
    runClientRaw(target_pkg, target_lib, offset, MODE_READ, &result, sizeof(T));
    return result;
}

template<typename T>
inline bool Write(uintptr_t offset, T value) {
    return runClientRaw(target_pkg, target_lib, offset, MODE_WRITE, &value, sizeof(T));
}

#endif // EXTERNALLGL_MACRO_HXX

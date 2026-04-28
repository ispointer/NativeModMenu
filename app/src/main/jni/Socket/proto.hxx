#ifndef EXTERNALLGL_PROTO_HXX
#define EXTERNALLGL_PROTO_HXX

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
#include <stddef.h>

#define MODE_READ  0
#define MODE_WRITE 1

constexpr size_t datapack = 128;

struct Request {
    char package[64];
    char lib[64];
    uint64_t offset;
    uint32_t mode;
    uint32_t size;
    uint8_t data[datapack];
};

struct Response {
    uint32_t success;
    uint8_t data[datapack];
};

#endif //EXTERNALLGL_PROTO_HXX

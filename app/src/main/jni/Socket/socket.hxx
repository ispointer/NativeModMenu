#ifndef EXTERNALLGL_SOCKET_HXX
#define EXTERNALLGL_SOCKET_HXX

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

#include <stddef.h>
#include <stdint.h>
#include "proto.hxx"

class Socket {
public:
    int sock;
    int client;

    Socket() : sock(-1), client(-1) {}

    bool startServer(int port);
    bool acceptClient();
    bool connectServer(const char* ip, int port);

    bool sendAll(int fd, const void* data, size_t size);
    bool recvAll(int fd, void* data, size_t size);

    void closeAll();
};

void runServer();
bool runClientRaw(const char* pkg, const char* lib, uintptr_t offset, int mode, void* buffer, size_t size);

#endif //EXTERNALLGL_SOCKET_HXX

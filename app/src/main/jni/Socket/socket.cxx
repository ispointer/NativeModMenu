#include "socket.hxx"
#include "Socket/Memory/Memory.hxx"

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

#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <sys/uio.h>
#include <sys/syscall.h>

namespace cfg {
    constexpr const char* IP   = "127.0.0.1";
    constexpr int PORT = 8080;
    constexpr int RETRIES = 20;
    constexpr useconds_t  DELAY  = 100000;
}

namespace util {
    inline bool validSize(size_t s) {
     return s > 0 && s <= datapack;
    }
}

bool Socket::startServer(int port) {
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) return false;
    int opt = 1;
    setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    addr.sin_addr.s_addr = INADDR_ANY;
    if (bind(sock, (sockaddr*)&addr, sizeof(addr)) != 0) return false;
    if (listen(sock, 5) != 0) return false;

    return true;
}

bool Socket::acceptClient() {
    client = accept(sock, nullptr, nullptr);
    return client >= 0;
}

bool Socket::connectServer(const char* ip, int port) {
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) return false;
    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_port   = htons(port);
    if (inet_pton(AF_INET, ip, &addr.sin_addr) != 1) return false;
    if (connect(sock, (sockaddr*)&addr, sizeof(addr)) != 0) return false;
    return true;
}

void Socket::closeAll() {
    if (client >= 0) { close(client); client = -1; }
    if (sock   >= 0) { close(sock);   sock   = -1; }
}

bool Socket::sendAll(int fd, const void* data, size_t size) {
    const char* p = (const char*)data;

    while (size) {
        ssize_t s = send(fd, p, size, 0);
        if (s <= 0) return false;
        p += s;
        size -= s;
    }
    return true;
}

bool Socket::recvAll(int fd, void* data, size_t size) {
    char* p = (char*)data;

    while (size) {
        ssize_t r = recv(fd, p, size, 0);
        if (r <= 0) return false;
        p += r;
        size -= r;
    }
    return true;
}

void runServer() {
    Socket s;
    if (!s.startServer(cfg::PORT)) return;

    while (true) {
        if (!s.acceptClient()) continue;
        Request req;
        Response res;
        if (!s.recvAll(s.client, &req, sizeof(req))) {
            s.closeAll();
            continue;
        }
        if (!util::validSize(req.size)) {
            s.sendAll(s.client, &res, sizeof(res));
            s.closeAll();
            continue;
        }
        int pid = Mem::GetPid(req.package);
        if (pid <= 0) {
            s.sendAll(s.client, &res, sizeof(res));
            s.closeAll();
            continue;
        }
        uintptr_t base = 0;
        if (req.lib[0]) base = Mem::GetBase(pid, req.lib);
        if (req.lib[0] && !base) {
            s.sendAll(s.client, &res, sizeof(res));
            s.closeAll();
            continue;
        }
        uintptr_t addr = base + req.offset;
        if (req.mode == MODE_READ) {
            struct iovec local  { res.data, (size_t)req.size };
            struct iovec remote { (void*)addr, (size_t)req.size };
            res.success = syscall(SYS_process_vm_readv, pid, &local, 1, &remote, 1, 0) == (ssize_t)req.size;
        }
        else if (req.mode == MODE_WRITE) {
            struct iovec local  { req.data, (size_t)req.size };
            struct iovec remote { (void*)addr, (size_t)req.size };
            res.success = syscall(SYS_process_vm_writev, pid, &local, 1, &remote, 1, 0) == (ssize_t)req.size;
        }
        s.sendAll(s.client, &res, sizeof(res));
        s.closeAll();
    }
}


bool runClientRaw(const char* pkg, const char* lib, uintptr_t offset, int mode, void* buffer, size_t size) {
    if (!pkg || !buffer || !util::validSize(size)) return false;
    Socket c;
    int retry = cfg::RETRIES;
    while (!c.connectServer(cfg::IP, cfg::PORT) && retry--) {
    usleep(cfg::DELAY);
    }
    if (retry <= 0) return false;
    Request req{};
    strncpy(req.package, pkg, sizeof(req.package) - 1);
    if (lib) strncpy(req.lib, lib, sizeof(req.lib) - 1);
    req.offset = offset;
    req.mode = mode;
    req.size = (uint32_t)size;
    if (mode == MODE_WRITE) {
    memcpy(req.data, buffer, size);
    }
    if (!c.sendAll(c.sock, &req, sizeof(req))) {
    c.closeAll();
    return false;
    }
    Response res{};
    if (!c.recvAll(c.sock, &res, sizeof(res))) {
    c.closeAll();
    return false;
    }
    if (res.success && mode == MODE_READ) {
    memcpy(buffer, res.data, size);
    }
    c.closeAll();
    return res.success;
}
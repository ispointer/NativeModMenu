#ifndef EXTERNALLGL_MEMORY_HXX
#define EXTERNALLGL_MEMORY_HXX

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
#include <sys/uio.h>
#include <unistd.h>
#include <sys/syscall.h>

#ifndef SYS_process_vm_readv
#if defined(__aarch64__)
    #define SYS_process_vm_readv 270
#elif defined(__arm__)
    #define SYS_process_vm_readv 376
#elif defined(__x86_64__)
    #define SYS_process_vm_readv 310
#elif defined(__i386__)
    #define SYS_process_vm_readv 347
#endif
#endif

#ifndef SYS_process_vm_writev
#if defined(__aarch64__)
    #define SYS_process_vm_writev 271
#elif defined(__arm__)
    #define SYS_process_vm_writev 377
#elif defined(__x86_64__)
    #define SYS_process_vm_writev 311
#elif defined(__i386__)
    #define SYS_process_vm_writev 348
#endif
#endif

namespace Mem {
    int GetPid(const char* pkg);
    uintptr_t GetBase(int pid, const char* lib);

    template<typename T>
    inline T Read(int pid, uintptr_t address) {
        T buf{};
        if (pid <= 0 || !address) return buf;
        struct iovec local = {&buf, sizeof(T)};
        struct iovec remote = {(void*)address, sizeof(T)};
        syscall(SYS_process_vm_readv, pid, &local, 1, &remote, 1, 0);
        return buf;
    }

    template<typename T>
    inline void Write(int pid, uintptr_t address, T value) {
        if (pid <= 0 || !address) return;
        struct iovec local = {&value, sizeof(T)};
        struct iovec remote = {(void*)address, sizeof(T)};
        syscall(SYS_process_vm_writev, pid, &local, 1, &remote, 1, 0);
    }
}

#endif //EXTERNALLGL_MEMORY_HXX

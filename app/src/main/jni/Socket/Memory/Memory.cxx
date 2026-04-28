#include "Memory.hxx"
#include "Includes/log.hxx"
#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <cerrno>
#include <unistd.h>

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


int Mem::GetPid(const char* pkg) {
    DIR* dir = opendir("/proc");
    if (!dir) return -1;
    struct dirent* entry;

    while ((entry = readdir(dir))) {
        int pid = atoi(entry->d_name);
        if (!pid) continue;

        char path[64], cmd[256];
        sprintf(path, "/proc/%d/cmdline", pid);

        FILE* f = fopen(path, "r");
        if (!f) continue;
        memset(cmd, 0, sizeof(cmd));
        if (fgets(cmd, sizeof(cmd), f)) {
            if (strstr(cmd, pkg) != nullptr) {
                fclose(f);
                closedir(dir);
                return pid;
            }
        }
        fclose(f);
    }
    closedir(dir);
    return -1;
}

uintptr_t Mem::GetBase(int pid, const char* lib) {
    char path[64], line[512];
    sprintf(path, "/proc/%d/maps", pid);
    FILE* f = fopen(path, "r");
    uintptr_t base = 0;
    while (fgets(line, sizeof(line), f)) {
        if (strstr(line, lib)) {
            uintptr_t addr = 0;
            uintptr_t offset = 0;
            if (sscanf(line, "%lx-%*x %*s %lx", &addr, &offset) == 2) {
                if (offset == 0 && base == 0) {
                    base = addr;
                }
            }
        }
    }
    fclose(f);
    return base;
}
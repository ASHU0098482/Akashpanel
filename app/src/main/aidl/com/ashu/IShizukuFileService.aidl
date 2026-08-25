package com.ashu;

interface IShizukuFileService {
    int replaceFiles(String sourceDir, String targetDir, String packageName, in String[] requiredPaths) = 1;
    void destroy() = 16777114;
}

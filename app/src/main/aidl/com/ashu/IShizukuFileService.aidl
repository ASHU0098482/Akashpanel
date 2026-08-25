package com.ashu;

interface IShizukuFileService {
    int replaceExistingFiles(String sourceDir, String packageName, in String[] requiredPaths) = 1;
    void destroy() = 16777114;
}

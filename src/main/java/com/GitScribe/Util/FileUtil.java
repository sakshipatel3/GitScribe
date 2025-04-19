package com.GitScribe.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static List<String> findJavaFiles(File root) {
        List<String> javaFiles = new ArrayList<>();
        findFilesRecursive(root, javaFiles);
        return javaFiles;
    }

    private static void findFilesRecursive(File dir, List<String> javaFiles) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                findFilesRecursive(file, javaFiles);
            } else if (file.getName().endsWith(".java")) {
                javaFiles.add(file.getAbsolutePath());
            }
        }
    }
}

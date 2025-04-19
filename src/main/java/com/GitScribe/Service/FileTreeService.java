package com.GitScribe.Service;

import org.springframework.stereotype.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileTreeService {

    /**
     * Lists all files in the repository directory recursively.
     * The returned file paths are relative to the repository root.
     *
     * @param repoDirPath the local repository directory.
     * @return a List of relative file paths.
     */
    public List<String> listFiles(String repoDirPath) {
        List<String> filePaths = new ArrayList<>();
        File root = new File(repoDirPath);
        if (root.exists() && root.isDirectory()) {
            listFilesRecursive(root, "", filePaths);
        }
        return filePaths;
    }

    private void listFilesRecursive(File dir, String relativePath, List<String> filePaths) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                listFilesRecursive(file, relativePath + file.getName() + "/", filePaths);
            } else {
                filePaths.add(relativePath + file.getName());
            }
        }
    }
}

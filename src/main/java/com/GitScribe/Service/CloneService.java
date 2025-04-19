package com.GitScribe.Service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;

@Service
public class CloneService {

    /**
     * Clones the repository from repoUrl.
     * If localDirPath is null or empty, generates a local directory name based on the repository name.
     *
     * @param repoUrl the URL of the repository to clone.
     * @param localDirPath (optional) local directory to use; if null or empty, one is generated.
     * @return the File representing the cloned repository directory.
     * @throws GitAPIException if the cloning fails.
     */
    public File cloneRepository(String repoUrl, String localDirPath) throws GitAPIException {
        // Trim whitespace from repoUrl
        repoUrl = repoUrl.trim();

        if (localDirPath == null || localDirPath.trim().isEmpty()) {
            localDirPath = generateLocalDirPath(repoUrl);
        }
        File localDir = new File(localDirPath);
        if (localDir.exists()) {
            deleteDirectory(localDir);
        }
        localDir.mkdirs();

        Git.cloneRepository()
            .setURI(repoUrl)
            .setDirectory(localDir)
            .call();

        return localDir;
    }

    /**
     * Generates a local directory path based on the repository name combined with the current timestamp.
     *
     * @param repoUrl the repository URL.
     * @return a generated local directory path.
     */
    private String generateLocalDirPath(String repoUrl) {
        String repoName = repoUrl.substring(repoUrl.lastIndexOf("/") + 1);
        if (repoName.endsWith(".git")) {
            repoName = repoName.substring(0, repoName.length() - 4);
        }
        // Use the current timestamp to ensure uniqueness.
        String uniqueFolder = repoName + "_" + System.currentTimeMillis();
        String tempDir = System.getProperty("java.io.tmpdir");
        return tempDir + File.separator + uniqueFolder;
    }

    /**
     * Recursively deletes a directory.
     *
     * @param dir the directory to delete.
     */
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if(files != null){
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
}

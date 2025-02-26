
package com.GitScribe.Util;

import org.eclipse.jgit.api.Git;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;

public class GitUtil {
    public static void cloneRepo(String repoUrl, String repoPath) throws GitAPIException {
        Git.cloneRepository()
            .setURI(repoUrl)
            .setDirectory(new File(repoPath))
            .call();
    }

    public static Iterable<RevCommit> getCommitHistory(String repoPath, String filePath) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(repoPath))) {
            return git.log().addPath(filePath).call();
        }
    }
}

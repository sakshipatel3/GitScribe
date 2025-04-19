package com.GitScribe.Service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GitHubService {

    private String localRepoPath;
    private Git git;

    /** (Re)initializes the local repo every time you set a new path. */
    public void setLocalRepoPath(String repoDirPath) throws IOException {
        this.localRepoPath = repoDirPath;
        File gitDir = new File(repoDirPath, ".git");
        Repository repo = new FileRepositoryBuilder()
                .setGitDir(gitDir)
                .readEnvironment()
                .findGitDir()
                .build();
        // close old one if present
        if (this.git != null) {
            this.git.getRepository().close();
        }
        this.git = new Git(repo);
    }

    /** Read the working‐tree version of a file. */
    public String getFileContent(String filePath) throws IOException {
        Path full = Path.of(localRepoPath, filePath);
        return Files.readString(full, StandardCharsets.UTF_8);
    }

    /**
     * Walks back from HEAD → oldest, but only records commits
     * where `filePath` was modified/renamed/copied.
     */
    public List<RevCommit> getCommitHistory(String filePath) throws IOException, GitAPIException {
        Repository repo = git.getRepository();
        RevWalk revWalk = new RevWalk(repo);
        List<RevCommit> history = new ArrayList<>();

        // start at HEAD
        ObjectId headId = repo.resolve(Constants.HEAD);
        RevCommit commit = revWalk.parseCommit(headId);
        String currentPath = filePath;

        while (commit != null) {
            // parse parent fully
            RevCommit parent = (commit.getParentCount() > 0)
                    ? revWalk.parseCommit(commit.getParent(0).getId())
                    : null;
            if (parent == null) break;

            // diff parent → commit
            try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                df.setRepository(repo);
                df.setDetectRenames(true);
                List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
                for (DiffEntry d : diffs) {
                    boolean touches = (d.getChangeType() == DiffEntry.ChangeType.MODIFY
                                   || d.getChangeType() == DiffEntry.ChangeType.RENAME
                                   || d.getChangeType() == DiffEntry.ChangeType.COPY)
                                   && d.getNewPath().equals(currentPath);
                    if (touches) {
                        history.add(commit);
                        // follow renames backwards
                        currentPath = d.getOldPath();
                        break;
                    }
                }
            }

            commit = parent;
        }

        revWalk.dispose();
        Collections.reverse(history);
        return history;
    }

    /**
     * Load a file’s contents at a specific commit SHA.
     * Returns empty string if the file wasn’t present.
     */
    public String getFileContentAtCommit(String filePath, String commitSha) throws IOException {
        Repository repo = git.getRepository();
        ObjectId commitId = repo.resolve(commitSha);
        try (RevWalk rw = new RevWalk(repo)) {
            RevCommit commit = rw.parseCommit(commitId);
            try (var treeWalk = new org.eclipse.jgit.treewalk.TreeWalk(repo)) {
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                treeWalk.setFilter(org.eclipse.jgit.treewalk.filter.PathFilter.create(filePath));
                if (!treeWalk.next()) return "";
                ObjectId objId = treeWalk.getObjectId(0);
                byte[] bytes = repo.open(objId).getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * List the paths of *all* files modified between two commits.
     * Useful for passing into your FileRenameDetector.
     */
    public List<String> getModifiedFilePathsBetween(RevCommit oldCommit, RevCommit newCommit) throws IOException {
        Repository repo = git.getRepository();
        try (ObjectReader reader = repo.newObjectReader();
             DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

            df.setRepository(repo);

            CanonicalTreeParser oldIter = new CanonicalTreeParser(null, reader, oldCommit.getTree());
            CanonicalTreeParser newIter = new CanonicalTreeParser(null, reader, newCommit.getTree());

            List<String> paths = new ArrayList<>();
            for (DiffEntry e : df.scan(oldIter, newIter)) {
                paths.add(e.getNewPath());
            }
            return paths;
        }
    }

    /** Clean up when you’re done. */
    public void close() {
        if (git != null) git.getRepository().close();
    }
}

package com.GitScribe.Service;

import com.GitScribe.Util.CommitInfo;
import com.GitScribe.Changes.FileRenameDetector;
import com.GitScribe.Changes.ParameterChangeDetector;
import com.GitScribe.Changes.ModifierChangeDetector;
import com.GitScribe.Changes.BodyChangeDetector;
import com.GitScribe.Changes.ReturnTypeChangeDetector;
import com.GitScribe.Changes.ExceptionsChangeDetector;

import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MethodHistoryService {

    @Autowired
    private GitHubService gitHubService;

    public List<MethodHistoryDTO> getAllMethodData(String repoDirPath, String filePath) throws Exception {
        gitHubService.setLocalRepoPath(repoDirPath);

        // load latest file
        String latestContent = gitHubService.getFileContent(filePath);
        if (latestContent == null || latestContent.isEmpty()) {
            throw new IOException("File content empty for: " + filePath);
        }
        List<MethodDeclarationData> methods = JDTParser.getMethods(latestContent);
        List<RevCommit> commits = gitHubService.getCommitHistory(filePath);

        List<MethodHistoryDTO> results = new ArrayList<>();
        for (MethodDeclarationData md : methods) {
            String methodName = md.getMethodDeclaration().getName().getIdentifier();
            String methodParams = md.getMethodDeclaration().parameters().toString();
            String oldDecl = md.getMethodDeclaration().toString();
            String oldBody = md.getBody();

            MethodHistoryDTO dto = new MethodHistoryDTO();
            dto.setMethodName(methodName);
            List<CommitInfo> history = new ArrayList<>();
            String currentPath = filePath;

            for (int i = 0; i < commits.size() - 1; i++) {
                RevCommit older = commits.get(i);
                RevCommit newer = commits.get(i + 1);
                // get modified files for this commit-pair
                List<String> modifiedFiles = gitHubService.getModifiedFilePathsBetween(older, newer);
                // load file before/after
                String oldContent = gitHubService.getFileContentAtCommit(currentPath, older.getName());
                String newContent = gitHubService.getFileContentAtCommit(currentPath, newer.getName());

                // extract old and new blocks
                String oldBlock = extractMethodBlockAccurate(oldContent, methodName, methodParams);
                String newBlock = extractMethodBlockAccurate(newContent, methodName, methodParams);

                // compute new declaration
                String newDecl = "";
                for (MethodDeclarationData cand : JDTParser.getMethods(newContent)) {
                    if (cand.getMethodDeclaration().getName().getIdentifier().equals(methodName)
                        && cand.getMethodDeclaration().parameters().toString().equals(methodParams)) {
                        newDecl = cand.getMethodDeclaration().toString();
                        break;
                    }
                }

                // 1) only detect rename if method no longer exists by declaration
                if (newDecl.isEmpty()) {
                    List<String> renameChanges = FileRenameDetector.detectFileRenameChangeTypes(
                        oldDecl, oldBody, modifiedFiles, gitHubService);
                    if (!renameChanges.isEmpty()) {
                        CommitInfo ci = new CommitInfo();
                        ci.setCommitId(newer.getName());
                        ci.setAuthor(newer.getAuthorIdent().getName());
                        ci.setAuthorEmail(newer.getAuthorIdent().getEmailAddress());
                        ci.setDate(newer.getAuthorIdent().getWhen().toString());
                        ci.setMessage(newer.getFullMessage());
                        ci.setChangeTypes(renameChanges);
                        history.add(ci);
                        // update path and skip
                        String newPath = FileRenameDetector.detectFileRename(
                            oldDecl, oldBody, modifiedFiles, gitHubService);
                        currentPath = newPath;
                        // reset baseline for next iterations
                        oldDecl = newDecl;
                        oldBody = newBlock;
                        continue;
                    }
                }

                // Gather other change types
                List<String> changes = new ArrayList<>();
                // parameter changes
                changes.addAll(ParameterChangeDetector.detectParameterChanges(oldDecl, newDecl));
                // return type changes
                changes.addAll(ReturnTypeChangeDetector.detectReturnTypeChange(oldBlock, newBlock));
                // modifier changes
                changes.addAll(ModifierChangeDetector.detectModifierChanges(oldBlock, newBlock));
                // body changes
                changes.addAll(BodyChangeDetector.detectBodyChange(oldBlock, newBlock));
                // exceptions (throws) changes
                changes.addAll(ExceptionsChangeDetector.detectExceptionsChange(oldDecl, newDecl));
                // parameter metadata changes
              

                // introduced or deleted
                if ((oldBody == null || oldBody.isEmpty()) && (newBlock != null && !newBlock.isEmpty())) {
                    changes.add("Introduced");
                }
                if ((oldBody != null && !oldBody.isEmpty()) && (newBlock == null || newBlock.isEmpty())) {
                    changes.add("Deleted");
                }

                // multi-change flag
                if (changes.size() > 1) {
                    changes.add("MultiChange");
                }

                // decide if recordable
                boolean isAddition = (oldBlock == null || oldBlock.isEmpty()) && (newBlock != null && !newBlock.isEmpty());
                boolean isModification = (oldBlock != null && !oldBlock.isEmpty()) && !changes.isEmpty();
                if (isAddition || isModification) {
                    CommitInfo ci = new CommitInfo();
                    ci.setCommitId(newer.getName());
                    ci.setAuthor(newer.getAuthorIdent().getName());
                    ci.setAuthorEmail(newer.getAuthorIdent().getEmailAddress());
                    ci.setDate(newer.getAuthorIdent().getWhen().toString());
                    ci.setMessage(newer.getFullMessage());
                    ci.setChangeTypes(changes);
                    history.add(ci);
                }

                // update baseline
                oldDecl = newDecl;
                oldBody = newBlock;
            }

            dto.setCommitHistory(history);
            results.add(dto);
        }
        return results;
    }

    private String extractMethodBlockAccurate(String fileContent, String methodName, String methodParams) {
        for (MethodDeclarationData m : JDTParser.getMethods(fileContent)) {
            String name = m.getMethodDeclaration().getName().getIdentifier();
            String params = m.getMethodDeclaration().parameters().toString();
            if (name.equals(methodName) && params.equals(methodParams)) {
                return m.getBody();
            }
        }
        return "";
    }
}


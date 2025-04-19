package com.GitScribe.Changes;

import com.GitScribe.Service.GitHubService;
import com.GitScribe.Service.JDTParser;
import com.GitScribe.Service.MethodDeclarationData;
import com.GitScribe.Util.ComprehensiveMethodChangeUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Detects if a method has been relocated due to a file rename in a given commit.
 */
public class FileRenameDetector {

    /**
     * Scans modified files for a matching method based on signature and body similarity.
     * Only considers .java files and skips unreadable paths.
     *
     * @param baselineBlock    The method block (code) from the previous commit.
     * @param targetMethodName The name of the target method.
     * @param modifiedFiles    List of file paths modified in the commit.
     * @param gitHubService    Service to fetch file contents.
     * @return The new file path containing the method, or null if not found.
     */
    public static String detectFileRename(
            String baselineBlock,
            String targetMethodName,
            List<String> modifiedFiles,
            GitHubService gitHubService
    ) {
        // extract method signature and body
        String baselineSignature = ComprehensiveMethodChangeUtil.extractSignature(baselineBlock);
        String baselineBody      = ComprehensiveMethodChangeUtil.extractBody(baselineBlock);
        if (baselineSignature.isEmpty() || baselineBody.isEmpty()) {
            return null;
        }

        for (String filePath : modifiedFiles) {
            // only inspect Java files
            if (!filePath.endsWith(".java")) {
                continue;
            }

            String fileContent;
            try {
                fileContent = gitHubService.getFileContent(filePath);
            } catch (IOException e) {
                // skip unreadable files
                continue;
            }
            if (fileContent.isEmpty()) {
                continue;
            }

            // parse methods in the candidate file
            List<MethodDeclarationData> methods = JDTParser.getMethods(fileContent);
            for (MethodDeclarationData md : methods) {
                String candSig  = ComprehensiveMethodChangeUtil.extractSignature(md.getBody());
                String candBody = ComprehensiveMethodChangeUtil.extractBody(md.getBody());

                // match signature exactly
                if (!candSig.equals(baselineSignature)) {
                    continue;
                }
                // check body similarity threshold (e.g., ≥ 0.50)
                double similarity = ComprehensiveMethodChangeUtil.jaroWinklerDistance(baselineBody, candBody);
                if (similarity >= 0.50) { // similarity of 70% of the same method in diff file
                    return filePath;
                }
            }
        }
        return null;
    }

    /**
     * Wrapper to expose rename detection as a change-type list.
     *
     * @return singleton list ["File Renamed: <newPath>"] if rename detected, else empty list.
     */
    public static List<String> detectFileRenameChangeTypes(
            String baselineBlock,
            String targetMethodName,
            List<String> modifiedFiles,
            GitHubService gitHubService
    ) {
        String newPath = detectFileRename(baselineBlock, targetMethodName, modifiedFiles, gitHubService);
        if (newPath != null) {
            return Collections.singletonList("File Renamed");
        }
        return Collections.emptyList();
    }
}

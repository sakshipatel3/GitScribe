package com.GitScribe.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.GitScribe.Service.GitHubService;
import com.GitScribe.Service.JDTParser;
import com.GitScribe.Service.MethodDeclarationData;

public class MethodMovementUtil {

    /**
     * Detects if a method has been moved to a different file in the given commit.
     * This version uses local file reading.
     *
     * It does so by examining all files modified in the commit (retrieved via getModifiedFilesForCommit).
     * For each modified file, it fetches file content using GitHubService configured in local mode,
     * then uses JDTParser to extract methods and compares the candidate method’s signature and body
     * with the baseline.
     *
     * If a method is found with the same signature (exact match) and a body that is at least 50% similar,
     * this function returns the new file path.
     *
     * @param baselineBlock    The baseline method block from the previous commit.
     * @param targetMethodName The name of the target method.
     * @param commitId         The commit ID to inspect.
     * @param gitHubService    An instance of GitHubService to perform local file reads.
     * @return The new file path if the method was moved; otherwise, null.
     * @throws IOException 
     */
    public static String detectMethodMovement(String baselineBlock, String targetMethodName,
                                                String commitId, GitHubService gitHubService) throws IOException {
        // Retrieve the list of files modified in this commit.
        List<String> modifiedFiles = getModifiedFilesForCommit(commitId);

        // Extract baseline signature and body.
        String baselineSignature = MethodChangeUtil.extractSignature(baselineBlock);
        String baselineBody = MethodChangeUtil.extractBody(baselineBlock);

        // Iterate through each modified file.
        for (String filePath : modifiedFiles) {
            // Get file content using local GitHubService.
            String fileContent = gitHubService.getFileContent(filePath);
            if (fileContent == null || fileContent.isEmpty()) {
                continue;
            }
            // Extract methods from the current file.
            List<MethodDeclarationData> methods = JDTParser.getMethods(fileContent);
            for (MethodDeclarationData md : methods) {
                // Compare signatures exactly.
                String candidateSignature = MethodChangeUtil.extractSignature(md.getBody());
                if (!candidateSignature.equals(baselineSignature)) {
                    continue;
                }
                // Compare method bodies using a simple similarity metric.
                String candidateBody = MethodChangeUtil.extractBody(md.getBody());
                double similarity = calculateSimilarity(baselineBody, candidateBody);
                if (similarity >= 0.50) { // At least 50% similar.
                    return filePath;  // Found a candidate indicating the method has moved.
                }
            }
        }
        return null;
    }

    /**
     * Stub: Returns a list of file paths modified in the given commit.
     * In a production system, replace this with actual Git diff logic.
     */
    private static List<String> getModifiedFilesForCommit(String commitId) {
        // TODO: Implement actual logic to retrieve modified files for the commit.
        return new ArrayList<>();
    }

    /**
     * A simple similarity metric based on common words.
     * Returns a value between 0.0 (no similarity) and 1.0 (exact match).
     */
    private static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");
        int common = 0;
        for (String w1 : words1) {
            for (String w2 : words2) {
                if (w1.equals(w2)) {
                    common++;
                    break;
                }
            }
        }
        double avg = (words1.length + words2.length) / 2.0;
        return avg > 0 ? common / avg : 0.0;
    }
}

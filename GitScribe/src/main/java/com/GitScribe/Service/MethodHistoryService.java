package com.GitScribe.Service;

import org.springframework.stereotype.Service;

import com.CommitInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.springframework.beans.factory.annotation.Autowired;



import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;




@Service
public class MethodHistoryService {

    @Autowired
    private GitHubService gitHubService;

    public List<String> getMethods(String repoOwner, String repoName, String filePath, String accessToken) throws IOException {
        String fileContentJson = gitHubService.getFileContent(repoOwner, repoName, filePath, accessToken);

        // Extract the content field from JSON and decode it from Base64
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(fileContentJson);
        String encodedContent = root.path("content").asText();

        // Handle newlines and replace any extraneous characters in the Base64 content
        encodedContent = encodedContent.replaceAll("\\n", "").replaceAll("\\\\n", "");

        // Base64 decode
        byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
        String fileContent = new String(decodedBytes);

        // Regular expression to find method definitions
        Pattern pattern = Pattern.compile("(public|protected|private|static|final|synchronized)?\\s*\\w+\\s+(\\w+)\\s*\\([^)]*\\)\\s*(\\{|;)");
        Matcher matcher = pattern.matcher(fileContent);

        List<String> methods = new ArrayList<>();
        while (matcher.find()) {
            String methodSignature = matcher.group();
            String methodName = matcher.group(2); // Extract method name
            methods.add(methodName);
        }

        return methods;
    }

    public List<CommitInfo> getMethodCommitHistory(String repoOwner, String repoName, String filePath, String accessToken, String methodName) throws IOException {
        List<CommitInfo> methodCommits = new ArrayList<>();

        // Fetch the initial file content
        String commitHistoryJson = gitHubService.getCommitHistory(repoOwner, repoName, filePath, accessToken);
        List<CommitInfo> commitHistory = gitHubService.parseCommitHistory(commitHistoryJson);

        String initialFileContentJson = gitHubService.getFileContentAtCommit(repoOwner, repoName, filePath, commitHistory.get(commitHistory.size() - 1).getCommitId(), accessToken);
        String initialFileContent = decodeBase64FileContent(initialFileContentJson);

        for (CommitInfo commit : commitHistory) {
            String currentFileContentJson = gitHubService.getFileContentAtCommit(repoOwner, repoName, filePath, commit.getCommitId(), accessToken);
            String currentFileContent = decodeBase64FileContent(currentFileContentJson);

            if (hasMethodChanged(initialFileContent, currentFileContent, methodName)) {
                methodCommits.add(commit);
            }

            initialFileContent = currentFileContent;
        }

        return methodCommits;
    }

    private String decodeBase64FileContent(String fileContentJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(fileContentJson);
        String encodedContent = root.path("content").asText();
        encodedContent = encodedContent.replaceAll("\\n", "").replaceAll("\\\\n", "");

        byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
        return new String(decodedBytes);
    }

    private boolean hasMethodChanged(String previousFileContent, String currentFileContent, String methodName) {
        // Extract method content from previous and current file content
        String previousMethodContent = extractMethodContent(previousFileContent, methodName);
        String currentMethodContent = extractMethodContent(currentFileContent, methodName);

        return !previousMethodContent.equals(currentMethodContent);
    }

    private String extractMethodContent(String fileContent, String methodName) {
        // Regular expression to find method content
        Pattern pattern = Pattern.compile("(public|protected|private|static|final|synchronized)?\\s*\\w+\\s+" + methodName + "\\s*\\([^)]*\\)\\s*\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(fileContent);

        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }
}

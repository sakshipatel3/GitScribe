package com.GitScribe.Service;

import com.CommitInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubService {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";

    public String getCommitHistory(String repoOwner, String repoName, String filePath, String accessToken) {
        String url = GITHUB_API_BASE_URL + "/repos/" + repoOwner + "/" + repoName + "/commits?path=" + filePath;

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch commit history from GitHub API.");
        }
    }

    public List<CommitInfo> parseCommitHistory(String commitHistoryJson) {
        List<CommitInfo> commitHistory = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(commitHistoryJson);
            
            for (JsonNode commitNode : root) {
                CommitInfo commitInfo = new CommitInfo();
                commitInfo.setCommitId(commitNode.path("sha").asText());
                commitInfo.setAuthor(commitNode.path("commit").path("author").path("name").asText());
                commitInfo.setAuthorEmail(commitNode.path("commit").path("author").path("email").asText());
                commitInfo.setDate(commitNode.path("commit").path("author").path("date").asText());
                commitInfo.setMessage(commitNode.path("commit").path("message").asText());
                commitInfo.setHtmlUrl(commitNode.path("html_url").asText());
                
                commitHistory.add(commitInfo);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return commitHistory;
    }

    public String getFileContent(String repoOwner, String repoName, String filePath, String accessToken) {
        String url = GITHUB_API_BASE_URL + "/repos/" + repoOwner + "/" + repoName + "/contents/" + filePath;

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch file content from GitHub API.");
        }
    }

    public String getFileContentAtCommit(String repoOwner, String repoName, String filePath, String commitSha, String accessToken) {
        String url = GITHUB_API_BASE_URL + "/repos/" + repoOwner + "/" + repoName + "/contents/" + filePath + "?ref=" + commitSha;

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch file content from GitHub API at commit.");
        }
    }
}

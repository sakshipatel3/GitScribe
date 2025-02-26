package com.GitScribe.Controller;

import com.GitScribe.Service.MethodHistoryService;
import com.GitScribe.Service.GitHubService;
import com.CommitInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;



@RestController
public class AnalysisController {
    @Autowired
    private MethodHistoryService methodHistoryService;

    @Autowired
    private GitHubService gitHubService;

    @GetMapping("/methods")
    public String getMethods(@RequestParam String repoOwner, @RequestParam String repoName, @RequestParam String filePath, @RequestParam String accessToken) {
        try {
            List<String> methods = methodHistoryService.getMethods(repoOwner, repoName, filePath, accessToken);
            StringBuilder result = new StringBuilder();
            result.append("<html><head><style>")
                  .append("body { font-family: Arial, sans-serif; background-color: #f0f0f5; margin: 0; padding: 20px; color: #333; }")
                  .append("h1 { color: #0044cc; }")
                  .append("ul { list-style-type: none; padding: 0; }")
                  .append("li { padding: 10px; margin-bottom: 5px; background-color: #e7f0fd; border: 1px solid #cce0fc; border-radius: 5px; }")
                  .append("li:hover { background-color: #d6e4fc; }")
                  .append("a { text-decoration: none; color: #0044cc; font-weight: bold; }")
                  .append("a:hover { text-decoration: underline; }")
                  .append("</style></head><body><h1>Method Names</h1><ul>");
            for (String method : methods) {
                String link = String.format("/analyze?repoOwner=%s&repoName=%s&filePath=%s&accessToken=%s&methodName=%s", 
                                            repoOwner, repoName, filePath, accessToken, method);
                result.append("<li><a href='").append(link).append("'>").append(method).append("</a></li>");
            }
            result.append("</ul></body></html>");
            return result.toString();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/analyze")
    public String analyzeMethodHistory(
            @RequestParam String repoOwner,
            @RequestParam String repoName,
            @RequestParam String filePath,
            @RequestParam String accessToken,
            @RequestParam String methodName) {
        try {
            System.out.println("Analyzing method history for: " + methodName);

            // Fetch the commit history for the specific method
            List<CommitInfo> methodCommits = methodHistoryService.getMethodCommitHistory(repoOwner, repoName, filePath, accessToken, methodName);
            System.out.println("Method commits: " + methodCommits);

            // Generate HTML response
            StringBuilder result = new StringBuilder();
            result.append("<html><head><style>")
                  .append("body { font-family: Arial, sans-serif; background-color: #f0f0f5; margin: 0; padding: 20px; color: #333; }")
                  .append("h1 { color: #0044cc; }")
                  .append(".commit { background-color: #ffffff; border: 1px solid #cce0fc; padding: 20px; margin-bottom: 20px; border-radius: 5px; }")
                  .append(".commit strong { color: #0044cc; }")
                  .append(".commit a { color: #007bff; text-decoration: none; }")
                  .append(".commit a:hover { text-decoration: underline; }")
                  .append("</style></head><body><h1>Commit History for Method: ").append(methodName).append("</h1>");

            for (CommitInfo commit : methodCommits) {
                result.append("<div class='commit'>")
                      .append("<strong>Commit ID:</strong> ").append(commit.getCommitId()).append("<br>")
                      .append("<strong>Author:</strong> ").append(commit.getAuthor()).append("<br>")
                      .append("<strong>Email:</strong> ").append(commit.getAuthorEmail()).append("<br>")
                      .append("<strong>Date:</strong> ").append(commit.getDate()).append("<br>")
                      .append("<strong>Message:</strong> ").append(commit.getMessage()).append("<br>")
                      .append("<strong>URL:</strong> <a href='").append(commit.getHtmlUrl()).append("'>View Commit</a><br>")
                      .append("</div>");
            }

            result.append("</body></html>");
            return result.toString();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return "Unexpected error: " + e.getMessage();
        }
    }
}

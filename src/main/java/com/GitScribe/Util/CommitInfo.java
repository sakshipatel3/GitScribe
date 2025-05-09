package com.GitScribe.Util;

import java.util.List;

public class CommitInfo {
    private String commitId;
    private String author;
    private String authorEmail;
    private String date;
    private String message;
    private String htmlUrl;
    private List<String> changeTypes; // new field

    // Getters and setters for all fields...
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }
    public List<String> getChangeTypes() { return changeTypes; }

    public void setChangeTypes(List<String> changeTypes) { this.changeTypes = changeTypes; }
}

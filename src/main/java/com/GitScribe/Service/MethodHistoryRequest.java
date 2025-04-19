package com.GitScribe.Service;

public class MethodHistoryRequest {
    private String repoDirPath;
    private String filePath;

    public String getRepoDirPath() {
        return repoDirPath;
    }

    public void setRepoDirPath(String repoDirPath) {
        this.repoDirPath = repoDirPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

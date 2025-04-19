package com.GitScribe.Service;

import java.util.List;
import com.GitScribe.Util.CommitInfo;

public class MethodHistoryDTO {
    private String methodName;
    private String methodSignature;
    private List<CommitInfo> commitHistory;

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public String getMethodSignature() { return methodSignature; }
    public void setMethodSignature(String methodSignature) { this.methodSignature = methodSignature; }

    public List<CommitInfo> getCommitHistory() { return commitHistory; }
    public void setCommitHistory(List<CommitInfo> commitHistory) { this.commitHistory = commitHistory; }
}

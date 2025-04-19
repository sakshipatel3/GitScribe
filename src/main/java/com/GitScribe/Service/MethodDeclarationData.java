
package com.GitScribe.Service;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodDeclarationData {
    private final MethodDeclaration methodDeclaration;
    private final String body;
    private final int startLine;
    private final int endLine;

    public MethodDeclarationData(MethodDeclaration methodDeclaration, String body, int startLine, int endLine) {
        this.methodDeclaration = methodDeclaration;
        this.body = body;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public MethodDeclaration getMethodDeclaration() {
        return methodDeclaration;
    }
    public String getBody() {
        return body;
    }
    public int getStartLine() {
        return startLine;
    }
    public int getEndLine() {
        return endLine;
    }
}
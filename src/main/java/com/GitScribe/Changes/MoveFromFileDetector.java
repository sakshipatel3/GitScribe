package com.GitScribe.Changes;

import com.GitScribe.Service.GitHubService;
import com.GitScribe.Service.JDTParser;
import com.GitScribe.Service.MethodDeclarationData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.GitScribe.Util.ComprehensiveMethodChangeUtil;

public class MoveFromFileDetector {

    /**
     * Returns ["MoveFromFile"] if the method signature+body exist in
     * any of the `modifiedFiles` (via rename detector), indicating it
     * moved away from this file.
     */
    public static List<String> detectMoveFromFileChangeTypes(
            String oldDecl,
            String oldBody,
            List<String> modifiedFiles,
            GitHubService gitHubService
    ) throws IOException {
        // reuse the existing file‑rename logic under the hood
        String newPath = FileRenameDetector.detectFileRename(
            oldDecl, oldBody, modifiedFiles, gitHubService
        );
        if (newPath != null) {
            return Collections.singletonList("MoveFromFile");
        }
        return Collections.emptyList();
    }
}

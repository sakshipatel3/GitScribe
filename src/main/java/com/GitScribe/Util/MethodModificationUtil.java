package com.GitScribe.Util;



import java.util.ArrayList;
import java.util.List;

import com.GitScribe.Service.JDTParser;
import com.GitScribe.Service.MethodDeclarationData;

public class MethodModificationUtil {

    /**
     * Detects modifications within the current file.
     * 
     * This method compares the baseline method's body (from the previous commit)
     * with the bodies of all methods in the current file. If any method's body is 
     * at least 75% similar to the baseline, we consider the method as modified 
     * within the file.
     *
     * @param baselineBlock   The method block from the previous commit.
     * @param fileContent     The entire file content from the current commit.
     * @param targetMethodName The name of the target method.
     * @return A list of change types indicating modifications.
     */
    public static List<String> detectModificationChanges(String baselineBlock, String fileContent, String targetMethodName) {
        List<String> changes = new ArrayList<>();
        
        // Extract the baseline method body.
        String baselineBody = MethodChangeUtil.extractBody(baselineBlock);
        if (baselineBody.isEmpty() || fileContent == null || fileContent.isEmpty()) {
            changes.add("Modified");
            return changes;
        }
        
        // Use JDTParser to extract all methods in the current file.
        List<MethodDeclarationData> methods = JDTParser.getMethods(fileContent);
        boolean foundSimilar = false;
        for (MethodDeclarationData md : methods) {
            // Compare only the method body.
            String currentBody = MethodChangeUtil.extractBody(md.getBody());
            double similarity = calculateSimilarity(baselineBody, currentBody);
            if (similarity >= 0.75) {  // 75% similarity threshold
                foundSimilar = true;
                break;
            }
        }
        
        if (foundSimilar) {
            changes.add("Modified within file");
        } else {
            changes.add("Modified");
        }
        
        return changes;
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

package com.GitScribe.Changes;

import java.util.ArrayList;
import java.util.List;

public class SignatureChangeDetector {

    public static String extractSignature(String methodBlock) {
        if (methodBlock == null) return "";
        int index = methodBlock.indexOf("{");
        if (index != -1) {
            return methodBlock.substring(0, index).trim();
        }
        return methodBlock.trim();
    }

    public static double jaroWinklerDistance(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int s1Len = s1.length();
        int s2Len = s2.length();
        if (s1Len == 0 || s2Len == 0) return 0.0;
        int matchDistance = Math.max(s1Len, s2Len) / 2 - 1;
        boolean[] s1Matches = new boolean[s1Len];
        boolean[] s2Matches = new boolean[s2Len];
        int matches = 0;
        for (int i = 0; i < s1Len; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, s2Len);
            for (int j = start; j < end; j++) {
                if (s2Matches[j]) continue;
                if (s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) return 0.0;
        double transpositions = 0;
        int k = 0;
        for (int i = 0; i < s1Len; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) {
                k++;
            }
            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }
            k++;
        }
        transpositions /= 2.0;
        double jaro = ((matches / (double) s1Len) +
                         (matches / (double) s2Len) +
                         ((matches - transpositions) / matches)) / 3.0;
        int prefix = 0;
        for (int i = 0; i < Math.min(4, Math.min(s1Len, s2Len)); i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                prefix++;
            } else {
                break;
            }
        }
        return jaro + prefix * 0.1 * (1 - jaro);
    }

    public static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    dp[i][j] = j;
                else if (j == 0)
                    dp[i][j] = i;
                else
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + 
                        (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
            }
        }
        return dp[s1.length()][s2.length()];
    }

    public static List<String> detectSignatureChange(String oldBlock, String newBlock) {
        List<String> changes = new ArrayList<>();
        String oldSignature = extractSignature(oldBlock);
        String newSignature = extractSignature(newBlock);

        // 1. Exact Match
        if (oldSignature.equals(newSignature)) {
            return changes; // No Change
        }

        // 2. Jaro-Winkler Similarity
        double similarity = jaroWinklerDistance(oldSignature, newSignature);

        if (similarity >= 0.95) {
            return changes; // No Change
        }

        // 3. For borderline cases use Levenshtein Distance
        if (similarity >= 0.85) {
            int distance = levenshteinDistance(oldSignature, newSignature);
            if (distance < 3) { // Very minor changes, skip
                return changes;
            }
        }

        changes.add("Signature Change");
        return changes;
    }
}

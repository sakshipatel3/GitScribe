package com.GitScribe.Changes;



import java.util.ArrayList;
import java.util.List;

public class BodyChangeDetector {

    public static String extractBody(String methodBlock) {
        if (methodBlock == null) return "";
        int open = methodBlock.indexOf("{");
        int close = methodBlock.lastIndexOf("}");
        if (open != -1 && close != -1 && close > open) {
            return methodBlock.substring(open + 1, close).trim();
        }
        return "";
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

    public static List<String> detectBodyChange(String oldBlock, String newBlock) {
        List<String> changes = new ArrayList<>();
        String oldBody = extractBody(oldBlock);
        String newBody = extractBody(newBlock);
        double similarity = jaroWinklerDistance(oldBody, newBody);
        if (similarity < 0.95) {
            changes.add("Body Change");
        }
        return changes;
    }
}

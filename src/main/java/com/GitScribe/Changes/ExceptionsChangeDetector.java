package com.GitScribe.Changes;

import java.util.ArrayList;
import java.util.List;

public class ExceptionsChangeDetector {

    /**
     * Compares two method declarations and returns ["Exceptions Change"]
     * if their throws‑clauses differ.
     *
     * @param oldDecl full old method declaration string
     * @param newDecl full new method declaration string
     */
    public static List<String> detectExceptionsChange(String oldDecl, String newDecl) {
        List<String> changes = new ArrayList<>();
        String oldThrows = extractThrows(oldDecl);
        String newThrows = extractThrows(newDecl);
        if (!oldThrows.equals(newThrows)) {
            changes.add("Exceptions Change");
        }
        return changes;
    }

    // Helper to pull out everything after 'throws' in the signature (or "" if none)
    private static String extractThrows(String decl) {
        String lower = decl.toLowerCase();
        int idx = lower.indexOf("throws");
        if (idx < 0) return "";
        return decl.substring(idx).trim();
    }
}

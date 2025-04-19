package com.GitScribe.Util;



import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class ComprehensiveMethodChangeUtil {

    /**
     * Parses the given source code (assumed to be a method block) into a MethodDeclaration.
     * To help the parser, the method block is wrapped in a dummy class.
     * Returns null if the source is empty or cannot be parsed.
     */
    public static MethodDeclaration parseMethodDeclaration(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        // Wrap the method in a dummy class to create a complete compilation unit.
        String wrappedSource = "public class Dummy { " + source + " }";
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(wrappedSource.toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        final MethodDeclaration[] result = new MethodDeclaration[1];
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                result[0] = node;
                return false; // Stop after finding the first method
            }
        });
        return result[0];
    }

    /**
     * Extracts the method signature (everything before the first '{') from a method block.
     */
    public static String extractSignature(String methodBlock) {
        if (methodBlock == null) return "";
        int index = methodBlock.indexOf("{");
        if (index != -1) {
            return methodBlock.substring(0, index).trim();
        }
        return methodBlock.trim();
    }

    /**
     * Extracts the method body (content within the outermost braces) from a method block.
     */
    public static String extractBody(String methodBlock) {
        if (methodBlock == null) return "";
        int open = methodBlock.indexOf("{");
        int close = methodBlock.lastIndexOf("}");
        if (open != -1 && close != -1 && close > open) {
            return methodBlock.substring(open + 1, close).trim();
        }
        return "";
    }

    /**
     * Computes the Jaro–Winkler distance between two strings.
     * Returns a value between 0.0 (no similarity) and 1.0 (exact match).
     */
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

    /**
     * Detects comprehensive changes between two method blocks.
     * It examines the following:
     *  - Method name differences ("Rename")
     *  - Parameter differences ("Parameter Change")
     *  - Modifier differences ("Modifier Change")
     *  - Annotation differences ("Annotation Change")
     *  - Signature differences (using Jaro–Winkler, threshold 0.95 for "Signature Change")
     *  - Body differences (using Jaro–Winkler, threshold 0.95 for "Body Change")
     *  - Return type differences ("Return Type Change")
     *
     * If no specific change is flagged but the texts differ, it returns "Minor Modification."
     *
     * @param oldBlock The method block from the previous commit.
     * @param newBlock The method block from the current commit.
     * @return A list of detected change types.
     */
    public static List<String> detectComprehensiveChanges(String oldBlock, String newBlock) {
        List<String> changes = new ArrayList<>();
        if (oldBlock == null || newBlock == null) {
            if (oldBlock != newBlock) {
                changes.add("Modified");
            }
            return changes;
        }

        // Normalize by removing whitespace and converting to lowercase.
        String normOld = oldBlock.replaceAll("\\s+", "").toLowerCase();
        String normNew = newBlock.replaceAll("\\s+", "").toLowerCase();
        if (normOld.equals(normNew)) {
            return changes; // No change detected.
        }

        MethodDeclaration oldMethod = parseMethodDeclaration(oldBlock);
        MethodDeclaration newMethod = parseMethodDeclaration(newBlock);

        if (oldMethod != null && newMethod != null) {
            // 1. Check method name.
            String oldName = oldMethod.getName().getIdentifier();
            String newName = newMethod.getName().getIdentifier();
            if (!oldName.equals(newName)) {
                changes.add("Rename");
            }

            // 2. Check parameter count.
            if (oldMethod.parameters().size() != newMethod.parameters().size()) {
                changes.add("Parameter Change");
            }

            // 3. Check modifiers.
            String oldModifiers = oldMethod.modifiers().toString();
            String newModifiers = newMethod.modifiers().toString();
            if (!oldModifiers.equals(newModifiers)) {
                changes.add("Modifier Change");
            }

            // 4. Check annotations.
            StringBuilder oldAnnotations = new StringBuilder();
            StringBuilder newAnnotations = new StringBuilder();
            for (Object o : oldMethod.modifiers()) {
                String s = o.toString();
                if (s.startsWith("@")) {
                    oldAnnotations.append(s);
                }
            }
            for (Object o : newMethod.modifiers()) {
                String s = o.toString();
                if (s.startsWith("@")) {
                    newAnnotations.append(s);
                }
            }
            if (!oldAnnotations.toString().equals(newAnnotations.toString())) {
                changes.add("Annotation Change");
            }

            // 5. Check signature similarity.
            String oldSignature = extractSignature(oldBlock);
            String newSignature = extractSignature(newBlock);
            double sigSim = jaroWinklerDistance(oldSignature, newSignature);
            if (sigSim < 0.95) {
                changes.add("Signature Change");
            }

            // 6. Check body similarity.
            String oldBody = extractBody(oldBlock);
            String newBody = extractBody(newBlock);
            double bodySim = jaroWinklerDistance(oldBody, newBody);
            if (bodySim < 0.95) {
                changes.add("Body Change");
            }

            // 7. Check return type.
            String oldReturn = (oldMethod.getReturnType2() != null) ? oldMethod.getReturnType2().toString() : "";
            String newReturn = (newMethod.getReturnType2() != null) ? newMethod.getReturnType2().toString() : "";
            if (!oldReturn.equals(newReturn)) {
                changes.add("Return Type Change");
            }
        } else {
            changes.add("Textual Change");
        }

        if (changes.isEmpty()) {
            changes.add("Minor Modification");
        }
        return changes;
    }
}

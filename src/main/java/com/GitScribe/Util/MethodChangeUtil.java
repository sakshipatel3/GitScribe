package com.GitScribe.Util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class MethodChangeUtil {

    /**
     * Parses the given source code (assumed to be a method block) into a MethodDeclaration.
     * Returns null if the source is empty or cannot be parsed.
     */
    public static MethodDeclaration parseMethodDeclaration(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source.toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        final MethodDeclaration[] result = new MethodDeclaration[1];
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                result[0] = node;
                return false; // stop after finding the first method
            }
        });
        return result[0];
    }

    /**
     * Detects changes between two method blocks (old and new) using robust normalization.
     * The normalization converts to lowercase and removes all whitespace.
     * If the normalized strings are identical, no changes are detected.
     * Otherwise, returns a list containing "Modified".
     * Debug prints are added to help diagnose differences.
     */
    public static List<String> detectMethodChangeTypes(String oldBlock, String newBlock) {
        List<String> changes = new ArrayList<>();
        if (oldBlock == null || newBlock == null) {
            if (oldBlock != newBlock) {
                changes.add("Modified");
            }
            return changes;
        }
        
        // Normalize: remove all whitespace and convert to lowercase.
        String normOld = oldBlock.replaceAll("\\s+", "").toLowerCase();
        String normNew = newBlock.replaceAll("\\s+", "").toLowerCase();
        
        // Debug logging: print the normalized blocks for inspection.
        System.out.println("Old normalized block: " + normOld);
        System.out.println("New normalized block: " + normNew);
        
        if (normOld.equals(normNew)) {
            return changes; // No change detected.
        } else {
            changes.add("Modified");
        }
        return changes;
    }
    
    /**
     * Extracts the method signature (everything before the first '{').
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
     * Extracts the method body (content within the outermost braces).
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
}

package com.GitScribe.Changes;



import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import java.util.ArrayList;
import java.util.List;

public class AnnotationChangeDetector {

    /**
     * Parses the given method block into a MethodDeclaration.
     * To help the parser, the method block is wrapped in a dummy class.
     *
     * @param methodBlock The source code of the method.
     * @return The MethodDeclaration, or null if parsing fails.
     */
    public static MethodDeclaration parseMethodDeclaration(String methodBlock) {
        if (methodBlock == null || methodBlock.trim().isEmpty()) {
            return null;
        }
        // Wrap the method block in a dummy class to create a complete compilation unit.
        String wrappedSource = "public class Dummy { " + methodBlock + " }";
        ASTParser parser = ASTParser.newParser(org.eclipse.jdt.core.dom.AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(wrappedSource.toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        final MethodDeclaration[] result = new MethodDeclaration[1];
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                result[0] = node;
                return false; // Stop after finding the first method.
            }
        });
        return result[0];
    }

    /**
     * Extracts annotations from a method block.
     *
     * @param methodBlock The source code of the method.
     * @return A list of annotation strings.
     */
    public static List<String> extractAnnotations(String methodBlock) {
        List<String> annotations = new ArrayList<>();
        MethodDeclaration method = parseMethodDeclaration(methodBlock);
        if (method != null) {
            List<?> modifiers = method.modifiers();
            for (Object modifier : modifiers) {
                if (modifier instanceof Annotation) {
                    Annotation ann = (Annotation) modifier;
                    annotations.add(ann.toString().trim());
                }
            }
        }
        return annotations;
    }

    /**
     * Compares the annotations between two method blocks.
     *
     * @param oldBlock The method block from the previous version.
     * @param newBlock The method block from the current version.
     * @return A list containing "Annotation Change" if the annotations differ; empty list otherwise.
     */
    public static List<String> detectAnnotationChanges(String oldBlock, String newBlock) {
        List<String> changes = new ArrayList<>();
        List<String> oldAnnotations = extractAnnotations(oldBlock);
        List<String> newAnnotations = extractAnnotations(newBlock);

        // Compare the lists (order and content)
        if (!oldAnnotations.equals(newAnnotations)) {
            changes.add("Annotation Change");
        }
        return changes;
    }
}

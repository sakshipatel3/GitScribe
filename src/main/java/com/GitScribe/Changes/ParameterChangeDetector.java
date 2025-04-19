package com.GitScribe.Changes;


import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class ParameterChangeDetector {

    private static MethodDeclaration parseMethodDeclaration(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
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
                return false;
            }
        });
        return result[0];
    }

    public static List<String> detectParameterChanges(String oldBlock, String newBlock) {
        List<String> changes = new ArrayList<>();
        MethodDeclaration oldMethod = parseMethodDeclaration(oldBlock);
        MethodDeclaration newMethod = parseMethodDeclaration(newBlock);
        if (oldMethod == null || newMethod == null) {
            return changes;
        }
        List<?> oldParams = oldMethod.parameters();
        List<?> newParams = newMethod.parameters();
        if (oldParams.size() != newParams.size()) {
            changes.add("Parameter Change");
        } else {
            for (int i = 0; i < oldParams.size(); i++) {
                String oldParam = oldParams.get(i).toString().trim();
                String newParam = newParams.get(i).toString().trim();
                if (!oldParam.equals(newParam)) {
                    changes.add("Parameter Change");
                    break;
                }
            }
        }
        return changes;
    }
}

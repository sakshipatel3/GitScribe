package com.GitScribe.Service;

import org.eclipse.jdt.core.dom.*;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import com.GitScribe.Service.MethodDeclarationData;

@Service
public class JDTParser {

    // Cache parsed methods per source to avoid re-parsing
    private static final Map<Integer, List<MethodDeclarationData>> METHOD_CACHE = new ConcurrentHashMap<>();

    /**
     * Uses JDT AST (without binding resolution) to parse method declarations and bodies.
     */
    public static List<MethodDeclarationData> getMethods(String javaSource) {
        if (javaSource == null) {
            return Collections.emptyList();
        }
        int key = javaSource.hashCode();
        List<MethodDeclarationData> cached = METHOD_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        // split lines for line-number calculations
        String[] lines = javaSource.split("");

        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setSource(javaSource.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        // disable bindings for speed
        parser.setResolveBindings(false);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        List<MethodDeclarationData> methods = new ArrayList<>();

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                int start = node.getStartPosition();
                int length = node.getLength();
                int end = start + length;
                int startLine = getLineForOffset(start, lines);
                int endLine = getLineForOffset(end, lines);
                String block = javaSource.substring(start, end);
                methods.add(new MethodDeclarationData(node, block, startLine, endLine));
                // do not visit inner nodes
                return false;
            }
        });

        METHOD_CACHE.put(key, methods);
        return methods;
    }

    private static int getLineForOffset(int offset, String[] lines) {
        int line = 1;
        int count = 0;
        for (String l : lines) {
            count += l.length() + 1;
            if (count > offset) break;
            line++;
        }
        return line;
    }
}

package com.GitScribe;

import com.GitScribe.Service.CloneService;
import com.GitScribe.Service.MethodHistoryDTO;
import com.GitScribe.Service.MethodHistoryService;
import com.GitScribe.Util.CSVWriterUtil;
import com.GitScribe.Changes.SignatureChangeDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class BatchRepositoryTester implements CommandLineRunner {

    @Autowired
    private MethodHistoryService methodHistoryService;

    @Autowired
    private CloneService cloneService;

    // List of repositories to process.
    private static final String[] repositories = {
    		
    	
    		"https://github.com/spring-projects/spring-petclinic.git"
       
    };

    @Override
    public void run(String... args) throws Exception {
        for (String repoUrl : repositories) {
            System.out.println("Processing Repository: " + repoUrl);
            File clonedRepo = cloneService.cloneRepository(repoUrl, null);
            String repoDirPath = clonedRepo.getAbsolutePath();

            List<String[]> csvData = new ArrayList<>();
            int rowIndex = 1;

            List<File> javaFiles = new ArrayList<>();
            findJavaFiles(clonedRepo, javaFiles);

            // Process each Java file in the repo with index
            for (int fileIndex = 0; fileIndex < javaFiles.size(); fileIndex++) {
                File javaFile = javaFiles.get(fileIndex);
                int displayFileNumber = fileIndex + 1;
                String relativePath = clonedRepo.toPath().relativize(javaFile.toPath()).toString();
                List<MethodHistoryDTO> methods = methodHistoryService.getAllMethodData(repoDirPath, relativePath);

                for (MethodHistoryDTO method : methods) {
                    int commitCount = method.getCommitHistory().size();
                    for (int i = 0; i < commitCount; i++) {
                        String changeTypes = method.getCommitHistory().get(i).getChangeTypes().isEmpty()
                            ? "-"
                            : String.join(", ", method.getCommitHistory().get(i).getChangeTypes());

                        String commitMessage = method.getCommitHistory().get(i).getMessage();
                        if (commitMessage == null || commitMessage.isEmpty()) {
                            commitMessage = "-";
                        }

                        String commitId = method.getCommitHistory().get(i).getCommitId();
                        if (commitId == null || commitId.isEmpty()) {
                            commitId = "-";
                        }

                        String commitAuthor = method.getCommitHistory().get(i).getAuthor();
                        if (commitAuthor == null || commitAuthor.isEmpty()) {
                            commitAuthor = "-";
                        }

                        // Add file number to log statement
                        System.out.println("[File " + displayFileNumber + "] Detected change in "
                            + relativePath + " @ " + commitId + ": " + changeTypes);

                        csvData.add(new String[]{
                            String.valueOf(rowIndex++),        // Index
                            method.getMethodName(),            // Method Name
                            SignatureChangeDetector.extractSignature(method.getMethodName()), // Method Signature
                            String.valueOf(commitCount),       // Commit Count
                            changeTypes,                       // Change Types
                            commitMessage,                     // Commit Message
                            commitId,                          // Commit ID
                            commitAuthor                       // Commit Author
                        });
                    }
                }
            }

            String repoName = repoUrl.substring(repoUrl.lastIndexOf("/") + 1).replace(".git", "");
            CSVWriterUtil.writeCSV("MethodHistory_" + repoName + ".csv", csvData);
            System.out.println("CSV Generated for " + repoName);
        }
    }

    private void findJavaFiles(File dir, List<File> javaFiles) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                findJavaFiles(file, javaFiles);
            } else if (file.getName().endsWith(".java")) {
                javaFiles.add(file);
            }
        }
    }
}

package com.GitScribe.Controller;

import com.GitScribe.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/method-history")
public class MethodHistoryController {

    @Autowired
    private MethodHistoryService methodHistoryService;

    @Autowired
    private CloneService cloneService;

    @Autowired
    private FileTreeService fileTreeService;

    @Autowired
    private JDTParser jdtParser;
    
    @Autowired
    private GitHubService gitHubService;


    /**
     * JSON endpoint: returns the method history data for a given file.
     * 
     * IMPORTANT: The client now must supply the local repository directory (repoDirPath)
     * and the relative file path (filePath). The MethodHistoryRequest should be updated
     * accordingly (i.e. it should no longer include repoOwner, repoName, or accessToken).
     */
    @PostMapping
    public ResponseEntity<?> getMethodHistory(@RequestBody MethodHistoryRequest request) {
        try {
            // Verify that the request has the new fields:
            // repoDirPath: local path to the cloned repository
            // filePath: relative path to the file within that repository.
            System.out.println("MethodHistoryController: repoDirPath = " + request.getRepoDirPath()
                    + ", filePath = " + request.getFilePath());
            
            List<MethodHistoryDTO> result = methodHistoryService.getAllMethodData(
                    request.getRepoDirPath(),  // changed from legacy remote fields to local repo directory path
                    request.getFilePath()      // relative file path inside the local clone
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            // In case e.getMessage() is null, you can provide a default error message.
            String errMsg = (e.getMessage() != null) ? e.getMessage() : "Unknown error in MethodHistoryService";
            return ResponseEntity.status(500).body("MethodHistoryService failed: " + errMsg);
        }
    }

    /**
     * HTML UI endpoint: displays method history with dropdowns and clickable commit links.
     */
    @GetMapping("/ui")
    public ResponseEntity<Void> getMethodHistoryUI() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/method-history-viewer.html"));
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    /**
     * Clone endpoint: the client provides a repository URL, the repo is cloned into a temporary folder,
     * and the absolute local path of the clone is returned.
     */
    @PostMapping("/clone")
    public ResponseEntity<String> cloneRepository(@RequestParam String repoUrl) {
        try {
            String folderName = UUID.randomUUID().toString(); // create unique directory
            String tempDir = System.getProperty("java.io.tmpdir") + "/" + folderName;
            File clonedRepo = cloneService.cloneRepository(repoUrl, tempDir);
            return ResponseEntity.ok(clonedRepo.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error cloning repository: " + e.getMessage());
        }
    }

    /**
     * List files endpoint: returns a list of file paths from the given repository directory.
     */
    @GetMapping("/files")
    public ResponseEntity<List<String>> listRepositoryFiles(@RequestParam String repoDirPath) {
        try {
            // fileTreeService should return an array (or list) of file paths that are relative to the repository root.
            List<String> files = fileTreeService.listFiles(repoDirPath);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


}

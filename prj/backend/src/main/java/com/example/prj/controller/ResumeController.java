package com.example.prj.controller;

import com.example.prj.Repository.UserRepository;
import com.example.prj.entity.User;
import com.example.prj.service.ElasticService;
import com.example.prj.service.EmbeddingService;
import com.example.prj.service.MatchService;
import com.example.prj.service.ResumeChunkService;

import com.example.prj.service.ResumeParserService;
import com.example.prj.service.ResumeStorageService;
import com.example.prj.service.SkillExtractorService;
import com.example.prj.service.UserService;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin
public class ResumeController {

    @Autowired
    private UserService userService;

    @Autowired
    private ResumeStorageService resumeStorageService;

    @PostMapping("/resume-upload")
    public ResponseEntity<?> uploadResume(@RequestParam("resume") MultipartFile resumeFile) {

        if (resumeFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Uploaded file is empty"));
        }

        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Not authenticated"));
        }

        try {
            List<String> skills = resumeStorageService.processResume(resumeFile, user);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Resume uploaded and processed successfully",
                            "userId", user.getId(),
                            "skills", skills


                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

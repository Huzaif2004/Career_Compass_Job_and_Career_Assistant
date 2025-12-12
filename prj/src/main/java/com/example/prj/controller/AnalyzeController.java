package com.example.prj.controller;

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

import com.example.prj.Repository.UserRepository;
import com.example.prj.entity.User;
import com.example.prj.service.*;

@RestController
public class AnalyzeController {

    @Autowired
    private ResumeParserService resumeParser;

    @Autowired
    private SkillExtractorService skillExtractor;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private ElasticService elasticService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MatchService matchService;
    // ======================== TEST EMBEDDING ============================
    @GetMapping("/test-embed")
    public List<Double> testEmbed() {
        return embeddingService.getEmbedding("Java developer with Spring Boot");
    }


    // ======================== RESUME UPLOAD ===============================
    @PostMapping("/resume-upload")
    public ResponseEntity<?> uploadResume(@RequestParam("resume") MultipartFile resumeFile) {
        try {
            System.out.println("=== resume upload debug ===");
            System.out.println("Original name = " + resumeFile.getOriginalFilename());
            System.out.println("Size = " + resumeFile.getSize());
            System.out.println("ContentType = " + resumeFile.getContentType());
            System.out.println("IsEmpty = " + resumeFile.isEmpty());

            if (resumeFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Uploaded file is empty"));
            }

            // 1️⃣ Save a stable temp copy
            File tmp = File.createTempFile("upload-", "-" + resumeFile.getOriginalFilename());
            resumeFile.transferTo(tmp);
            System.out.println("Saved temp file to: " + tmp.getAbsolutePath());

            // 2️⃣ Debug magic bytes
            try (InputStream is = Files.newInputStream(tmp.toPath())) {
                byte[] head = new byte[8];
                is.read(head);
                System.out.println("Magic bytes: " + toHex(head));
            }

            // 3️⃣ Parse text
            String text = resumeParser.extractFromFile(tmp);
            System.out.println("Extracted text length = " + text.length());

            // 4️⃣ Get authenticated user
            User user = userService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Not authenticated"));
            }

            // 5️⃣ Extract skills
            List<String> skills = skillExtractor.extractSkills(text);
            if (skills == null) skills = new ArrayList<>();
            System.out.println("Extracted skills = " + skills);

            // 6️⃣ Save to user table
            user.setResumeText(text);
            user.setResumeSkills(new ArrayList<>(skills)); // FIX: mutable!!
            userRepo.save(user);
            System.out.println("User updated in DB.");

            // 7️⃣ Generate embedding
            List<Double> embedding = embeddingService.getEmbedding(text);
            System.out.println("Embedding size = " + embedding.size());

            // 8️⃣ Store in Elasticsearch index
            elasticService.indexResume(
                    user.getId().toString(),
                    user.getId(),
                    text,
                    skills,
                    embedding
            );
            System.out.println("Indexed in Elasticsearch.");

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Resume uploaded and indexed successfully",
                            "skillsFound", skills
                    )
            );

        } catch (Exception e) {
            System.out.println("=== CONTROLLER ERROR ===");
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // ===================== MATCH ENDPOINT ================================
    @PostMapping("/match")
    public Object match(@RequestParam("jd") String jd) {
        return matchService.matchJob(jd);
    }


    // ====================== Utility ======================
    private static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}

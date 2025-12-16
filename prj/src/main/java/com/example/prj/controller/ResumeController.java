package com.example.prj.controller;

import com.example.prj.Repository.UserRepository;
import com.example.prj.entity.User;
import com.example.prj.service.ElasticService;
import com.example.prj.service.EmbeddingService;
import com.example.prj.service.MatchService;
import com.example.prj.service.ResumeChunkService;

import com.example.prj.service.ResumeParserService;
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

    @Autowired
    private ResumeChunkService resumeChunkService;

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

            File tmp = File.createTempFile("upload-", "-" + resumeFile.getOriginalFilename());
            resumeFile.transferTo(tmp);
            System.out.println("Saved temp file to: " + tmp.getAbsolutePath());

            try (InputStream is = Files.newInputStream(tmp.toPath())) {
                byte[] head = new byte[8];
                is.read(head);
                System.out.println("Magic bytes: " + toHex(head));
            }

            String text = resumeParser.extractFromFile(tmp);
            System.out.println("Extracted text length = " + text.length());

            User user = userService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Not authenticated"));
            }

            List<String> skills = skillExtractor.extractSkills(text);
            if (skills == null)
                skills = new ArrayList<>();
            System.out.println("Extracted skills = " + skills);

            user.setResumeText(text);
            user.setResumeSkills(new ArrayList<>(skills));
            userRepo.save(user);
            System.out.println("User updated in DB.");

            List<Double> embedding = embeddingService.getEmbedding(text);
            System.out.println("Embedding size = " + embedding.size());

            elasticService.indexResume(
                    user.getId().toString(),
                    user.getId(),
                    text,
                    skills,
                    embedding);
            System.out.println("Indexed in Elasticsearch.");

            List<String> chunks = resumeChunkService.chunk(text);
            System.out.println("Total chunks: " + chunks.size());

            List<List<Double>> chunkEmbeddings = new ArrayList<>();
            for (String chunk : chunks) {
                chunkEmbeddings.add(embeddingService.getEmbedding(chunk));
            }

            elasticService.indexResumeChunks(
                    user.getId(),
                    chunks,
                    chunkEmbeddings);
            System.out.println("Resume chunks indexed");

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Resume uploaded, indexed & chunked successfully",
                            "userId", user.getId(),
                            "skills", skills,
                            "chunks", chunks.size()));

        } catch (Exception e) {
            System.out.println("=== CONTROLLER ERROR ===");
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }
}

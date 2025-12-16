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

    @Autowired
    private ResumeChunkService resumeChunkService;
   
    @GetMapping("/test-embed")
    public List<Double> testEmbed() {
        return embeddingService.getEmbedding("Java developer with Spring Boot");
    }

    @PostMapping("/match")
    public Object match(@RequestParam("jd") String jd) {
        return matchService.matchJob(jd);
    }
    @PostMapping("/test-chunk")
public Object testChunk(@RequestParam("resume") MultipartFile file) {
    try {
        
        String text = resumeParser.extractText(file);

        
        List<String> chunks = resumeChunkService.chunk(text);

       
        return Map.of(
                "total_chunks", chunks.size(),
                "chunks", chunks
        );

    } catch (Exception e) {
        return Map.of("error", e.getMessage());
    }
}


   
}

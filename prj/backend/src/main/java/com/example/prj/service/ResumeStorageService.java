package com.example.prj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.prj.Repository.UserRepository;
import com.example.prj.entity.User;
import java.util.List;

@Service
public class ResumeStorageService {

    @Autowired private ResumeParserService parser;
    @Autowired private SkillExtractorService skillExtractor;
    @Autowired private EmbeddingService embeddingService;
    @Autowired private ElasticService elasticService;
    @Autowired private UserRepository userRepo;

    public void processResume(MultipartFile file, User user) throws Exception {

        // 1️⃣ Extract text
        String text = parser.extractText(file);

        // 2️⃣ Extract skills
        List<String> skills = skillExtractor.extractSkills(text);

        // 3️⃣ Generate embedding (1536 vector)
        List<Double> embedding = embeddingService.getEmbedding(text);

        // 4️⃣ Store embedding + resume in Elasticsearch
        elasticService.indexResume(
        String.valueOf(user.getId()),   // ES document ID
        user.getId(),                   // store userId inside ES doc
        text,
        skills,
        embedding
);


        // 5️⃣ Store parsed data in MySQL
        user.setResumeText(text);
        user.setResumeSkills(skills);
        userRepo.save(user);
    }
}

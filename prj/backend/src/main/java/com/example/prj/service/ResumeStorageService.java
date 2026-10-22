package com.example.prj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.prj.Repository.UserRepository;
import com.example.prj.entity.User;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeStorageService {

    @Autowired
    private ResumeParserService parser;
    @Autowired
    private SkillExtractorService skillExtractor;
    @Autowired
    private EmbeddingService embeddingService;
    @Autowired
    private ElasticService elasticService;
    @Autowired
    private ResumeChunkService chunkService;
    @Autowired
    private UserRepository userRepo;

    public List<String> processResume(MultipartFile file, User user) throws Exception {

        // 1️⃣ Parse resume
        String text = parser.extractText(file);

        // 2️⃣ Extract skills
        List<String> skills = skillExtractor.extractSkills(text);
        if (skills == null) {
            skills = new ArrayList<>();
        }

        // 3️⃣ Generate embedding
        List<Double> embedding = embeddingService.getEmbedding(text);

        // 4️⃣ Store resume in Elasticsearch
        elasticService.indexResume(
                String.valueOf(user.getId()),
                user.getId(),
                text,
                skills,
                embedding);

        // 5️⃣ Save resume data to DB
        user.setResumeText(text);
        user.setResumeSkills(skills);
        userRepo.save(user);

        // 6️⃣ Chunk resume
        List<String> chunks = chunkService.chunk(text);

        // 7️⃣ Generate chunk embeddings
        List<List<Double>> chunkEmbeddings = new ArrayList<>();
        for (String chunk : chunks) {
            chunkEmbeddings.add(embeddingService.getEmbedding(chunk));
            Thread.sleep(300); // rate-limit safety
        }

        // 8️⃣ Index chunks
        elasticService.indexResumeChunks(
                user.getId(),
                chunks,
                chunkEmbeddings);

        return skills;        
    }
}

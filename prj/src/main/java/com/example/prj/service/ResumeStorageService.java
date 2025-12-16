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

    @Autowired private ResumeParserService parser;
    @Autowired private SkillExtractorService skillExtractor;
    @Autowired private EmbeddingService embeddingService;
    @Autowired private ElasticService elasticService;
    @Autowired private ResumeChunkService chunkService;  // ✅ Add chunk service
    @Autowired private UserRepository userRepo;

    public void processResume(MultipartFile file, User user) throws Exception {

       
        String text = parser.extractText(file);

        
        List<String> skills = skillExtractor.extractSkills(text);

        
        List<Double> embedding = embeddingService.getEmbedding(text);

        
        elasticService.indexResume(
                String.valueOf(user.getId()),   
                user.getId(),                   
                text,
                skills,
                embedding
        );

        
        user.setResumeText(text);
        user.setResumeSkills(skills);
        userRepo.save(user);

        List<String> chunks = chunkService.chunk(text);

        
        List<List<Double>> chunkEmbeddings = new ArrayList<>();
        for (String chunk : chunks) {
            chunkEmbeddings.add(embeddingService.getEmbedding(chunk));
             Thread.sleep(300);
        }

       
        elasticService.indexResumeChunks(
                user.getId(),       
                chunks,
                chunkEmbeddings
        );

       
    }
}

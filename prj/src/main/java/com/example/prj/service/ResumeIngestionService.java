package com.example.prj.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeIngestionService {

    private final ResumeChunkService resumeChunkService;
    private final EmbeddingService embeddingService;
    private final ElasticService elasticService;

    public ResumeIngestionService(
            ResumeChunkService resumeChunkService,
            EmbeddingService embeddingService,
            ElasticService elasticService) {

        this.resumeChunkService = resumeChunkService;
        this.embeddingService = embeddingService;
        this.elasticService = elasticService;
    }

    public void ingestResume(Long userId, String resumeText) {

       
        List<String> chunks =
                resumeChunkService.chunk(resumeText);

       
        List<List<Double>> embeddings = new ArrayList<>();
        for (String chunk : chunks) {
            embeddings.add(embeddingService.getEmbedding(chunk));
        }

        
        elasticService.indexResumeChunks(userId, chunks, embeddings);
    }
}

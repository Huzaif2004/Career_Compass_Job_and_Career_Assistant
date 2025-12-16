package com.example.prj.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RAGService {

    private final EmbeddingService embeddingService;
    private final ElasticService elasticService;
    private final OpenAIService openAIService;

    public RAGService(
            EmbeddingService embeddingService,
            ElasticService elasticService,
            OpenAIService openAIService) {

        this.embeddingService = embeddingService;
        this.elasticService = elasticService;
        this.openAIService = openAIService;
    }

    public String chat(Long userId, String userMessage) {

        
        List<Double> queryEmbedding =
                embeddingService.getEmbedding(userMessage);

       
        List<Map<String, Object>> hits =
                elasticService.searchChunksForChat(userId,queryEmbedding, 5);
        
        if (hits == null || hits.isEmpty()) {
            return "I could not find relevant information in your resume.";
        }

        
        List<String> context = hits.stream()
                .map(hit -> (String) hit.get("chunk_text"))
                .filter(Objects::nonNull)
                .limit(5)
                .collect(Collectors.toList());

        String prompt = buildPrompt(context, userMessage);

        
        return openAIService.generateFeedback(prompt);
    }

    private String buildPrompt(List<String> context, String question) {

        StringBuilder sb = new StringBuilder();

        sb.append("""
        You are an AI career assistant.
        Use ONLY the resume context below.
        If the answer is not found, say you don't know.

        ===== Resume Context =====
        """);

        for (int i = 0; i < context.size(); i++) {
            sb.append("\n[Context ").append(i + 1).append("]\n");
            sb.append(context.get(i)).append("\n");
        }

        sb.append("""
        
        ===== User Question =====
        """).append(question);

        sb.append("""
        
        ===== Rules =====
        - Be clear and practical
        - Use bullet points when helpful
        - Do not hallucinate
        """);

        return sb.toString();
    }
}

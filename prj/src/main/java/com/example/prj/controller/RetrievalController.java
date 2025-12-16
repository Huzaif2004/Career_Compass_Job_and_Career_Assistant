package com.example.prj.controller;

import com.example.prj.dto.RetrievalRequest;
import com.example.prj.service.ElasticService;
import com.example.prj.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/retrieval")
@CrossOrigin
public class RetrievalController {

    private final EmbeddingService embeddingService;
    private final ElasticService elasticService;

    public RetrievalController(
            EmbeddingService embeddingService,
            ElasticService elasticService) {

        this.embeddingService = embeddingService;
        this.elasticService = elasticService;
    }

    @PostMapping("/topk")
    public List<Map<String, Object>> getTopKChunks(
            @RequestBody RetrievalRequest request
    ) {
        
        List<Double> queryEmbedding =
                embeddingService.getEmbedding(request.getQuery());

        
        return elasticService.searchChunksForChat(
                request.getUserId(),
                queryEmbedding,
                request.getK()
        );
    }
}

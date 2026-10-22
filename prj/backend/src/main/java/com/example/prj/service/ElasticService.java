package com.example.prj.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.util.*;

@Service
public class ElasticService {

    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.index}")
    private String indexName;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public void indexResume(String id, Long userId, String resumeText, List<String> skills, List<Double> embedding) {
    try {
        String url = esHost + "/" + indexName + "/_doc/" + id + "?refresh=true";

        Map<String, Object> doc = new HashMap<>();
        doc.put("userId", userId);  // ✅ store logged-in user ID
        doc.put("resume_text", resumeText);
        doc.put("skills", skills != null ? skills : new ArrayList<>());
        doc.put("embedding", embedding != null ? embedding : new ArrayList<>());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(doc)))
                .header("Content-Type", "application/json")
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

    } catch (Exception e) {
        throw new RuntimeException("Failed to index resume: " + e.getMessage(), e);
    }
}


    public List<Map<String, Object>> searchByEmbedding(List<Double> queryEmbedding, int k) {

        try {
            String url = esHost + "/" + indexName + "/_search";

            Map<String, Object> body = Map.of(
                    "size", k,
                    "query", Map.of(
                            "script_score", Map.of(
                                    "query", Map.of("match_all", Map.of()),
                                    "script", Map.of(
                                            "source", "cosineSimilarity(params.vector, 'embedding') + 1.0",
                                            "params", Map.of("vector", queryEmbedding)
                                    )
                            )
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> json = mapper.readValue(response.body(), Map.class);

            List<Map<String, Object>> hits =
                    (List<Map<String, Object>>) ((Map<String, Object>) json.get("hits")).get("hits");

            List<Map<String, Object>> results = new ArrayList<>();

            for (Map<String, Object> hit : hits) {
                Map<String, Object> src = (Map<String, Object>) hit.get("_source");
                src.put("_id", hit.get("_id"));
                src.put("_score", hit.get("_score"));
                results.add(src);
            }
            System.out.println("ES returned " + hits.size() + " results");
hits.forEach(hit -> {
    System.out.println("→ Resume ID: " + hit.get("_id"));
    System.out.println("→ Score: " + hit.get("_score"));
});

            return results;

        } catch (Exception e) {
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }
}

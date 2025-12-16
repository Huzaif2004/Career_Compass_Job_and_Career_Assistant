package com.example.prj.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
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
            doc.put("userId", userId);             doc.put("resume_text", resumeText);
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
                                            "params", Map.of("vector", queryEmbedding)))));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(
                            mapper.writeValueAsString(body)))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("=== ES RAW RESPONSE ===");
            System.out.println(response.body());

            Map<String, Object> json = mapper.readValue(response.body(), Map.class);

                        if (json.containsKey("error")) {
                throw new RuntimeException(
                        "Elasticsearch error: " + json.get("error"));
            }

            Object hitsObj = json.get("hits");
            if (!(hitsObj instanceof Map)) {
                return Collections.emptyList();
            }

            Map<String, Object> hitsMap = (Map<String, Object>) hitsObj;

            Object innerHits = hitsMap.get("hits");
            if (!(innerHits instanceof List)) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> hits = (List<Map<String, Object>>) innerHits;

            List<Map<String, Object>> results = new ArrayList<>();

            for (Map<String, Object> hit : hits) {
                Map<String, Object> src = (Map<String, Object>) hit.get("_source");

                src.put("_id", hit.get("_id"));
                src.put("_score", hit.get("_score"));
                results.add(src);
            }

            System.out.println("ES RESULTS COUNT = " + results.size());
            return results;

        } catch (Exception e) {
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    public void indexResumeChunks(Long userId, List<String> chunks, List<List<Double>> chunkEmbeddings) {

        try {
            StringBuilder bulk = new StringBuilder();

            for (int i = 0; i < chunks.size(); i++) {
                String docId = userId + "_chunk_" + i;

                Map<String, Object> source = Map.of(
                        "userId", userId,
                        "chunk_id", docId,
                        "chunk_index", i,                         "chunk_text", chunks.get(i),
                        "embedding", chunkEmbeddings.get(i));

                bulk.append("{\"index\": {\"_index\": \"")
                        .append(indexName)
                        .append("\", \"_id\": \"")
                        .append(docId)
                        .append("\"} }\n");

                bulk.append(mapper.writeValueAsString(source)).append("\n");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(esHost + "/_bulk"))
                    .header("Content-Type", "application/x-ndjson")
                    .POST(HttpRequest.BodyPublishers.ofString(bulk.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("=== BULK INDEX RESPONSE ===");
            System.out.println(response.body());

        } catch (Exception e) {
            throw new RuntimeException("Failed to bulk index chunks", e);
        }
    }

    public List<Map<String, Object>> searchChunksForChat(
        Long userId,
        List<Double> queryEmbedding,
        int k
) {
    try {
        String url = esHost + "/" + indexName + "/_search";

                Map<String, Object> body = Map.of(
            "size", k,
            "query", Map.of(
                "script_score", Map.of(
                    "query", Map.of(
                        "bool", Map.of(
                            "filter", List.of(
                                Map.of("term", Map.of("userId", userId)),                                          Map.of("exists", Map.of("field", "chunk_text"))                                 )
                        )
                    ),
                    "script", Map.of(
                        "source", "cosineSimilarity(params.vector, 'embedding') + 1.0",
                        "params", Map.of("vector", queryEmbedding)
                    )
                )
            )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(body)))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        Map<String, Object> json =
                mapper.readValue(response.body(), Map.class);

                if (!json.containsKey("hits")) {
            return Collections.emptyList();
        }

        Map<String, Object> hitsWrapper =
                (Map<String, Object>) json.get("hits");

        Object hitsObj = hitsWrapper.get("hits");
        if (!(hitsObj instanceof List)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> hits =
                (List<Map<String, Object>>) hitsObj;

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> hit : hits) {
            Map<String, Object> src =
                    (Map<String, Object>) hit.get("_source");

                        src.put("_id", hit.get("_id"));
            src.put("_score", hit.get("_score"));

            results.add(src);
        }

        return results;

    } catch (Exception e) {
        throw new RuntimeException(
            "Failed to search resume chunks for RAG: " + e.getMessage(), e
        );
    }
}


}

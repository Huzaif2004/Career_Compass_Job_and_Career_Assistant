package com.example.prj.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String MODEL = "text-embedding-3-small";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Double> getEmbedding(String text) {

        try {
            // Build JSON safely (NO STRING CONCAT)
            ObjectNode body = mapper.createObjectNode();
            body.put("model", MODEL);
            body.put("input", text);   // mapper escapes all invalid characters

            String jsonBody = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/embeddings"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("=== RAW OPENAI RESPONSE ===");
            System.out.println(response.body());

            JsonNode root = mapper.readTree(response.body());

            if (root.has("error")) {
                throw new RuntimeException("OpenAI API Error: "
                        + root.get("error").get("message").asText());
            }

            JsonNode arr = root.get("data").get(0).get("embedding");

            List<Double> vector = new ArrayList<>();
            arr.forEach(v -> vector.add(v.asDouble()));

            return vector;

        } catch (Exception e) {
            throw new RuntimeException("Embedding error: " + e.getMessage(), e);
        }
    }
}


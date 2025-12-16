package com.example.prj.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.SocketException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
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
            
            ObjectNode body = mapper.createObjectNode();
            body.put("model", MODEL);
            body.put("input", text);

            String jsonBody = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/embeddings"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = null;
int retries = 2;

while (retries-- > 0) {
    try {
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        break;
    } catch (SocketException e) {
        if (retries == 0) throw e;
        Thread.sleep(500);
    }
}

            JsonNode root = mapper.readTree(response.body());

            if (root.has("error")) {
                throw new RuntimeException("OpenAI API Error: "
                        + root.get("error").get("message").asText());
            }

            JsonNode arr = root.get("data").get(0).get("embedding");

            List<Double> vector = new ArrayList<>();
            arr.forEach(v -> vector.add(v.asDouble()));

            
            // try {
            //     Thread.sleep(350); // 300–500 ms recommended
            // } catch (InterruptedException e) {
            //     Thread.currentThread().interrupt();
            // }

            return vector;

        } catch (Exception e) {
            throw new RuntimeException("Embedding error: " + e.getMessage(), e);
        }
    }
}



package com.example.prj.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String MODEL = "gpt-4.1-mini";  // ⬅ or gpt-4.1 for better results

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();


    public String generateFeedback(String prompt) {

        try {
            Map<String, Object> body = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", 
                            "You are an expert career advisor. Provide extremely detailed and practical feedback."),
                        Map.of("role", "user", "content", prompt)
                )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(response.body());

            return json.get("choices").get(0).get("message").get("content").asText();

        } catch (Exception e) {
            return "AI feedback unavailable: " + e.getMessage();
        }
    }
}

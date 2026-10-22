package com.example.prj.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String MODEL = "gpt-4.1-mini";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String generateFeedback(String prompt) {

        try {
            Map<String, Object> body = Map.of(
                    "model", MODEL,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "You are an expert career assistant."
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(
                            HttpRequest.BodyPublishers.ofString(
                                    mapper.writeValueAsString(body)
                            )
                    )
                    .build();

            int retries = 2;

            while (retries-- > 0) {
                try {
                    HttpResponse<String> response =
                            client.send(request, HttpResponse.BodyHandlers.ofString());

                    JsonNode json = mapper.readTree(response.body());
                    return json
                            .get("choices")
                            .get(0)
                            .get("message")
                            .get("content")
                            .asText();

                } catch (SocketException e) {
                    if (retries == 0) {
                        throw e;
                    }
                    Thread.sleep(500);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("OpenAI API call failed", e);
        }

        return "Unable to generate feedback at this time.";
    }
}

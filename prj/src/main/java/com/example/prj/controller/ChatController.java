package com.example.prj.controller;

import com.example.prj.dto.ChatRequest;
import com.example.prj.security.JwtUtil;
import com.example.prj.service.RAGService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    private final RAGService ragService;
    private final JwtUtil jwtUtil;

    public ChatController(RAGService ragService, JwtUtil jwtUtil) {
        this.ragService = ragService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public String chat(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest
    ) {
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization header");
        }

        String token = authHeader.substring(7);

        
        Long userId = jwtUtil.extractUserId(token);
        System.out.println("JWT userId = " + userId);
        System.out.println("Extracted userId = " + jwtUtil.extractUserId(token));

        return ragService.chat(userId, request.getMessage());
    }
}

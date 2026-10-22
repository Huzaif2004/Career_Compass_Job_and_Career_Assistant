package com.example.prj.service;

import com.example.prj.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchService {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private ElasticService elasticService;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private SkillExtractorService skillExtractor;

    @Autowired
    private UserService userService;

    public Map<String, Object> matchJob(String jdText) {

        // 1️⃣ Extract JD details
        List<String> jdSkills = skillExtractor.extractSkills(jdText);
        jdSkills.replaceAll(s -> s.toLowerCase().trim());   // normalize

        int jdExperience = skillExtractor.extractExperience(jdText);

        // 2️⃣ JD embedding
        List<Double> jdEmbedding = embeddingService.getEmbedding(jdText);

        // 3️⃣ Logged in user
        User user = userService.getCurrentUser();
        if (user == null) {
            return Map.of("error", "User not authenticated");
        }

        // 4️⃣ Search ES for resumes
        List<Map<String, Object>> esResults = elasticService.searchByEmbedding(jdEmbedding, 10);

        // Filter only user's resume
        esResults.removeIf(doc -> {
            Object id = doc.get("userId");
            if (id == null) id = doc.get("userid"); // fallback
            return id == null || !id.toString().equals(user.getId().toString());
        });

        if (esResults.isEmpty()) {
            return Map.of("error", "No resume found for this user");
        }

        Map<String, Object> doc = esResults.get(0);

        // Avoid null pointer issues
        String resumeText = (String) doc.getOrDefault("resume_text", "");
        List<String> resumeSkills = (List<String>) doc.getOrDefault("skills", new ArrayList<>());

        // Normalize resume skills
        resumeSkills.replaceAll(s -> s.toLowerCase().trim());

        // 5️⃣ Compute scores
        long matchedSkills = resumeSkills.stream().filter(jdSkills::contains).count();
        double skillScore = matchedSkills / (double) Math.max(jdSkills.size(), 1);

        int resumeExp = skillExtractor.extractExperience(resumeText);
        double experienceScore =
                jdExperience == 0 ? 1.0 : Math.min(1.0, resumeExp / (double) jdExperience);

        int projectCount = skillExtractor.projectCount(resumeText);
        double projectScore = Math.min(1.0, projectCount / 3.0);

        List<String> keywords = extractKeywords(jdText);
        long matchK = keywords.stream().filter(k -> resumeText.toLowerCase().contains(k)).count();
        double keywordScore = matchK / (double) Math.max(keywords.size(), 1);

        double esScore = ((Number) doc.get("_score")).doubleValue();
        double semanticScore = 1 / (1 + Math.exp(-esScore)); // sigmoid
        double finalScore =
        (experienceScore * 0.40) +   // highest priority
        (skillScore * 0.30) +
        (projectScore * 0.15) +
        (keywordScore * 0.05) +
        (semanticScore * 0.10);      // lowest priority


        // 6️⃣ Missing skills (case-insensitive)
        List<String> missingSkills = new ArrayList<>(jdSkills);
        missingSkills.removeAll(resumeSkills);

        // 7️⃣ AI Evaluation
        String aiPrompt = """
            You are an expert career evaluator.

            Job Description Skills: %s
            Candidate Skills: %s
            Missing Skills: %s
            Experience Required: %d
            Candidate Experience: %d

            Write a LinkedIn-style evaluation containing without giving Header as LinkedIn Style Evaluation:
            1. Candidate's strengths
            2. Missing skills and how to improve them
            3. Whether experience is sufficient
            4. Recommended projects to improve resume
            5. How to rewrite resume for better match
            6. Final recommendation ("Strong Match", "Average Match", or "Needs Improvement")
        """.formatted(jdSkills, resumeSkills, missingSkills, jdExperience, resumeExp);

        String aiFeedback = openAIService.generateFeedback(aiPrompt);

        // 8️⃣ Build safe response
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("jdSkills", jdSkills);
        response.put("resume_skills", resumeSkills);
        response.put("matched_skills", matchedSkills);
        response.put("missing_skills", missingSkills);
        response.put("semantic_score", semanticScore);
        response.put("experience_score", experienceScore);
        response.put("project_score", projectScore);
        response.put("keyword_score", keywordScore);
        response.put("final_score", finalScore);
        response.put("percentage", Math.round(finalScore * 100));
        response.put("ai_feedback", aiFeedback);
        response.put("resume_text", resumeText.substring(0, Math.min(300, resumeText.length())));

        return response;
    }

    private List<String> extractKeywords(String jd) {
        jd = jd.toLowerCase();
        String[] common = {"developer", "api", "backend", "frontend", "sql", "microservices", "cloud"};
        List<String> result = new ArrayList<>();
        for (String w : common)
            if (jd.contains(w)) result.add(w);
        return result;
    }
}


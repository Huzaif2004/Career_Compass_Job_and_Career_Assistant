package com.example.prj.service;

import com.example.prj.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

        List<String> jdSkills = skillExtractor.extractSkills(jdText);
        jdSkills.replaceAll(s -> s.toLowerCase().trim());

        System.out.println("JD SKILLS = " + jdSkills);

        int jdExperience = skillExtractor.extractExperience(jdText);

        List<Double> jdEmbedding = embeddingService.getEmbedding(jdText);

        User user = userService.getCurrentUser();
        if (user == null) {
            return Map.of("error", "User not authenticated");
        }

        List<Map<String, Object>> esResults = elasticService.searchUserResumesByEmbedding(
                user.getId(),
                jdEmbedding,
                1);
        System.out.println("esResult"+esResults);
        System.out.println("END");
        if (esResults.isEmpty()) {
            return Map.of(
                    "final_score", 0,
                    "message", "Your resume does not match this job description");
        }

        Map<String, Object> doc = esResults.get(0);

        String resumeText = Objects.toString(doc.get("resume_text"), "");

        @SuppressWarnings("unchecked")
        List<String> resumeSkills = (List<String>) doc.getOrDefault("skills", new ArrayList<>());

        resumeSkills.replaceAll(s -> s.toLowerCase().trim());

        System.out.println("RESUME SKILLS = " + resumeSkills);

        long matchedSkillsCount = jdSkills.stream()
                .filter(jd -> resumeSkills.stream().anyMatch(rs -> skillMatches(jd, rs)))
                .count();
        List<String> matchedSkills = jdSkills.stream()
                .filter(jd -> resumeSkills.stream().anyMatch(rs -> skillMatches(jd, rs)))
                .collect(Collectors.toList());

        double skillScore = matchedSkillsCount / (double) Math.max(jdSkills.size(), 1);

        int resumeExperience = skillExtractor.extractRelevantExperience(resumeText, jdSkills);

        int projectCount = skillExtractor.countRelevantProjects(resumeText, jdSkills);

        double experienceScore;
        if (jdExperience <= 0) {
            experienceScore = 1.0;
        } else {
            experienceScore = Math.min(1.0, resumeExperience / (double) jdExperience);
        }

        double projectScore = Math.min(1.0, projectCount / 3.0);

        List<String> keywords = extractKeywords(jdText);

        long keywordMatches = keywords.stream()
                .filter(k -> resumeText.toLowerCase().contains(k))
                .count();

        double keywordScore = keywordMatches / (double) Math.max(keywords.size(), 1);

        double esScore = ((Number) doc.get("_score")).doubleValue();

        if (esScore < 1.2) { // cosineSimilarity + 1
            return Map.of(
                    "final_score", 0,
                    "message", "Your resume is not relevant to this job description");
        }

        double semanticScore = Math.min(1.0, esScore / 2.0);

        double finalScore = (experienceScore * 0.40) +
                (skillScore * 0.30) +
                (projectScore * 0.15) +
                (keywordScore * 0.05) +
                (semanticScore * 0.10);

        List<String> missingSkills = jdSkills.stream()
                .filter(jd -> resumeSkills.stream().noneMatch(rs -> skillMatches(jd, rs)))
                .toList();
        System.out.println(finalScore);
        System.out.println(jdSkills);

        String aiFeedback = openAIService.generateFeedback("""
                give consice and brief feedback without any headings like thank you
                Job Skills: %s
                Resume Skills: %s
                Missing Skills: %s
                Required Experience: %d years
                Candidate Experience: %d years
                """.formatted(
                jdSkills,
                resumeSkills,
                missingSkills,
                jdExperience,
                resumeExperience));

        System.out.print(finalScore);

        System.out.println("\nProject Count" + projectCount);
        System.out.println("Project Count" + resumeExperience);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("final_score", finalScore);
        response.put("jdSkills", jdSkills);
        response.put("matched_skills", matchedSkills);
        response.put("missing_skills", missingSkills);
        response.put("experience_score", experienceScore);
        response.put("project_score", projectScore);
        response.put("semantic_score", semanticScore);
        response.put("resume_text", resumeText);
        response.put("ai_feedback", aiFeedback);
        response.put("resume_experience", resumeExperience);
        response.put("percentage", Math.round(finalScore * 100));

        response.put(
                "resumeSnippet",
                resumeText.substring(0, Math.min(300, resumeText.length())));

        return response;
    }

    private List<String> extractKeywords(String jd) {
        jd = jd.toLowerCase();
        String[] common = {
                "developer", "api", "backend",
                "frontend", "sql", "microservices", "cloud"
        };

        List<String> result = new ArrayList<>();
        for (String w : common) {
            if (jd.contains(w))
                result.add(w);
        }
        return result;
    }

    private String norm(String s) {
        return s == null ? ""
                : s.toLowerCase()
                        .replaceAll("[^a-z0-9]", "").trim();
    }

    private boolean skillMatches(String a, String b) {
        return a.equals(b);
    }

}

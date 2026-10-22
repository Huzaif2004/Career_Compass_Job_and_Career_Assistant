package com.example.prj.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SkillExtractorService {

    private static final Set<String> CANONICAL_SKILLS = Set.of(
            "java", "spring", "spring boot", "spring cloud", "hibernate", "jpa",
            "microservices", "distributed systems", "system design",
            "rest api", "graphql", "api gateway",
            "kafka", "rabbitmq", "redis",
            "docker", "kubernetes", "ci/cd", "jenkins", "github actions",
            "aws", "azure", "gcp", "terraform",
            "sql", "mysql", "postgresql", "oracle",
            "nosql", "mongodb", "cassandra", "dynamodb",
            "javascript", "typescript", "html", "css",
            "react", "angular", "vue", "next js",
            "redux", "tailwind", "material ui",
            "python", "numpy", "pandas", "scikit learn",
            "tensorflow", "pytorch", "keras",
            "data analysis", "machine learning",
            "deep learning", "nlp", "computer vision",
            "ddd", "cqrs", "scalability", "performance tuning");

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("springboot", "spring boot"),
            Map.entry("spring boot", "spring boot"),
            Map.entry("spring-boot", "spring boot"),
            Map.entry("reactjs", "react"),
            Map.entry("react.js", "react"),
            Map.entry("js", "javascript"),
            Map.entry("ts", "typescript"),
            Map.entry("restful api", "rest api"),
            Map.entry("rest apis", "rest api"),
            Map.entry("amazon web services", "aws"),
            Map.entry("ec2", "aws"),
            Map.entry("s3", "aws"),
            Map.entry("rds", "aws"),
            Map.entry("microservice", "microservices"),
            Map.entry("ml", "machine learning"),
            Map.entry("dl", "deep learning"));

    public List<String> extractSkills(String text) {

        if (text == null || text.isBlank())
            return List.of();

        String normalized = normalize(text);
        Set<String> found = new LinkedHashSet<>();

        // 1️⃣ Alias detection first
        for (var entry : ALIASES.entrySet()) {
            String alias = normalize(entry.getKey());
            if (containsPhrase(normalized, alias)) {
                found.add(entry.getValue());
            }
        }

        // 2️⃣ Canonical skill detection
        for (String skill : CANONICAL_SKILLS) {
            String normSkill = normalize(skill);
            if (containsPhrase(normalized, normSkill)) {
                found.add(skill);
            }
        }

        return new ArrayList<>(found);
    }

    private boolean containsPhrase(String text, String phrase) {
        return Pattern.compile("\\b" + Pattern.quote(phrase) + "\\b")
                .matcher(text)
                .find();
    }

    private String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // experience & projectCount can stay as-is
    public int extractExperience(String text) {
        if (text == null)
            return 0;
        Matcher matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*\\+?\\s*(years?|yrs?)").matcher(text.toLowerCase());
        int max = 0;
        while (matcher.find()) {
            int years = (int) Math.floor(Double.parseDouble(matcher.group(1)));
            max = Math.max(max, years);
        }
        return max;
    }

    public int projectCount(String text) {
        if (text == null)
            return 0;
        return (int) Arrays.stream(text.toLowerCase().split("\n")).filter(line -> line.contains("project")).count();
    }

    public int extractRelevantExperience(String resumeText, List<String> jdSkills) {
        String section = extractSection(resumeText, "PROFESSIONAL EXPERIENCE");
        if (section.isBlank())
            return 0;

        // JD relevance check
        System.out.println("Section" + section);
        if (!containsAnySkill(section, jdSkills)) {
            return 0;
        }

        // Internship heuristic
        if (section.toLowerCase().contains("intern")) {
            return 1; // count as 1 year-equivalent
        }

        return extractExperience(section); // fallback if years exist
    }

    private List<String> splitProjects(String projectSection) {
        return Arrays.stream(
                projectSection.split("\n(?=[A-Z][A-Za-z ]{3,})"))
                .map(String::trim)
                .filter(p -> p.length() > 40)
                .toList();
    }

    public int countRelevantProjects(String resumeText, List<String> jdSkills) {
        String section = extractSection(resumeText, "PROJECTS");
        if (section.isBlank())
            return 0;

        List<String> projects = splitProjects(section);

        int count = 0;
        System.out.println("Projects");
        for (String project : projects) {
            System.out.println(project);
            if (containsAnySkill(project, jdSkills)) {
                count++;
            }
        }
        return count;
    }

    private boolean containsAnySkill(String textBlock, List<String> jdSkills) {
        if (textBlock == null || textBlock.isBlank() || jdSkills == null || jdSkills.isEmpty()) {
            return false;
        }

        String normalizedBlock = normalize(textBlock);

        for (String skill : jdSkills) {
            String normSkill = normalize(skill);
            if (containsPhrase(normalizedBlock, normSkill)) {
                return true; // at least one relevant skill found
            }
        }
        return false;
    }

    private String extractSection(String text, String sectionName) {
        String lower = text.toLowerCase();

        int start = lower.indexOf(sectionName.toLowerCase());
        if (start == -1)
            return "";

        int end = lower.length();
        List<String> endMarkers = List.of(
                "professional experience",
                "education",
                "projects",
                "achievements",
                "skills",
                "certifications");

        for (String marker : endMarkers) {
            int idx = lower.indexOf(marker, start + sectionName.length());
            if (idx != -1 && idx < end) {
                end = idx;
            }
        }

        return text.substring(start, end);
    }

}

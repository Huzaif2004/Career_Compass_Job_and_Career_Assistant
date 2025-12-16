package com.example.prj.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SkillExtractorService {

  
    private static final Set<String> CANONICAL_SKILLS = Set.of(

        // -------- Backend --------
        "java","spring","spring boot","spring cloud","hibernate","jpa",
        "microservices","distributed systems","system design",
        "rest api","graphql","api gateway",
        "kafka","rabbitmq","redis",

        // -------- DevOps / Cloud --------
        "docker","kubernetes","ci/cd","jenkins","github actions",
        "aws","azure","gcp","terraform",

        // -------- Databases --------
        "sql","mysql","postgresql","oracle",
        "nosql","mongodb","cassandra","dynamodb",

        // -------- Frontend --------
        "javascript","typescript","html","css",
        "react","angular","vue","next.js",
        "redux","tailwind","material ui",

        // -------- ML / Data --------
        "python","numpy","pandas","scikit-learn",
        "tensorflow","pytorch","keras",
        "data analysis","machine learning",
        "deep learning","nlp","computer vision",

        // -------- Architecture --------
        "ddd","cqrs","scalability","performance tuning"
    );

    
    private static final Map<String, String> ALIASES = Map.ofEntries(
        Map.entry("springboot", "spring boot"),
        Map.entry("spring-boot", "spring boot"),
        Map.entry("restful api", "rest api"),
        Map.entry("rest apis", "rest api"),
        Map.entry("amazon web services", "aws"),
        Map.entry("ec2", "aws"),
        Map.entry("s3", "aws"),
        Map.entry("rds", "aws"),
        Map.entry("microservice", "microservices"),
        Map.entry("js", "javascript"),
        Map.entry("ts", "typescript"),
        Map.entry("ml", "machine learning"),
        Map.entry("dl", "deep learning")
    );

    
    public List<String> extractSkills(String text) {

        if (text == null || text.isBlank()) return List.of();

        
        String normalized = normalize(text);

        Set<String> found = new LinkedHashSet<>();

        
        Set<String> tokens = new HashSet<>(Arrays.asList(normalized.split(" ")));

       
        for (String skill : CANONICAL_SKILLS) {
            if (normalized.contains(" " + skill + " ") || tokens.contains(skill)) {
                found.add(skill);
            }
        }

        
        for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                found.add(entry.getValue());
            }
        }

        return new ArrayList<>(found);
    }

    
    public int extractExperience(String text) {
        if (text == null) return 0;

        Matcher matcher = Pattern
                .compile("(\\d+(?:\\.\\d+)?)\\s*\\+?\\s*(years?|yrs?)")
                .matcher(text.toLowerCase());

        int max = 0;
        while (matcher.find()) {
            int years = (int) Math.floor(Double.parseDouble(matcher.group(1)));
            max = Math.max(max, years);
        }
        return max;
    }

    
    public int projectCount(String text) {
        if (text == null) return 0;

        return (int) Arrays.stream(text.toLowerCase().split("\n"))
                .filter(line -> line.contains("project"))
                .count();
    }

    
    private String normalize(String text) {

        return text
                .toLowerCase()
                // remove punctuation: numpy., pandas,
                .replaceAll("[^a-z0-9+.# ]", " ")
                // collapse spaces
                .replaceAll("\\s+", " ")
                .trim();
    }
}

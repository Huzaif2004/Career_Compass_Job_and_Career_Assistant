package com.example.prj.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SkillExtractorService {

    private static final List<String> SKILLS = List.of(
    "java","spring","spring boot","spring cloud","hibernate","jpa",
    "microservices","distributed systems","system design","api gateway",
    "kafka","rabbitmq","redis","hazelcast",
    "docker","kubernetes","ci/cd","jenkins","github actions",
    "aws","lambda","sqs","ecs","ec2","rds","s3",
    "sql","mysql","postgresql","nosql","mongodb",
    "ddd","cqrs","rest api","scalability","performance tuning"
);


    public List<String> extractSkills(String text) {
        if (text == null) return new ArrayList<>();

        text = text.toLowerCase();

        // Convert to mutable list before returning
        return new ArrayList<>(
                SKILLS.stream()
                        .filter(text::contains)
                        .toList()
        );
    }

    public int extractExperience(String text) {
        if (text == null) return 0;

        text = text.toLowerCase();

        return java.util.regex.Pattern.compile("(\\d+)\\s+years")
                .matcher(text)
                .results()
                .map(m -> Integer.parseInt(m.group(1)))
                .findFirst()
                .orElse(0);
    }

    public int projectCount(String text) {
        if (text == null) return 0;

        String lower = text.toLowerCase();

        return (int) Arrays.stream(lower.split("\n"))
                .filter(line -> line.contains("project"))
                .count();
    }
}

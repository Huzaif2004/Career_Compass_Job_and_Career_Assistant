package com.example.prj.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeChunkService {

    
    private static final int WORDS_PER_CHUNK = 180;
    private static final int OVERLAP = 40;

    private static final int MAX_CHUNKS = 8;       
    private static final int MIN_WORDS = 40;       

    public List<String> chunk(String text) {

        List<String> chunks = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        text = text.replaceAll("\\s+", " ").trim();

        String[] words = text.split(" ");
        int i = 0;
        int chunkIndex = 0;

        while (i < words.length && chunkIndex < MAX_CHUNKS) {

            int end = Math.min(words.length, i + WORDS_PER_CHUNK);

            
            if (end - i < MIN_WORDS) {
                break;
            }

            StringBuilder sb = new StringBuilder();
            for (int j = i; j < end; j++) {
                sb.append(words[j]).append(" ");
            }

            chunks.add(sb.toString().trim());

            
            i += (WORDS_PER_CHUNK - OVERLAP);
            chunkIndex++;
        }

        System.out.println("Chunking completed → Total chunks = " + chunks.size());
        return chunks;
    }
}

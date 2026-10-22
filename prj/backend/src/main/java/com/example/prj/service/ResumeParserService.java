package com.example.prj.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Service
public class ResumeParserService {
    public String extractText(MultipartFile file) throws Exception {

        String fileName = file.getOriginalFilename();
        if (fileName == null) throw new RuntimeException("Invalid file upload");

        InputStream is = file.getInputStream();

        if (fileName.endsWith(".pdf")) {
            return parsePDF(is);
        } else if (fileName.endsWith(".docx")) {
            return parseDOCX(is);
        } else if (fileName.endsWith(".doc")) {
            return parseDOC(is);
        } else {
            throw new RuntimeException("Unsupported file format");
        }
    }
    public String extractFromFile(File file) throws Exception {
    String name = file.getName().toLowerCase();

    if (name.endsWith(".pdf")) {
        return parsePDF(new FileInputStream(file));
    } else if (name.endsWith(".docx")) {
        return parseDOCX(new FileInputStream(file));
    } else if (name.endsWith(".doc")) {
        return parseDOC(new FileInputStream(file));
    } else {
        throw new RuntimeException("Unsupported file format: " + name);
    }
}

    private String parsePDF(InputStream is) throws Exception {
        try (PDDocument doc = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String parseDOCX(InputStream is) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder text = new StringBuilder();
            doc.getParagraphs().forEach(p -> text.append(p.getText()).append("\n"));
            return text.toString();
        }
    }

    private String parseDOC(InputStream is) throws Exception {
        try (HWPFDocument doc = new HWPFDocument(is)) {
            WordExtractor extractor = new WordExtractor(doc);
            return extractor.getText();
        }
    }
}

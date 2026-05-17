package service.ai.impl;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import service.ai.ResumeTextExtractor;

public class PdfBoxResumeTextExtractor implements ResumeTextExtractor {
    @Override
    public String extract(File resumeFile) throws IOException {
        if (resumeFile == null || !resumeFile.exists() || !resumeFile.isFile()) {
            return "";
        }

        try (PDDocument document = Loader.loadPDF(resumeFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return normalize(stripper.getText(document));
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}

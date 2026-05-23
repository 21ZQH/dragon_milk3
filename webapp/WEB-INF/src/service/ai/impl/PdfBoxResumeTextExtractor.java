package service.ai.impl;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import service.ai.ResumeTextExtractor;

/**
 * Implementation of {@link ResumeTextExtractor} that uses Apache PDFBox to
 * extract text content from PDF resume files.
 * <p>
 * This extractor loads the PDF via {@link Loader#loadPDF(File)},
 * employs {@link PDFTextStripper} with position-based sorting, and normalises
 * the resulting text (removing excessive whitespace and normalising line
 * endings).
 *
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see ResumeTextExtractor
 * @see SimplePdfResumeTextExtractor
 */
public class PdfBoxResumeTextExtractor implements ResumeTextExtractor {
    /**
     * Extracts text from the given PDF resume file using Apache PDFBox.
     *
     * @param resumeFile the PDF resume file to process
     * @return the extracted and normalised plain text, or an empty string
     *         if the file is {@code null}, does not exist, or is not a file
     * @throws IOException if an I/O error occurs during PDF loading or text
     *                     extraction
     */
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

    /**
     * Normalises the extracted text by unifying line separators, collapsing
     * runs of whitespace, and limiting consecutive blank lines to at most two.
     *
     * @param value the raw text to normalise
     * @return the normalised text, or an empty string if the input was
     *         {@code null}
     */
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

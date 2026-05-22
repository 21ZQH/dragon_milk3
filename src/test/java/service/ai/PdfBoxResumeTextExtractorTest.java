package service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import service.ai.impl.PdfBoxResumeTextExtractor;

/**
 * Unit tests for {@link PdfBoxResumeTextExtractor} in the TA Recruitment
 * system. Verifies text extraction from PDF documents and error handling
 * for missing or invalid PDF files.
 */
class PdfBoxResumeTextExtractorTest {
    /** Temporary directory used for creating test PDF files. */
    @TempDir
    Path tempDir;

    /**
     * Tests that text content is correctly extracted from a valid PDF file,
     * preserving all written lines including name, education, skills, and
     * project information.
     */
    @Test
    void extractReturnsTextFromPdf() throws Exception {
        File resume = tempDir.resolve("resume.pdf").toFile();
        writePdf(resume,
                "Alex Chen",
                "Education: BUPT Software Engineering",
                "Skills: Java, Spring Boot, SQL",
                "Project: TA Recruitment Platform");

        String text = new PdfBoxResumeTextExtractor().extract(resume);

        assertTrue(text.contains("Alex Chen"));
        assertTrue(text.contains("BUPT Software Engineering"));
        assertTrue(text.contains("Spring Boot"));
        assertTrue(text.contains("TA Recruitment Platform"));
    }

    /**
     * Tests that extracting text from a non-existent PDF file returns an
     * empty string instead of throwing an exception.
     */
    @Test
    void extractReturnsEmptyStringForMissingFile() throws Exception {
        File missing = tempDir.resolve("missing.pdf").toFile();

        assertEquals("", new PdfBoxResumeTextExtractor().extract(missing));
    }

    /**
     * Tests that extracting text from a file that exists but is not a valid
     * PDF format throws an {@link IOException}.
     */
    @Test
    void extractRejectsInvalidPdf() {
        File invalid = tempDir.resolve("invalid.pdf").toFile();

        try {
            assertTrue(invalid.createNewFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertThrows(IOException.class, () -> new PdfBoxResumeTextExtractor().extract(invalid));
    }

    /**
     * Helper method that creates a PDF file containing the given lines of
     * text using Apache PDFBox.
     *
     * @param file  the target PDF file to create
     * @param lines the text lines to write into the PDF
     * @throws IOException if an I/O error occurs during PDF creation
     */
    private void writePdf(File file, String... lines) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(72, 720);
                for (String line : lines) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -18);
                }
                contentStream.endText();
            }
            document.save(file);
        }
    }
}

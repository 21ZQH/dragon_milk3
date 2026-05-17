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

class PdfBoxResumeTextExtractorTest {
    @TempDir
    Path tempDir;

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

    @Test
    void extractReturnsEmptyStringForMissingFile() throws Exception {
        File missing = tempDir.resolve("missing.pdf").toFile();

        assertEquals("", new PdfBoxResumeTextExtractor().extract(missing));
    }

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

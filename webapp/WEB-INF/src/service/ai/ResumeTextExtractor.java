package service.ai;

import java.io.File;
import java.io.IOException;

/**
 * Interface for extracting plain text from resume files.
 * <p>
 * Implementations support different document formats (e.g., PDF) and
 * extraction strategies, returning the content as a single string for
 * downstream processing by AI clients or other services.
 *
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see service.ai.impl.PdfBoxResumeTextExtractor
 * @see service.ai.impl.SimplePdfResumeTextExtractor
 */
public interface ResumeTextExtractor {
    /**
     * Extracts the textual content from the given resume file.
     *
     * @param resumeFile the PDF (or other supported format) resume file
     * @return the extracted plain text; an empty string if the file is
     *         {@code null}, does not exist, or cannot be read
     * @throws IOException if an I/O error occurs during extraction
     */
    String extract(File resumeFile) throws IOException;
}

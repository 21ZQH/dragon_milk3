package service.ai.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

import service.ai.ResumeTextExtractor;

/**
 * Lightweight implementation of {@link ResumeTextExtractor} that extracts
 * text from PDF files without relying on Apache PDFBox.
 * <p>
 * This extractor reads the raw PDF bytes and applies heuristic techniques:
 * decompressing {@code stream} content (including ASCII-85 decoding followed
 * by zlib inflation), extracting text from PDF text-showing operators
 * ({@code Tj}), and falling back to printable-ASCII scanning for simple or
 * corrupted PDFs.
 *
 *
 * @author TA Recruitment Team
 * @version 1.0
 * @since 2025-03-01
 * @see ResumeTextExtractor
 * @see PdfBoxResumeTextExtractor
 */
public class SimplePdfResumeTextExtractor implements ResumeTextExtractor {
    /** Pattern to locate PDF stream content between {@code stream} and {@code endstream}. */
    private static final Pattern STREAM_PATTERN = Pattern.compile("stream\\r?\\n(.*?)\\r?\\nendstream", Pattern.DOTALL);
    /** Pattern to extract text from parentheses in {@code Tj} PDF operators. */
    private static final Pattern TEXT_PATTERN = Pattern.compile("\\((.*?)\\)\\s*Tj", Pattern.DOTALL);

    /**
     * Extracts text from the given PDF resume file using heuristic parsing.
     *
     * @param resumeFile the PDF resume file to process
     * @return the extracted plain text with normalised whitespace, or an
     *         empty string if the file is {@code null}, does not exist, or
     *         is not a file
     * @throws IOException if an I/O error occurs during reading
     */
    @Override
    public String extract(File resumeFile) throws IOException {
        if (resumeFile == null || !resumeFile.exists() || !resumeFile.isFile()) {
            return "";
        }

        byte[] bytes = Files.readAllBytes(resumeFile.toPath());
        String decodedStreamText = extractDecodedStreamText(bytes);
        if (!decodedStreamText.isBlank()) {
            return decodedStreamText;
        }

        StringBuilder text = new StringBuilder();
        StringBuilder token = new StringBuilder();
        for (byte value : bytes) {
            int ch = value & 0xff;
            if (ch >= 32 && ch <= 126) {
                token.append((char) ch);
            } else {
                appendToken(text, token);
            }
        }
        appendToken(text, token);
        return text.toString().replaceAll("\\s+", " ").trim();
    }

    /**
     * Attempts to extract text by decoding PDF streams (ASCII-85 + zlib
     * inflation) and parsing {@code Tj} operators.
     *
     * @param bytes the raw PDF file bytes
     * @return the extracted text with normalised whitespace, or an empty
     *         string if decoding yields no content
     */
    private String extractDecodedStreamText(byte[] bytes) {
        String pdf = new String(bytes, StandardCharsets.ISO_8859_1);
        Matcher streamMatcher = STREAM_PATTERN.matcher(pdf);
        StringBuilder extracted = new StringBuilder();
        while (streamMatcher.find()) {
            String stream = streamMatcher.group(1).trim();
            byte[] streamBytes = stream.getBytes(StandardCharsets.ISO_8859_1);
            byte[] decodedBytes = tryDecodeStream(streamBytes);
            if (decodedBytes.length == 0) {
                continue;
            }

            String content = new String(decodedBytes, StandardCharsets.ISO_8859_1);
            Matcher textMatcher = TEXT_PATTERN.matcher(content);
            while (textMatcher.find()) {
                String value = unescapePdfText(textMatcher.group(1));
                if (!value.isBlank()) {
                    if (extracted.length() > 0) {
                        extracted.append(' ');
                    }
                    extracted.append(value);
                }
            }
        }
        return extracted.toString().replaceAll("\\s+", " ").trim();
    }

    /**
     * Attempts to decode a raw PDF stream by first applying ASCII-85
     * decoding if the stream ends with {@code ~>}, then zlib inflation.
     *
     * @param streamBytes the raw stream bytes
     * @return the decoded bytes, or an empty array if decoding fails
     */
    private byte[] tryDecodeStream(byte[] streamBytes) {
        byte[] current = streamBytes;
        try {
            String asText = new String(current, StandardCharsets.ISO_8859_1).trim();
            if (asText.endsWith("~>")) {
                current = ascii85Decode(asText);
            }
            return inflate(current);
        } catch (Exception ignored) {
            return new byte[0];
        }
    }

    /**
     * Decodes an ASCII-85 (also known as btoa) encoded string.
     *
     * @param value the ASCII-85 encoded text (may contain whitespace and
     *              the {@code ~>} end marker)
     * @return the decoded binary data
     */
    private byte[] ascii85Decode(String value) {
        String text = value.replaceAll("\\s+", "");
        if (text.endsWith("~>")) {
            text = text.substring(0, text.length() - 2);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long tuple = 0;
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == 'z' && count == 0) {
                output.write(0);
                output.write(0);
                output.write(0);
                output.write(0);
                continue;
            }
            if (ch < '!' || ch > 'u') {
                continue;
            }
            tuple = tuple * 85 + (ch - '!');
            count++;
            if (count == 5) {
                writeTuple(output, tuple, 4);
                tuple = 0;
                count = 0;
            }
        }

        if (count > 0) {
            for (int i = count; i < 5; i++) {
                tuple = tuple * 85 + 84;
            }
            writeTuple(output, tuple, count - 1);
        }
        return output.toByteArray();
    }

    /**
     * Writes a specified number of bytes from a 32-bit tuple to the output
     * stream in big-endian order.
     *
     * @param output        the output stream to write to
     * @param tuple         the 32-bit tuple value
     * @param bytesToWrite  the number of bytes to write (1-4)
     */
    private void writeTuple(ByteArrayOutputStream output, long tuple, int bytesToWrite) {
        for (int shift = 24, written = 0; written < bytesToWrite; shift -= 8, written++) {
            output.write((int) ((tuple >> shift) & 0xff));
        }
    }

    /**
     * Decompresses data using zlib (RFC 1950) inflation.
     *
     * @param bytes the compressed data
     * @return the decompressed bytes
     * @throws IOException if inflation fails
     */
    private byte[] inflate(byte[] bytes) throws IOException {
        try (InflaterInputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(bytes));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Un-escapes common PDF text escape sequences.
     *
     * @param value the PDF-escaped text
     * @return the unescaped text
     */
    private String unescapePdfText(String value) {
        return value
                .replace("\\(", "(")
                .replace("\\)", ")")
                .replace("\\\\", "\\");
    }

    /**
     * Appends a token to the text buffer if its length is at least 3
     * characters, then resets the token builder.
     *
     * @param text  the main text buffer
     * @param token the token builder to flush
     */
    private void appendToken(StringBuilder text, StringBuilder token) {
        if (token.length() >= 3) {
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append(token);
        }
        token.setLength(0);
    }
}

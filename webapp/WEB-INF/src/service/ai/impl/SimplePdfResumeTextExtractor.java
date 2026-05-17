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

public class SimplePdfResumeTextExtractor implements ResumeTextExtractor {
    private static final Pattern STREAM_PATTERN = Pattern.compile("stream\\r?\\n(.*?)\\r?\\nendstream", Pattern.DOTALL);
    private static final Pattern TEXT_PATTERN = Pattern.compile("\\((.*?)\\)\\s*Tj", Pattern.DOTALL);

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

    private void writeTuple(ByteArrayOutputStream output, long tuple, int bytesToWrite) {
        for (int shift = 24, written = 0; written < bytesToWrite; shift -= 8, written++) {
            output.write((int) ((tuple >> shift) & 0xff));
        }
    }

    private byte[] inflate(byte[] bytes) throws IOException {
        try (InflaterInputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(bytes));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String unescapePdfText(String value) {
        return value
                .replace("\\(", "(")
                .replace("\\)", ")")
                .replace("\\\\", "\\");
    }

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

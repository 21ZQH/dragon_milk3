package store;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing and formatting CSV (Comma-Separated Values) records.
 *
 * <p>This class provides static methods for parsing CSV lines with proper
 * handling of quoted fields and embedded delimiters, as well as serializing
 * field arrays back to CSV format. It supports standard CSV quoting rules
 * where double quotes inside quoted fields are escaped by doubling them.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
final class CsvRecord {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CsvRecord() {
    }

    /**
     * Parses a CSV line into an array of field values.
     * <p>Handles quoted fields and escaped double-quote characters ("").
     * If the input line is {@code null}, an empty array is returned.</p>
     *
     * @param line the CSV line to parse
     * @return an array of field values extracted from the line
     */
    static String[] parse(String line) {
        if (line == null) {
            return new String[0];
        }

        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(ch);
                }
            } else if (ch == '"') {
                quoted = true;
            } else if (ch == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(ch);
            }
        }

        fields.add(field.toString());
        return fields.toArray(String[]::new);
    }

    /**
     * Serializes an array of field values into a single CSV line.
     * <p>Each field is automatically quoted if it contains a comma or
     * double-quote character.</p>
     *
     * @param fields the field values to serialize
     * @return a CSV-formatted line string
     */
    static String toLine(String... fields) {
        List<String> encodedFields = new ArrayList<>();
        for (String field : fields) {
            encodedFields.add(field(field));
        }
        return String.join(",", encodedFields);
    }

    /**
     * Encodes a single field value for CSV output.
     * <p>If the value contains a comma or double-quote character, it is
     * wrapped in double quotes and internal quotes are escaped by doubling.
     * Carriage return and newline characters are replaced with spaces.</p>
     *
     * @param value the raw field value
     * @return the CSV-encoded field string
     */
    static String field(String value) {
        String safeValue = value == null ? "" : value.replace('\r', ' ').replace('\n', ' ');
        if (!safeValue.contains(",") && !safeValue.contains("\"")) {
            return safeValue;
        }
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}

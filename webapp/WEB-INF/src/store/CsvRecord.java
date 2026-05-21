package store;

import java.util.ArrayList;
import java.util.List;

final class CsvRecord {
    private CsvRecord() {
    }

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

    static String toLine(String... fields) {
        List<String> encodedFields = new ArrayList<>();
        for (String field : fields) {
            encodedFields.add(field(field));
        }
        return String.join(",", encodedFields);
    }

    static String field(String value) {
        String safeValue = value == null ? "" : value.replace('\r', ' ').replace('\n', ' ');
        if (!safeValue.contains(",") && !safeValue.contains("\"")) {
            return safeValue;
        }
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}

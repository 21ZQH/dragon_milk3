package store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeadlineStore {
    public static final String FILE_PATH_PROPERTY = "deadline.store.path";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void saveDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            return;
        }

        Path filePath = resolveFilePath();
        try {
            ensureParentDirectoryExists(filePath);
            Files.writeString(filePath, deadline.format(FORMATTER));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LocalDateTime getDeadline() {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return null;
        }

        try {
            String content = Files.readString(filePath).trim();
            if (content.isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(content, FORMATTER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Path resolveFilePath() {
        String overridePath = System.getProperty(FILE_PATH_PROPERTY);
        if (overridePath != null && !overridePath.isBlank()) {
            return Paths.get(overridePath);
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "webapps", "SE", "WEB-INF", "file", "deadline.txt");
        }

        return Paths.get(System.getProperty("user.dir"), "webapp", "WEB-INF", "file", "deadline.txt");
    }

    private static void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
    }
}

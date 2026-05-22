package store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data access layer for persisting and retrieving recruitment deadlines.
 *
 * <p>This class manages two separate deadlines: the TA application deadline and
 * the MO modification deadline. Each deadline is stored in a separate file and
 * formatted using the pattern "yyyy-MM-dd HH:mm".</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public class DeadlineStore {
    /** System property key for overriding the default TA application deadline file path. */
    public static final String FILE_PATH_PROPERTY = "deadline.store.path";
    /** System property key for overriding the default MO modification deadline file path. */
    public static final String MO_FILE_PATH_PROPERTY = "mo.deadline.store.path";

    /** The date-time formatter used for serializing and deserializing deadline values. */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Saves the TA application deadline to the store.
     *
     * @param deadline the deadline date-time to save; if {@code null}, the method returns without action
     */
    public static void saveDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            return;
        }

        Path filePath = resolveApplicationDeadlineFilePath();
        try {
            ensureParentDirectoryExists(filePath);
            Files.writeString(filePath, deadline.format(FORMATTER));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the TA application deadline from the store.
     *
     * @return the saved deadline date-time, or {@code null} if no deadline has been saved
     */
    public static LocalDateTime getDeadline() {
        Path filePath = resolveApplicationDeadlineFilePath();
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

    /**
     * Saves the MO modification deadline to the store.
     *
     * @param deadline the deadline date-time to save; if {@code null}, the method returns without action
     */
    public static void saveMoModifyDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            return;
        }

        Path filePath = resolveMoDeadlineFilePath();
        try {
            ensureParentDirectoryExists(filePath);
            Files.writeString(filePath, deadline.format(FORMATTER));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the MO modification deadline from the store.
     *
     * @return the saved deadline date-time, or {@code null} if no deadline has been saved
     */
    public static LocalDateTime getMoModifyDeadline() {
        Path filePath = resolveMoDeadlineFilePath();
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

    /**
     * Clears all stored deadlines (both TA application and MO modification).
     */
    public static void clearDeadlines() {
        clearFile(resolveApplicationDeadlineFilePath());
        clearFile(resolveMoDeadlineFilePath());
    }

    /**
     * Clears the content of the specified file.
     *
     * @param filePath the path to the file to clear
     */
    private static void clearFile(Path filePath) {
        try {
            ensureParentDirectoryExists(filePath);
            Files.writeString(filePath, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resolves the TA application deadline file path from system properties or defaults.
     *
     * @return the resolved file path
     */
    private static Path resolveApplicationDeadlineFilePath() {
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

    /**
     * Resolves the MO modification deadline file path from system properties or defaults.
     *
     * @return the resolved file path
     */
    private static Path resolveMoDeadlineFilePath() {
        String overridePath = System.getProperty(MO_FILE_PATH_PROPERTY);
        if (overridePath != null && !overridePath.isBlank()) {
            return Paths.get(overridePath);
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "webapps", "SE", "WEB-INF", "file", "mo-deadline.txt");
        }

        return Paths.get(System.getProperty("user.dir"), "webapp", "WEB-INF", "file", "mo-deadline.txt");
    }

    /**
     * Ensures the parent directory of the given file path exists, creating it if necessary.
     *
     * @param filePath the file path whose parent directory should be checked
     * @throws IOException if an I/O error occurs during directory creation
     */
    private static void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
    }
}

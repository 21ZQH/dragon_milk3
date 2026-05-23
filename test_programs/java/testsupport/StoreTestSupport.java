package testsupport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import store.CourseStore;
import store.DeadlineStore;
import store.ApplicationFormStore;
import store.UserStore;

/**
 * Utility class providing helper methods for store-related unit tests in the TA
 * Recruitment system. Manages temporary file paths and system property overrides
 * to enable isolated test execution without interfering with production data files.
 *
 * @author TA Recruitment System
 */
public final class StoreTestSupport {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private StoreTestSupport() {
    }

    /**
     * Overrides the {@link UserStore} file path to point to a temporary directory
     * and returns the resolved file path.
     *
     * @param tempDir the temporary directory to use
     * @return the resolved file path for the user store file
     */
    public static Path useUserStore(Path tempDir) {
        Path filePath = tempDir.resolve("users.txt");
        System.setProperty(UserStore.FILE_PATH_PROPERTY, filePath.toString());
        return filePath;
    }

    /**
     * Overrides the {@link CourseStore} file path to point to a temporary directory
     * and returns the resolved file path.
     *
     * @param tempDir the temporary directory to use
     * @return the resolved file path for the course store file
     */
    public static Path useCourseStore(Path tempDir) {
        Path filePath = tempDir.resolve("courses.txt");
        System.setProperty(CourseStore.FILE_PATH_PROPERTY, filePath.toString());
        return filePath;
    }

    /**
     * Overrides the {@link DeadlineStore} application deadline file path to point
     * to a temporary directory and returns the resolved file path.
     *
     * @param tempDir the temporary directory to use
     * @return the resolved file path for the application deadline file
     */
    public static Path useApplicationDeadlineStore(Path tempDir) {
        Path filePath = tempDir.resolve("deadline.txt");
        System.setProperty(DeadlineStore.FILE_PATH_PROPERTY, filePath.toString());
        return filePath;
    }

    /**
     * Overrides the {@link DeadlineStore} MO modification deadline file path to
     * point to a temporary directory and returns the resolved file path.
     *
     * @param tempDir the temporary directory to use
     * @return the resolved file path for the MO deadline file
     */
    public static Path useMoDeadlineStore(Path tempDir) {
        Path filePath = tempDir.resolve("mo-deadline.txt");
        System.setProperty(DeadlineStore.MO_FILE_PATH_PROPERTY, filePath.toString());
        return filePath;
    }

    /**
     * Overrides the {@link ApplicationFormStore} file path to point to a temporary
     * directory and returns the resolved file path.
     *
     * @param tempDir the temporary directory to use
     * @return the resolved file path for the application form store file
     */
    public static Path useApplicationFormStore(Path tempDir) {
        Path filePath = tempDir.resolve("application-forms.txt");
        System.setProperty(ApplicationFormStore.FILE_PATH_PROPERTY, filePath.toString());
        return filePath;
    }

    /**
     * Writes the given lines to the specified file path, creating parent directories
     * if they do not already exist. Uses UTF-8 character encoding.
     *
     * @param filePath the target file path
     * @param lines    the lines to write to the file
     * @throws IOException if an I/O error occurs during writing
     */
    public static void writeLines(Path filePath, String... lines) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
        Files.write(filePath, Arrays.asList(lines), StandardCharsets.UTF_8);
    }

    /**
     * Clears all store-related system property overrides to restore default file
     * paths after test execution. This includes properties for UserStore, CourseStore,
     * ApplicationFormStore, DeadlineStore, and the catalina.base property.
     */
    public static void clearStoreOverrides() {
        System.clearProperty(UserStore.FILE_PATH_PROPERTY);
        System.clearProperty(CourseStore.FILE_PATH_PROPERTY);
        System.clearProperty(ApplicationFormStore.FILE_PATH_PROPERTY);
        System.clearProperty(DeadlineStore.FILE_PATH_PROPERTY);
        System.clearProperty(DeadlineStore.MO_FILE_PATH_PROPERTY);
        System.clearProperty("catalina.base");
    }
}

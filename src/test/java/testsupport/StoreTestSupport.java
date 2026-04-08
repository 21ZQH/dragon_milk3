package testsupport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import store.CourseStore;
import store.UserStore;

public final class StoreTestSupport {
    private StoreTestSupport() {
    }

    public static Path useUserStore(Path tempDir) {
        Path filePath = tempDir.resolve("users.txt");
        System.setProperty(UserStore.FILE_PATH_PROPERTY, filePath.toString());
        return filePath;
    }

    public static Path useCourseStore(Path tempDir) {
        Path filePath = tempDir.resolve("courses.txt");
        System.setProperty(CourseStore.FILE_PATH_PROPERTY, filePath.toString());
        return filePath;
    }

    public static void writeLines(Path filePath, String... lines) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
        Files.write(filePath, Arrays.asList(lines), StandardCharsets.UTF_8);
    }

    public static void clearStoreOverrides() {
        System.clearProperty(UserStore.FILE_PATH_PROPERTY);
        System.clearProperty(CourseStore.FILE_PATH_PROPERTY);
        System.clearProperty("catalina.base");
    }
}

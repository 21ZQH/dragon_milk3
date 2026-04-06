package store;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import model.Admin;
import model.Mo;
import model.TA;
import model.User;

public class UserStore {
    public static final String FILE_PATH_PROPERTY = "user.store.path";

    public static void saveUser(User user) {
        String line = user.getName() + "," + user.getPassword() + "," + user.getRole() + "," + user.getEmail();
        Path filePath = resolveFilePath();

        try {
            ensureParentDirectoryExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (FileWriter fw = new FileWriter(filePath.toFile(), true)) {
            fw.write(line + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User validateUser(String password, String role, String email) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return null;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String name = parts[0];
                    String p = parts[1];
                    String r = parts[2];
                    String e = parts[3];
                    if (p.equals(password) && r.equals(role) && e.equals(email)) {
                        return buildUser(name, r, p, e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User validateUser(String password, String email) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return null;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String name = parts[0];
                    String p = parts[1];
                    String r = parts[2];
                    String e = parts[3];
                    if (p.equals(password) && e.equals(email)) {
                        return buildUser(name, r, p, e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isEmailRegistered(String email) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return false;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String e = parts[3];
                    if (e.equals(email)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static User buildUser(String name, String role, String password, String email) {
        User user = null;
        if ("Admin".equals(role)) {
            user = new Admin(password, email);
        }
        if ("TA".equals(role)) {
            user = new TA(password, email);
        }
        if ("Mo".equals(role)) {
            user = new Mo(password, email);
        }
        if (user != null && name != null && !name.isBlank()) {
            user.setName(name);
        }
        return user;
    }

    private static Path resolveFilePath() {
        String overridePath = System.getProperty(FILE_PATH_PROPERTY);
        if (overridePath != null && !overridePath.isBlank()) {
            return Paths.get(overridePath);
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "webapps", "SE", "WEB-INF", "file", "users.txt");
        }

        return Paths.get(System.getProperty("user.dir"), "webapp", "WEB-INF", "file", "users.txt");
    }

    private static void ensureParentDirectoryExists(Path filePath) throws IOException {
        Path parentPath = filePath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
    }
}

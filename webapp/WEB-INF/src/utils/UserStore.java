package utils;

import model.Admin;
import model.Mo;
import model.TA;
import model.User;
import java.io.*;

public class UserStore {
    private static final String FILE_PATH = "G:\\Tomcat\\webapps\\SE\\WEB-INF\\file\\users.txt";

    public static void saveUser(User user) {
        String line = user.getName() + "," + user.getPassword() + "," + user.getRole()+ "," + user.getEmail();
        try (FileWriter fw = new FileWriter(FILE_PATH, true)) {
            fw.write(line + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User validateUser(String password, String role, String email) {
    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 4) {
                String n = parts[0];
                String p = parts[1];
                String r = parts[2];
                String e = parts[3];
                if ( p.equals(password) && r.equals(role) && e.equals(email)) {
                    switch (r) {
                        case "Admin":
                            return new Admin( p, e);
                        case "TA":
                            return new TA(p, e);
                        case "Mo":
                            return new Mo( p, e);
                        default:
                            return null;
                    }
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

    public static boolean isEmailRegistered(String email) {
    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
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

}

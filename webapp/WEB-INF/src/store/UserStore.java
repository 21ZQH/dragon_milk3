package store;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.Admin;
import model.Course;
import model.Mo;
import model.ResumeSubmission;
import model.TA;
import model.User;

/**
 * Data access layer for persisting and retrieving user accounts.
 *
 * <p>Users are stored as CSV records in a file-based store. This class provides
 * operations for reading, writing, and updating {@link User} records including
 * TAs, MOs, and Admins. It handles the serialization and deserialization of
 * user fields, applied courses, and resume submission mappings.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public class UserStore {
    /** System property key for overriding the default user store file path. */
    public static final String FILE_PATH_PROPERTY = "user.store.path";

    /**
     * Retrieves all TA users from the store, with their applied courses resolved
     * from the available courses list.
     *
     * @return a list of all TA users with populated course data
     */
    public static List<TA> getTAList() {
        List<Course> availableCourses = CourseStore.getCourseList();
        return getTaUsersForCourses(availableCourses);
    }

    /**
     * Retrieves all MO users from the store, with their owned courses resolved
     * from the available courses list.
     *
     * @return a list of all MO users with populated course data
     */
    public static List<Mo> getMOList() {
        List<Course> availableCourses = CourseStore.getCourseList();
        List<Mo> moUsers = new ArrayList<>();
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return moUsers;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine == null || !"Mo".equals(parsedLine.role)) {
                    continue;
                }

                User user = buildUser(parsedLine, availableCourses);
                if (user instanceof Mo mo) {
                    moUsers.add(mo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return moUsers;
    }

    /**
     * Saves a new user record to the store file.
     *
     * @param user the user to save (must be an instance of TA, Mo, or Admin)
     */
    public static void saveUser(User user) {
        String line = toLine(user);
        Path filePath = resolveFilePath();

        try {
            ensureParentDirectoryExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (FileWriter fw = new FileWriter(filePath.toFile(), true)) {
            fw.write(line + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates user credentials by matching password, role, and email.
     *
     * @param password the password to validate
     * @param role     the expected role of the user
     * @param email    the email address to look up
     * @return the matching {@link User} if credentials are valid, or {@code null}
     *         if no match is found
     */
    public static User validateUser(String password, String role, String email) {
        return findUser(password, email, role, true);
    }

    /**
     * Validates user credentials by matching password and email without role check.
     *
     * @param password the password to validate
     * @param email    the email address to look up
     * @return the matching {@link User} if credentials are valid, or {@code null}
     *         if no match is found
     */
    public static User validateUser(String password, String email) {
        return findUser(password, email, null, false);
    }

    /**
     * Checks whether an email address is already registered in the system.
     *
     * @param email the email address to check
     * @return {@code true} if the email is already registered, {@code false} otherwise
     */
    public static boolean isEmailRegistered(String email) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return false;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine != null && email.equals(parsedLine.email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the applied course IDs for a TA in the store.
     *
     * @param ta the TA whose applied course data should be updated
     */
    public static void updateAppliedCourseIds(TA ta) {
        updateUserLine("TA", ta.getEmail(), toTaLine(ta));
    }

    /**
     * Updates the profile information for a TA in the store.
     *
     * @param ta the TA whose profile data should be updated
     */
    public static void updateTaProfile(TA ta) {
        updateUserLine("TA", ta.getEmail(), toTaLine(ta));
    }

    /**
     * Updates the profile information for an MO in the store.
     *
     * @param mo the MO whose profile data should be updated
     */
    public static void updateMoProfile(Mo mo) {
        updateUserLine("Mo", mo.getEmail(), toMoLine(mo));
    }

    /**
     * Updates the owned course IDs for an MO in the store.
     *
     * @param mo the MO whose owned course data should be updated
     */
    public static void updateOwnedCourseIds(Mo mo) {
        updateUserLine("Mo", mo.getEmail(), toMoLine(mo));
    }

    /**
     * Resets the application state for all TA users.
     * <p>This clears the applied course IDs and resume mappings for every TA
     * record, used when starting a new recruitment cycle.</p>
     */
    public static void resetTaApplicationState() {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return;
        }

        List<String> resetLines = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(filePath)) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine == null) {
                    resetLines.add(line);
                    continue;
                }

                if ("TA".equals(parsedLine.role)) {
                    parsedLine.appliedCourseIds = "";
                    parsedLine.resumeMappings = "";
                    resetLines.add(toLine(buildUser(parsedLine, List.of())));
                } else {
                    resetLines.add(line);
                }
            }
            ensureParentDirectoryExists(filePath);
            Files.write(filePath, resetLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves TA users whose applied courses can be resolved from the provided
     * course list.
     *
     * @param availableCourses the list of available courses for resolving references
     * @return a list of TA users with resolved course data
     */
    static List<TA> getTaUsersForCourses(List<Course> availableCourses) {
        List<TA> taUsers = new ArrayList<>();
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return taUsers;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine == null || !"TA".equals(parsedLine.role)) {
                    continue;
                }

                User user = buildUser(parsedLine, availableCourses);
                if (user instanceof TA ta) {
                    taUsers.add(ta);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return taUsers;
    }

    /**
     * Finds a user by matching credentials.
     *
     * @param password  the password to match
     * @param email     the email address to match
     * @param role      the expected role (can be {@code null} if {@code matchRole} is false)
     * @param matchRole whether to enforce role matching
     * @return the matching {@link User}, or {@code null} if not found
     */
    private static User findUser(String password, String email, String role, boolean matchRole) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            return null;
        }

        List<Course> availableCourses = CourseStore.getCourseList();

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine == null) {
                    continue;
                }

                boolean passwordMatches = password.equals(parsedLine.password);
                boolean emailMatches = email.equals(parsedLine.email);
                boolean roleMatches = !matchRole || role.equals(parsedLine.role);

                if (passwordMatches && emailMatches && roleMatches) {
                    return buildUser(parsedLine, availableCourses);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Updates a single user line in the store file identified by role and email.
     * <p>If the user is not found, the new line is appended to the file.</p>
     *
     * @param role            the role of the user to update
     * @param email           the email of the user to update
     * @param replacementLine the new CSV line to write
     */
    private static void updateUserLine(String role, String email, String replacementLine) {
        Path filePath = resolveFilePath();
        if (!Files.exists(filePath)) {
            try {
                ensureParentDirectoryExists(filePath);
                Files.write(filePath, List.of(replacementLine));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        List<String> updatedLines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                ParsedUserLine parsedLine = parseUserLine(line);
                if (parsedLine != null && role.equals(parsedLine.role) && email.equals(parsedLine.email)) {
                    updatedLines.add(replacementLine);
                    updated = true;
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (!updated) {
            updatedLines.add(replacementLine);
        }

        try {
            ensureParentDirectoryExists(filePath);
            Files.write(filePath, updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a {@link User} object from parsed CSV data.
     *
     * @param parsedLine       the parsed user line data
     * @param availableCourses the list of available courses for resolving references
     * @return the constructed user, or {@code null} if the role is unknown
     */
    private static User buildUser(ParsedUserLine parsedLine, List<Course> availableCourses) {
        User user = switch (parsedLine.role) {
            case "Admin" -> new Admin(parsedLine.password, parsedLine.email);
            case "TA" -> new TA(parsedLine.password, parsedLine.email);
            case "Mo" -> new Mo(parsedLine.password, parsedLine.email);
            default -> null;
        };

        if (user == null) {
            return null;
        }

        if (parsedLine.name != null && !parsedLine.name.isBlank()) {
            user.setName(parsedLine.name);
        }

        if (user instanceof TA ta) {
            ta.setCollege(parsedLine.taCollege);
            ta.setSkill(parsedLine.taSkill);
            ta.setMasterResumeDirectory(parsedLine.masterResumeDirectory);
            if (!parsedLine.appliedCourseIds.isBlank()) {
                ta.setAppliedClasses(resolveCourses(parsedLine.appliedCourseIds, availableCourses));
            }
            if (!parsedLine.resumeMappings.isBlank()) {
                ta.setResumeSubmissions(resolveResumeSubmissions(parsedLine.resumeMappings, availableCourses));
            }
        }

        if (user instanceof Mo mo) {
            mo.setDegree(parsedLine.moDegree);
            mo.setCollege(parsedLine.moCollege);
            if (!parsedLine.ownedCourseIds.isBlank()) {
                mo.setOwnedCourses(resolveCourses(parsedLine.ownedCourseIds, availableCourses));
            }
        }

        return user;
    }

    /**
     * Parses a single CSV line into a {@link ParsedUserLine} structure.
     *
     * @param line the CSV line to parse
     * @return a parsed user line, or {@code null} if the line is invalid
     */
    private static ParsedUserLine parseUserLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String[] parts = CsvRecord.parse(line);
        if (parts.length < 4) {
            return null;
        }

        ParsedUserLine parsedLine = new ParsedUserLine();
        parsedLine.name = parts[0];
        parsedLine.password = parts[1];
        parsedLine.role = parts[2];
        parsedLine.email = parts[3];

        if ("TA".equals(parsedLine.role)) {
            parseTaFields(parsedLine, parts);
        } else if ("Mo".equals(parsedLine.role)) {
            parseMoFields(parsedLine, parts);
        }

        return parsedLine;
    }

    /**
     * Parses TA-specific fields from the CSV parts.
     *
     * @param parsedLine the parsed user line to populate
     * @param parts      the CSV field array
     */
    private static void parseTaFields(ParsedUserLine parsedLine, String[] parts) {
        if (parts.length >= 8) {
            parsedLine.taCollege = parts[4];
            parsedLine.taSkill = parts[5];
            parsedLine.appliedCourseIds = parts[6];
            parsedLine.resumeMappings = parts[7];
            if (parts.length >= 9) {
                parsedLine.masterResumeDirectory = parts[8];
            }
            return;
        }

        if (parts.length >= 6) {
            parsedLine.appliedCourseIds = parts[4];
            parsedLine.resumeMappings = parts[5];
            return;
        }

        if (parts.length >= 5) {
            parsedLine.appliedCourseIds = parts[4];
        }
    }

    /**
     * Parses MO-specific fields from the CSV parts.
     *
     * @param parsedLine the parsed user line to populate
     * @param parts      the CSV field array
     */
    private static void parseMoFields(ParsedUserLine parsedLine, String[] parts) {
        if (parts.length >= 7) {
            parsedLine.moDegree = parts[4];
            parsedLine.moCollege = parts[5];
            parsedLine.ownedCourseIds = parts[6];
            return;
        }

        if (parts.length >= 5) {
            parsedLine.ownedCourseIds = parts[4];
        }
    }

    /**
     * Converts a user object to its CSV line representation.
     *
     * @param user the user to convert
     * @return the CSV line string
     */
    private static String toLine(User user) {
        if (user instanceof TA ta) {
            return toTaLine(ta);
        }
        if (user instanceof Mo mo) {
            return toMoLine(mo);
        }
        return toAdminLine(user);
    }

    /**
     * Converts an Admin user to a CSV line.
     *
     * @param user the admin user to convert
     * @return the CSV line string
     */
    private static String toAdminLine(User user) {
        return baseLine(user);
    }

    /**
     * Converts a TA user to a CSV line including TA-specific fields.
     *
     * @param ta the TA user to convert
     * @return the CSV line string
     */
    private static String toTaLine(TA ta) {
        String line = baseLine(ta) + "," + CsvRecord.field(safe(ta.getCollege())) + ","
                + CsvRecord.field(safe(ta.getSkill())) + ","
                + CsvRecord.field(serializeAppliedCourseIds(ta)) + ","
                + CsvRecord.field(serializeResumeSubmissions(ta));
        String masterResumeDirectory = safe(ta.getMasterResumeDirectory());
        if (!masterResumeDirectory.isBlank()) {
            line += "," + CsvRecord.field(masterResumeDirectory);
        }
        return line;
    }

    /**
     * Converts an MO user to a CSV line including MO-specific fields.
     *
     * @param mo the MO user to convert
     * @return the CSV line string
     */
    private static String toMoLine(Mo mo) {
        String degree = safe(mo.getDegree());
        String college = safe(mo.getCollege());
        String ownedCourseIds = serializeOwnedCourseIds(mo);
        if (degree.isBlank() && college.isBlank()) {
            return baseLine(mo) + "," + CsvRecord.field(ownedCourseIds);
        }
        return baseLine(mo) + "," + CsvRecord.field(degree) + "," + CsvRecord.field(college)
                + "," + CsvRecord.field(ownedCourseIds);
    }

    /**
     * Creates the base CSV line with common user fields.
     *
     * @param user the user to convert
     * @return the base CSV line string
     */
    private static String baseLine(User user) {
        return CsvRecord.toLine(
                safe(user.getName()),
                safe(user.getPassword()),
                safe(user.getRole()),
                safe(user.getEmail()));
    }

    /**
     * Resolves the user store file path from system properties or defaults.
     *
     * @return the resolved file path
     */
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

    /**
     * Serializes the owned course IDs of an MO into a pipe-delimited string.
     *
     * @param mo the MO whose owned course IDs to serialize
     * @return the serialized course ID string
     */
    private static String serializeOwnedCourseIds(Mo mo) {
        return mo.getOwnedCourses().stream()
                .map(Course::getId)
                .distinct()
                .collect(Collectors.joining("|"));
    }

    /**
     * Serializes the applied course IDs of a TA into a pipe-delimited string.
     *
     * @param ta the TA whose applied course IDs to serialize
     * @return the serialized course ID string
     */
    private static String serializeAppliedCourseIds(TA ta) {
        return ta.getAppliedClasses().stream()
                .map(Course::getId)
                .distinct()
                .collect(Collectors.joining("|"));
    }

    /**
     * Serializes the resume submissions of a TA into a pipe-delimited string.
     * <p>Each submission is encoded as {@code courseId@status@reviewUnread}.</p>
     *
     * @param ta the TA whose resume submissions to serialize
     * @return the serialized resume mappings string
     */
    private static String serializeResumeSubmissions(TA ta) {
        return ta.getResumeSubmissions().stream()
                .filter(submission -> submission.getCourse() != null)
                .map(submission -> submission.getCourse().getId() + "@"
                        + submission.getStatus() + "@"
                        + submission.isReviewUnread())
                .collect(Collectors.joining("|"));
    }

    /**
     * Resolves a pipe-delimited string of course IDs into a list of Course objects.
     *
     * @param courseIds        the pipe-delimited course ID string
     * @param availableCourses the list of available courses to resolve against
     * @return the resolved list of Course objects
     */
    private static List<Course> resolveCourses(String courseIds, List<Course> availableCourses) {
        List<Course> resolvedCourses = new ArrayList<>();
        for (String courseId : courseIds.split("\\|")) {
            if (courseId == null || courseId.isBlank()) {
                continue;
            }
            for (Course course : availableCourses) {
                if (courseId.equals(course.getId())) {
                    resolvedCourses.add(course);
                    break;
                }
            }
        }
        return resolvedCourses;
    }

    /**
     * Resolves a pipe-delimited string of resume mappings into a list of
     * {@link ResumeSubmission} objects.
     *
     * @param resumeMappings   the pipe-delimited resume mapping string
     * @param availableCourses the list of available courses to resolve against
     * @return the resolved list of ResumeSubmission objects
     */
    private static List<ResumeSubmission> resolveResumeSubmissions(String resumeMappings, List<Course> availableCourses) {
        List<ResumeSubmission> submissions = new ArrayList<>();
        for (String mapping : resumeMappings.split("\\|")) {
            if (mapping == null || mapping.isBlank()) {
                continue;
            }

            String[] parts = mapping.split("@", 3);
            if (parts.length < 1) {
                continue;
            }

            String courseId = parts[0];
            int status = ResumeSubmission.STATUS_PENDING;
            boolean reviewUnread = false;

            if (courseId == null || courseId.isBlank()) {
                continue;
            }

            if (parts.length >= 2) {
                try {
                    status = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                    status = ResumeSubmission.STATUS_PENDING;
                }
            }

            if (parts.length == 3) {
                reviewUnread = Boolean.parseBoolean(parts[2]);
            }

            for (Course course : availableCourses) {
                if (courseId.equals(course.getId())) {
                    submissions.add(new ResumeSubmission(course, buildApplicationFormId(courseId), status, reviewUnread));
                    break;
                }
            }
        }
        return submissions;
    }

    /**
     * Builds an application form identifier from a course ID.
     * <p>Currently returns the course ID directly as the form identifier.</p>
     *
     * @param courseId the course identifier
     * @return the application form identifier
     */
    private static String buildApplicationFormId(String courseId) {
        return courseId;
    }

    /**
     * Returns the given value or an empty string if it is {@code null}.
     *
     * @param value the value to check
     * @return the original value if non-null, or an empty string otherwise
     */
    private static String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Internal structure representing a parsed user line from the CSV store.
     * <p>Contains all common and role-specific fields extracted from a single
     * CSV record.</p>
     */
    private static final class ParsedUserLine {
        private String name = "";
        private String password = "";
        private String role = "";
        private String email = "";
        private String taCollege = "";
        private String taSkill = "";
        private String moDegree = "";
        private String moCollege = "";
        private String ownedCourseIds = "";
        private String appliedCourseIds = "";
        private String resumeMappings = "";
        private String masterResumeDirectory = "";
    }
}

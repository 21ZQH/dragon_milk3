package store;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Candidate;

public class CandidateStore {
    public static final String FILE_PATH_PROPERTY = "candidate.store.path";

    public static List<Candidate> getCandidateList() {
        Path filePath = resolveFilePath();
        ensureSeedData(filePath);

        List<Candidate> candidateList = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return candidateList;
        }

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length >= 3) {
                    boolean picked = parts.length == 4 && Boolean.parseBoolean(parts[3].trim());
                    candidateList.add(new Candidate(parts[0].trim(), parts[1].trim(), parts[2].trim(), picked));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return candidateList;
    }

    public static void savePickedIndexes(String[] pickedIndexes) {
        List<Candidate> candidateList = getCandidateList();
        Set<Integer> pickedIndexSet = new HashSet<>();

        if (pickedIndexes != null) {
            for (String value : pickedIndexes) {
                try {
                    pickedIndexSet.add(Integer.parseInt(value));
                } catch (NumberFormatException ignored) {
                    // Ignore invalid indexes from request.
                }
            }
        }

        List<String> linesToWrite = new ArrayList<>();
        for (int i = 0; i < candidateList.size(); i++) {
            Candidate candidate = candidateList.get(i);
            boolean picked = pickedIndexSet.contains(i);
            candidate.setPicked(picked);
            linesToWrite.add(candidate.getName() + ","
                    + candidate.getEducation() + ","
                    + candidate.getDetails() + ","
                    + picked);
        }

        Path filePath = resolveFilePath();
        try {
            Path parentPath = filePath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }
            Files.write(filePath, linesToWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path resolveFilePath() {
        String overridePath = System.getProperty(FILE_PATH_PROPERTY);
        if (overridePath != null && !overridePath.isBlank()) {
            return Paths.get(overridePath);
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "webapps", "SE", "WEB-INF", "file", "candidates.txt");
        }

        return Paths.get(System.getProperty("user.dir"), "webapp", "WEB-INF", "file", "candidates.txt");
    }

    private static void ensureSeedData(Path filePath) {
        try {
            Path parentPath = filePath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }

            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                List<String> seedData = List.of(
                        "Alice Zhang,BSc in Computer Science,Strong Java coding ability and has one year of tutoring experience,false",
                        "Brian Li,MSc in Software Engineering,Experienced in grading assignments and leading lab sessions,false",
                        "Cindy Wang,BSc in Information Systems,Good communication skills and active in student projects,false",
                        "David Chen,MSc in Data Science,Familiar with Python and SQL and can support data-related courses,false",
                        "Eva Liu,BSc in Mathematics,Excellent logic skills and patient in answering student questions,false",
                        "Frank Sun,BSc in Artificial Intelligence,Served as TA in machine learning labs,false",
                        "Grace Xu,MSc in Human Computer Interaction,Strong UI design and usability testing background,false",
                        "Henry Zhao,BSc in Cybersecurity,Can support network security and system hardening classes,false",
                        "Iris Gao,MSc in Education Technology,Skilled in LMS operations and teaching support,false",
                        "Jason Wu,BSc in Software Engineering,Good at code review and debugging support,false",
                        "Kelly Ma,MSc in Computer Networks,Experienced in router and protocol experiments,false",
                        "Leo Tang,BSc in Data Analytics,Can assist with statistics and visualization tasks,false",
                        "Mia Feng,MSc in Cloud Computing,Familiar with Docker and deployment pipelines,false",
                        "Nick He,BSc in Information Security,Strong incident response and log analysis abilities,false",
                        "Olivia Ren,MSc in Computer Vision,Can support OpenCV and project mentoring,false",
                        "Peter Qiu,BSc in Embedded Systems,Experienced in C programming and microcontroller labs,false",
                        "Queenie Lin,MSc in Software Testing,Focused on QA process and test case design,false",
                        "Ryan Zhou,BSc in Computer Science,Strong algorithm foundation and tutoring patience,false",
                        "Sophie Yao,MSc in Big Data,Can guide Spark and Hadoop assignments,false",
                        "Tony Hu,BSc in Digital Media Technology,Can support multimedia course projects,false");
                Files.write(filePath, seedData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

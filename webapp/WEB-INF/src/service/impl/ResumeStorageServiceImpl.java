package service.impl;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.http.Part;
import model.Course;
import model.TA;
import service.ResumeStorageService;

/**
 * Implementation of the {@link ResumeStorageService} interface.
 * Provides concrete file-system operations for managing TA resume
 * files, including storing uploaded resumes, resolving file paths,
 * and cleaning up stored files. Supports both per-course and master
 * resume directories.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class ResumeStorageServiceImpl implements ResumeStorageService {

    /**
     * Builds the stored resume file name for the given TA by normalizing
     * the email address and appending a {@code .pdf} extension.
     *
     * @param ta the TA whose resume file name is to be built
     * @return the normalized resume file name
     */
    @Override
    public String buildStoredResumeFileName(TA ta) {
        String normalizedEmail = ta == null || ta.getEmail() == null || ta.getEmail().trim().isEmpty()
                ? "unknown"
                : ta.getEmail().replaceAll("[^a-zA-Z0-9._-]", "_");
        return normalizedEmail + ".pdf";
    }

    /**
     * Resolves the base directory for resume uploads. Prefers the
     * {@code catalina.base} system property when running inside a Tomcat
     * container; falls back to the current working directory otherwise.
     *
     * @return the absolute path to the resume upload directory
     */
    @Override
    public String resolveResumeUploadDirectory() {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return catalinaBase + File.separator + "webapps" + File.separator + "SE"
                    + File.separator + "WEB-INF" + File.separator + "file" + File.separator + "resume";
        }

        return System.getProperty("user.dir") + File.separator + "webapp"
                + File.separator + "WEB-INF" + File.separator + "file" + File.separator + "resume";
    }

    /**
     * Constructs a {@link File} object representing the resume file for the
     * given applicant within the specified directory.
     *
     * @param applicant       the TA applicant
     * @param resumeDirectory the directory containing the resume file
     * @return the resume {@link File}, or {@code null} if inputs are invalid
     */
    @Override
    public File buildResumeFile(TA applicant, String resumeDirectory) {
        if (applicant == null || applicant.getEmail() == null || applicant.getEmail().isBlank()
                || resumeDirectory == null || resumeDirectory.isBlank()) {
            return null;
        }
        return new File(resumeDirectory, buildStoredResumeFileName(applicant));
    }

    /**
     * Retrieves the resume file for a TA for a specific course.
     *
     * @param ta       the TA applicant
     * @param courseId the unique identifier of the course
     * @return the resume {@link File}, or {@code null} if not found
     */
    @Override
    public File getTaResumeFile(TA ta, String courseId) {
        if (ta == null || courseId == null || courseId.isBlank()) {
            return null;
        }

        String resumeDirectory = ta.getResumeDirectoryForCourse(courseId);
        if (resumeDirectory == null || resumeDirectory.isBlank()) {
            return null;
        }
        return buildResumeFile(ta, resumeDirectory);
    }

    /**
     * Retrieves the master resume file for a TA.
     *
     * @param ta the TA whose master resume is to be retrieved
     * @return the master resume {@link File}, or {@code null} if not set
     */
    @Override
    public File getMasterResumeFile(TA ta) {
        if (ta == null || ta.getMasterResumeDirectory() == null || ta.getMasterResumeDirectory().isBlank()) {
            return null;
        }
        return buildResumeFile(ta, ta.getMasterResumeDirectory());
    }

    /**
     * Retrieves the resume file for a given applicant email within a course.
     *
     * @param course           the course the applicant applied to
     * @param applicantEmail   the email address of the applicant
     * @return the applicant's resume {@link File}, or {@code null} if not found
     */
    @Override
    public File getApplicantResumeFile(Course course, String applicantEmail) {
        if (course == null || applicantEmail == null || applicantEmail.isBlank()) {
            return null;
        }

        for (int i = 0; i < course.getTaApplicants().size(); i++) {
            TA applicant = course.getTaApplicants().get(i);
            if (applicant == null || !applicantEmail.equals(applicant.getEmail())) {
                continue;
            }

            String resumeDirectory = applicant.getResumeDirectoryForCourse(course.getId());
            if ((resumeDirectory == null || resumeDirectory.isBlank()) && i < course.getApplicantFormIds().size()) {
                resumeDirectory = course.getApplicantFormIds().get(i);
            }
            return buildResumeFile(applicant, resumeDirectory);
        }
        return null;
    }

    /**
     * Retrieves the resume file for a given email and course ID combination.
     *
     * @param email    the email address of the applicant
     * @param courseId the unique identifier of the course
     * @return the resume {@link File}, or {@code null} if inputs are invalid
     */
    @Override
    public File getResumeFile(String email, String courseId) {
        if (email == null || courseId == null) {
            return null;
        }

        String normalizedEmail = email.replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File(resolveResumeUploadDirectory() + File.separator + courseId, normalizedEmail + ".pdf");
    }

    /**
     * Stores an uploaded resume part for a TA under the given course.
     * Creates the necessary directory structure if it does not exist.
     *
     * @param resumePart the uploaded file part
     * @param ta         the TA applicant
     * @param course     the course the resume is for
     * @return the absolute path of the directory where the resume was stored
     * @throws IOException if directory creation or file writing fails
     */
    @Override
    public String storeResume(Part resumePart, TA ta, Course course) throws IOException {
        String courseDirectoryName = course == null || course.getId() == null || course.getId().trim().isEmpty()
                ? "unknown-course"
                : course.getId();
        File uploadDirectory = new File(resolveResumeUploadDirectory(), courseDirectoryName);
        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            throw new IOException("Failed to create upload directory.");
        }

        File targetFile = new File(uploadDirectory, buildStoredResumeFileName(ta));
        if (targetFile.exists() && !targetFile.delete()) {
            throw new IOException("Failed to replace existing resume file.");
        }
        resumePart.write(targetFile.getAbsolutePath());
        return uploadDirectory.getAbsolutePath();
    }

    /**
     * Stores an uploaded resume part as the TA's master resume in a
     * dedicated directory.
     *
     * @param resumePart the uploaded file part
     * @param ta         the TA whose master resume is being stored
     * @return the absolute path of the master resume directory
     * @throws IOException if directory creation or file writing fails
     */
    @Override
    public String storeMasterResume(Part resumePart, TA ta) throws IOException {
        File uploadDirectory = new File(resolveResumeUploadDirectory(), "master");
        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            throw new IOException("Failed to create upload directory.");
        }

        File targetFile = new File(uploadDirectory, buildStoredResumeFileName(ta));
        if (targetFile.exists() && !targetFile.delete()) {
            throw new IOException("Failed to replace existing resume file.");
        }
        resumePart.write(targetFile.getAbsolutePath());
        return uploadDirectory.getAbsolutePath();
    }

    /**
     * Deletes the stored resume file for a TA in the given directory.
     * Also removes the parent directory if it becomes empty after deletion.
     *
     * @param ta              the TA whose resume file should be deleted
     * @param resumeDirectory the directory containing the resume file
     * @return {@code true} if the deletion succeeded or no action was needed,
     *         {@code false} if the file exists but could not be deleted
     */
    @Override
    public boolean deleteStoredResumeFileIfPresent(TA ta, String resumeDirectory) {
        if (ta == null || resumeDirectory == null || resumeDirectory.isBlank()) {
            return true;
        }

        File resumeFile = new File(resumeDirectory, buildStoredResumeFileName(ta));
        if (resumeFile.exists() && !resumeFile.delete()) {
            return false;
        }

        File directory = new File(resumeDirectory);
        String[] files = directory.list();
        if (directory.exists() && directory.isDirectory() && files != null && files.length == 0) {
            directory.delete();
        }
        return true;
    }
}

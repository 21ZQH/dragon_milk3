package service;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.http.Part;
import model.Course;
import model.TA;

/**
 * Service interface for handling resume file storage operations within
 * the TA recruitment system. Provides methods for building file paths,
 * storing uploaded resumes (course-specific and master), retrieving
 * resume files, and deleting stored resume files.
 *
 * @version 1.0
 * @since 2025
 */
public interface ResumeStorageService {
    /**
     * Constructs the stored file name for a TA's resume based on their profile.
     *
     * @param ta the TA whose resume file name is to be built
     * @return a string representing the stored resume file name
     */
    String buildStoredResumeFileName(TA ta);

    /**
     * Resolves the base directory path for storing uploaded resumes.
     *
     * @return the absolute path of the resume upload directory
     */
    String resolveResumeUploadDirectory();

    /**
     * Builds a {@link File} reference for a TA's resume in the specified directory.
     *
     * @param applicant       the TA applicant
     * @param resumeDirectory the directory where resumes are stored
     * @return a {@link File} object pointing to the TA's resume
     */
    File buildResumeFile(TA applicant, String resumeDirectory);

    /**
     * Retrieves the resume file for a TA associated with a specific course.
     *
     * @param ta       the TA whose resume to retrieve
     * @param courseId the course identifier
     * @return the {@link File} object for the course-specific resume
     */
    File getTaResumeFile(TA ta, String courseId);

    /**
     * Retrieves the master resume file for a TA.
     *
     * @param ta the TA whose master resume to retrieve
     * @return the {@link File} object for the master resume
     */
    File getMasterResumeFile(TA ta);

    /**
     * Retrieves the resume file for a specific applicant and course.
     *
     * @param course         the course associated with the application
     * @param applicantEmail the email address of the applicant
     * @return the {@link File} object for the applicant's resume
     */
    File getApplicantResumeFile(Course course, String applicantEmail);

    /**
     * Retrieves the resume file for a given email and course combination.
     *
     * @param email    the email address of the applicant
     * @param courseId the course identifier
     * @return the {@link File} object for the resume
     */
    File getResumeFile(String email, String courseId);

    /**
     * Stores an uploaded resume file for a TA applying to a specific course.
     *
     * @param resumePart the uploaded resume file part from the HTTP request
     * @param ta         the TA submitting the resume
     * @param course     the course being applied to
     * @return the file name under which the resume was stored
     * @throws IOException if an I/O error occurs during file storage
     */
    String storeResume(Part resumePart, TA ta, Course course) throws IOException;

    /**
     * Stores an uploaded master resume file for a TA.
     *
     * @param resumePart the uploaded resume file part from the HTTP request
     * @param ta         the TA submitting the master resume
     * @return the file name under which the master resume was stored
     * @throws IOException if an I/O error occurs during file storage
     */
    String storeMasterResume(Part resumePart, TA ta) throws IOException;

    /**
     * Deletes the stored resume file for a TA if it exists in the given directory.
     *
     * @param ta              the TA whose resume file to delete
     * @param resumeDirectory the directory containing the resume file
     * @return {@code true} if the file was successfully deleted or did not exist,
     *         {@code false} otherwise
     */
    boolean deleteStoredResumeFileIfPresent(TA ta, String resumeDirectory);
}

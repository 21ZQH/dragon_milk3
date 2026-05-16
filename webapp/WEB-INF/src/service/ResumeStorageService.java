package service;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.http.Part;
import model.Course;
import model.TA;

public interface ResumeStorageService {
    String buildStoredResumeFileName(TA ta);

    String resolveResumeUploadDirectory();

    File buildResumeFile(TA applicant, String resumeDirectory);

    File getTaResumeFile(TA ta, String courseId);

    File getApplicantResumeFile(Course course, String applicantEmail);

    File getResumeFile(String email, String courseId);

    String storeResume(Part resumePart, TA ta, Course course) throws IOException;

    boolean deleteStoredResumeFileIfPresent(TA ta, String resumeDirectory);
}

package service.impl;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.http.Part;
import model.Course;
import model.TA;
import service.ResumeStorageService;

public class ResumeStorageServiceImpl implements ResumeStorageService {
    @Override
    public String buildStoredResumeFileName(TA ta) {
        String normalizedEmail = ta == null || ta.getEmail() == null || ta.getEmail().trim().isEmpty()
                ? "unknown"
                : ta.getEmail().replaceAll("[^a-zA-Z0-9._-]", "_");
        return normalizedEmail + ".pdf";
    }

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

    @Override
    public File buildResumeFile(TA applicant, String resumeDirectory) {
        if (applicant == null || applicant.getEmail() == null || applicant.getEmail().isBlank()
                || resumeDirectory == null || resumeDirectory.isBlank()) {
            return null;
        }
        return new File(resumeDirectory, buildStoredResumeFileName(applicant));
    }

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

    @Override
    public File getMasterResumeFile(TA ta) {
        if (ta == null || ta.getMasterResumeDirectory() == null || ta.getMasterResumeDirectory().isBlank()) {
            return null;
        }
        return buildResumeFile(ta, ta.getMasterResumeDirectory());
    }

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

    @Override
    public File getResumeFile(String email, String courseId) {
        if (email == null || courseId == null) {
            return null;
        }

        String normalizedEmail = email.replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File(resolveResumeUploadDirectory() + File.separator + courseId, normalizedEmail + ".pdf");
    }

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

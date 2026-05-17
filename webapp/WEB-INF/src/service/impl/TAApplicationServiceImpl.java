package service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.Part;
import model.Course;
import model.ResumeSubmission;
import model.TA;
import service.ResumeStorageService;
import service.TAApplicationService;
import service.UserProfileService;

public class TAApplicationServiceImpl implements TAApplicationService {
    private final UserProfileService userProfileService;
    private final ResumeStorageService resumeStorageService;

    public TAApplicationServiceImpl(UserProfileService userProfileService, ResumeStorageService resumeStorageService) {
        this.userProfileService = userProfileService;
        this.resumeStorageService = resumeStorageService;
    }

    @Override
    public Course getCourseByIndex(List<Course> courseList, String courseIndexParam) {
        if (courseIndexParam == null || courseList == null) {
            return null;
        }
        try {
            int courseIndex = Integer.parseInt(courseIndexParam);
            if (courseIndex < 0 || courseIndex >= courseList.size()) {
                return null;
            }
            return courseList.get(courseIndex);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Course getCourseById(List<Course> courseList, String courseId) {
        if (courseList == null || courseId == null || courseId.isBlank()) {
            return null;
        }
        for (Course course : courseList) {
            if (course != null && courseId.equals(course.getId())) {
                return course;
            }
        }
        return null;
    }

    @Override
    public Integer findCourseIndexById(List<Course> courseList, String courseId) {
        if (courseList == null || courseId == null || courseId.isBlank()) {
            return null;
        }
        for (int i = 0; i < courseList.size(); i++) {
            Course course = courseList.get(i);
            if (course != null && courseId.equals(course.getId())) {
                return i;
            }
        }
        return null;
    }

    @Override
    public boolean hasCurrentResume(TA ta, Course course) {
        if (course == null) {
            return false;
        }
        File resumeFile = resumeStorageService.getTaResumeFile(ta, course.getId());
        return resumeFile != null && resumeFile.exists() && resumeFile.isFile();
    }

    @Override
    public SubmitResumeResult submitResume(TA ta, Course course, Part resumePart) throws IOException {
        String resumeDirectory = ta == null ? null : ta.getMasterResumeDirectory();
        String submittedFileName = "profile resume";

        if (resumePart != null && resumePart.getSize() > 0) {
            SubmitResumeResult uploadResult = uploadMasterResume(ta, resumePart);
            if (!uploadResult.isSuccess()) {
                return uploadResult;
            }
            resumeDirectory = ta.getMasterResumeDirectory();
            submittedFileName = uploadResult.getSubmittedFileName();
        }

        if (resumeDirectory == null || resumeDirectory.isBlank()) {
            return SubmitResumeResult.error("Please upload your resume before submitting.");
        }

        String applicationFormId = course.getId();
        ta.addOrUpdateApplication(course, applicationFormId, ResumeSubmission.STATUS_PENDING);
        course.addApplication(ta, applicationFormId);
        userProfileService.updateAppliedCourseIds(ta);
        return SubmitResumeResult.success(submittedFileName);
    }

    @Override
    public SubmitResumeResult uploadMasterResume(TA ta, Part resumePart) throws IOException {
        if (resumePart == null || resumePart.getSize() <= 0) {
            return SubmitResumeResult.error("Please upload your resume before submitting.");
        }
        String submittedFileName = resumePart.getSubmittedFileName();
        submittedFileName = submittedFileName == null ? "" : new File(submittedFileName).getName();
        if (!submittedFileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return SubmitResumeResult.error("Only PDF resumes are accepted.");
        }

        String resumeDirectory = resumeStorageService.storeMasterResume(resumePart, ta);
        ta.setMasterResumeDirectory(resumeDirectory);
        userProfileService.updateTaProfile(ta);
        return SubmitResumeResult.success(submittedFileName);
    }

    @Override
    public void updateProfile(TA ta, String name, String college, boolean skillFormSubmitted, String[] selectedSkills) {
        ta.setName(trimValue(name));
        ta.setCollege(trimValue(college));

        if (skillFormSubmitted) {
            List<String> skills = new ArrayList<>();
            if (selectedSkills != null) {
                for (String skill : selectedSkills) {
                    String trimmedSkill = trimValue(skill);
                    if (!trimmedSkill.isEmpty()) {
                        skills.add(trimmedSkill);
                    }
                }
            }
            ta.setSkill(String.join(", ", skills));
        }
        userProfileService.updateTaProfile(ta);
    }

    @Override
    public WithdrawApplicationResult withdrawApplication(TA ta, Course course) {
        if (ta == null || course == null) {
            return new WithdrawApplicationResult(true);
        }
        ta.withdrawApplication(course.getId());
        course.removeApplicationByTaEmail(ta.getEmail());
        userProfileService.updateAppliedCourseIds(ta);
        return new WithdrawApplicationResult(true);
    }

    @Override
    public PersonalCentreData preparePersonalCentreData(TA ta, String selectedCourseId, boolean applicationOpen) {
        if (ta.markAllReviewUpdatesRead()) {
            userProfileService.updateAppliedCourseIds(ta);
        }
        List<Course> appliedCourses = ta.getAppliedClasses();
        Course selectedCourse = resolveSelectedAppliedCourse(appliedCourses, selectedCourseId);
        Integer selectedStatus = selectedCourse == null ? null : resolveResumeStatus(ta, selectedCourse.getId());
        return new PersonalCentreData(appliedCourses, selectedCourse, selectedStatus, applicationOpen);
    }

    private Course resolveSelectedAppliedCourse(List<Course> appliedCourses, String selectedCourseId) {
        if (appliedCourses == null || appliedCourses.isEmpty()) {
            return null;
        }
        if (selectedCourseId != null && !selectedCourseId.isBlank()) {
            for (Course course : appliedCourses) {
                if (course != null && selectedCourseId.equals(course.getId())) {
                    return course;
                }
            }
        }
        return appliedCourses.get(0);
    }

    private int resolveResumeStatus(TA ta, String courseId) {
        Integer status = ta.getResumeStatusForCourse(courseId);
        return status == null ? ResumeSubmission.STATUS_PENDING : status;
    }

    private String trimValue(String value) {
        return value == null ? "" : value.trim();
    }
}

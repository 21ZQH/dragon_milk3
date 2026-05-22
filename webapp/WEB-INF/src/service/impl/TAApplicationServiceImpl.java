package service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;

import jakarta.servlet.http.Part;
import model.ApplicationForm;
import model.Course;
import model.ResumeSubmission;
import model.TA;
import service.ResumeStorageService;
import service.TAApplicationService;
import service.UserProfileService;

public class TAApplicationServiceImpl implements TAApplicationService {
    private static final int MAX_DISTINCT_APPLICATIONS = 3;
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
    public boolean hasMasterResume(TA ta) {
        File resumeFile = resumeStorageService.getMasterResumeFile(ta);
        return resumeFile != null && resumeFile.exists() && resumeFile.isFile();
    }

    @Override
    public File getMasterResumeFile(TA ta) {
        return resumeStorageService.getMasterResumeFile(ta);
    }

    @Override
    public String getStoredResumeFileName(TA ta) {
        return resumeStorageService.buildStoredResumeFileName(ta);
    }

    @Override
    public CurrentApplicationData prepareCurrentApplicationData(TA ta, Course course) {
        boolean hasCurrentApplication = false;
        if (ta != null && course != null) {
            String applicationFormId = ta.getApplicationFormIdForCourse(course.getId());
            hasCurrentApplication = applicationFormId != null && !applicationFormId.isBlank();
        }
        return new CurrentApplicationData(
                hasMasterResume(ta),
                getStoredResumeFileName(ta),
                hasCurrentApplication,
                hasCurrentApplication ? "Submitted application form" : null);
    }

    @Override
    public TA refreshTa(TA ta) {
        if (ta == null || ta.getEmail() == null || ta.getEmail().isBlank()) {
            return ta;
        }

        for (TA savedTa : userProfileService.getTAList()) {
            if (savedTa != null && ta.getEmail().equals(savedTa.getEmail())) {
                return savedTa;
            }
        }
        return ta;
    }

    @Override
    public int getApplicationLimit() {
        return MAX_DISTINCT_APPLICATIONS;
    }

    @Override
    public int getApplicationCount(TA ta) {
        return ta == null || ta.getAppliedClasses() == null ? 0 : ta.getAppliedClasses().size();
    }

    @Override
    public SubmitApplicationResult validateApplicationLimit(TA ta, Course course) {
        if (ta == null || course == null) {
            return SubmitApplicationResult.error("Unable to start this application.");
        }
        if (isNewCourseApplication(ta, course) && getApplicationCount(ta) >= MAX_DISTINCT_APPLICATIONS) {
            return SubmitApplicationResult.error("You can apply for up to 3 different TA positions.");
        }
        return SubmitApplicationResult.success();
    }

    @Override
    public SubmitResumeResult submitResume(TA ta, Course course, Part resumePart) throws IOException {
        SubmitApplicationResult applicationLimitResult = validateApplicationLimit(ta, course);
        if (!applicationLimitResult.isSuccess()) {
            return SubmitResumeResult.error(applicationLimitResult.getErrorMessage());
        }

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
    public SubmitApplicationResult submitApplicationForm(TA ta, Course course, ApplicationForm form) {
        if (ta == null || course == null) {
            return SubmitApplicationResult.error("Unable to submit this application.");
        }
        SubmitApplicationResult applicationLimitResult = validateApplicationLimit(ta, course);
        if (!applicationLimitResult.isSuccess()) {
            return applicationLimitResult;
        }
        String applicationFormId = form == null || form.getCourseId() == null || form.getCourseId().isBlank()
                ? course.getId()
                : form.getCourseId();
        ta.addOrUpdateApplication(course, applicationFormId, ResumeSubmission.STATUS_PENDING);
        course.addApplication(ta, applicationFormId);
        userProfileService.updateAppliedCourseIds(ta);
        return SubmitApplicationResult.success();
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
        List<Course> appliedCourses = ta.getAppliedClasses();
        Course selectedCourse = resolveSelectedAppliedCourse(appliedCourses, selectedCourseId);
        Integer selectedStatus = selectedCourse == null ? null : resolveResumeStatus(ta, selectedCourse.getId());
        Set<String> unreadReviewCourseIds = resolveUnreadReviewCourseIds(ta, appliedCourses);
        if (ta.markAllReviewUpdatesRead()) {
            userProfileService.updateAppliedCourseIds(ta);
        }
        return new PersonalCentreData(
                appliedCourses,
                selectedCourse,
                selectedStatus,
                unreadReviewCourseIds,
                applicationOpen,
                getApplicationCount(ta),
                MAX_DISTINCT_APPLICATIONS);
    }

    private Set<String> resolveUnreadReviewCourseIds(TA ta, List<Course> appliedCourses) {
        Set<String> unreadCourseIds = new LinkedHashSet<>();
        if (ta == null || appliedCourses == null) {
            return unreadCourseIds;
        }

        for (Course course : appliedCourses) {
            if (course != null && course.getId() != null && ta.isReviewUnreadForCourse(course.getId())) {
                unreadCourseIds.add(course.getId());
            }
        }
        return unreadCourseIds;
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

    private boolean isNewCourseApplication(TA ta, Course course) {
        return ta.getApplicationFormIdForCourse(course.getId()) == null;
    }

    private int resolveResumeStatus(TA ta, String courseId) {
        Integer status = ta.getResumeStatusForCourse(courseId);
        return status == null ? ResumeSubmission.STATUS_PENDING : status;
    }

    private String trimValue(String value) {
        return value == null ? "" : value.trim();
    }
}

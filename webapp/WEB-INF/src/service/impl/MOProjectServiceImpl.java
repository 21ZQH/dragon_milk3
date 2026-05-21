package service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import model.Course;
import model.Mo;
import model.ResumeSubmission;
import model.TA;
import service.CourseService;
import service.MOProjectService;
import service.UserProfileService;

public class MOProjectServiceImpl implements MOProjectService {
    private final CourseService courseService;
    private final UserProfileService userProfileService;

    public MOProjectServiceImpl(CourseService courseService, UserProfileService userProfileService) {
        this.courseService = courseService;
        this.userProfileService = userProfileService;
    }

    @Override
    public Course publishCourse(Mo mo, String courseName, String jobTitle, String workingHours,
            String jobDescription, String jobRequirement) {
        Course newCourse = new Course(UUID.randomUUID().toString(), courseName, jobTitle, workingHours,
                "TBD", jobDescription, jobRequirement);
        newCourse.setRecruitmentPublished(true);
        courseService.saveCourse(newCourse);
        if (mo != null) {
            mo.addOwnedCourse(newCourse);
            userProfileService.updateOwnedCourseIds(mo);
        }
        return newCourse;
    }

    @Override
    public Course saveCourseDraft(Mo mo, Course oldCourse, String courseName, String jobTitle,
            String jobDescription, String jobRequirement) {
        return applyCourseUpdate(mo, oldCourse, courseName, jobTitle, "", jobDescription, jobRequirement, false);
    }

    @Override
    public Course updateCourse(Mo mo, Course oldCourse, String courseName, String jobTitle, String workingHours,
            String jobDescription, String jobRequirement) {
        return applyCourseUpdate(mo, oldCourse, courseName, jobTitle, workingHours, jobDescription, jobRequirement, true);
    }

    private Course applyCourseUpdate(Mo mo, Course oldCourse, String courseName, String jobTitle, String workingHours,
            String jobDescription, String jobRequirement, boolean recruitmentPublished) {
        String salary = oldCourse.getSalary() == null ? "TBD" : oldCourse.getSalary();
        String resolvedCourseName = courseName == null || courseName.isBlank() ? oldCourse.getCourseName() : courseName;
        Course updatedCourse = new Course(oldCourse.getId(), resolvedCourseName, jobTitle, workingHours,
                salary, jobDescription, jobRequirement);
        updatedCourse.setPickedApplicantEmails(oldCourse.getPickedApplicantEmails());
        updatedCourse.setReviewPublished(oldCourse.isReviewPublished());
        updatedCourse.setRecruitmentPublished(recruitmentPublished);
        for (int i = 0; i < oldCourse.getTaApplicants().size(); i++) {
            String applicationFormId = i < oldCourse.getApplicantFormIds().size()
                    ? oldCourse.getApplicantFormIds().get(i)
                    : null;
            updatedCourse.addApplication(oldCourse.getTaApplicants().get(i), applicationFormId);
        }
        courseService.updateCourse(updatedCourse);
        if (mo != null) {
            mo.replaceOwnedCourse(updatedCourse);
        }
        return updatedCourse;
    }

    @Override
    public void updateProfile(Mo mo, String name, String degree, String college) {
        mo.setName(trimValue(name));
        mo.setDegree(trimValue(degree));
        mo.setCollege(trimValue(college));
        userProfileService.updateMoProfile(mo);
    }

    @Override
    public List<Course> refreshOwnedCourses(Mo mo) {
        if (mo == null) {
            return new ArrayList<>();
        }
        List<Course> allCourses = courseService.getCourseList();
        List<Course> freshOwnedCourses = new ArrayList<>();
        for (Course ownedCourse : mo.getOwnedCourses()) {
            if (ownedCourse == null || ownedCourse.getId() == null) {
                continue;
            }
            for (Course course : allCourses) {
                if (ownedCourse.getId().equals(course.getId())) {
                    freshOwnedCourses.add(course);
                    break;
                }
            }
        }
        mo.setOwnedCourses(freshOwnedCourses);
        return freshOwnedCourses;
    }

    @Override
    public void syncOwnedCourse(Mo mo, Course course) {
        if (mo == null || course == null) {
            return;
        }
        mo.replaceOwnedCourse(course);
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
    public int resolveCourseIndexForReview(String courseIndexParam, List<Course> courseList) {
        if (courseIndexParam == null) {
            return 0;
        }
        try {
            int courseIndex = Integer.parseInt(courseIndexParam);
            if (courseIndex < 0 || courseIndex >= courseList.size()) {
                return 0;
            }
            return courseIndex;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public List<String> resolvePickedApplicantEmails(Course course, String[] pickedEmails) {
        Set<String> validApplicantEmails = new LinkedHashSet<>();
        for (TA applicant : course.getTaApplicants()) {
            if (applicant != null && applicant.getEmail() != null && !applicant.getEmail().isBlank()) {
                validApplicantEmails.add(applicant.getEmail());
            }
        }
        List<String> resolvedPickedEmails = new ArrayList<>();
        if (pickedEmails == null) {
            return resolvedPickedEmails;
        }
        for (String pickedEmail : pickedEmails) {
            if (pickedEmail != null && validApplicantEmails.contains(pickedEmail) && !resolvedPickedEmails.contains(pickedEmail)) {
                resolvedPickedEmails.add(pickedEmail);
            }
        }
        return resolvedPickedEmails;
    }

    @Override
    public boolean saveReviewPicks(Course course, String[] pickedEmails) {
        if (course == null || course.isReviewPublished()) {
            return false;
        }
        course.setPickedApplicantEmails(resolvePickedApplicantEmails(course, pickedEmails));
        courseService.updateCourse(course);
        return true;
    }

    @Override
    public void publishReview(Course course, String[] pickedEmails) {
        if (course == null || course.isReviewPublished()) {
            return;
        }
        course.setPickedApplicantEmails(resolvePickedApplicantEmails(course, pickedEmails));
        applyPublishedStatuses(course);
        course.setReviewPublished(true);
        courseService.updateCourse(course);
    }

    private void applyPublishedStatuses(Course course) {
        for (int i = 0; i < course.getTaApplicants().size(); i++) {
            TA applicant = course.getTaApplicants().get(i);
            if (applicant == null || applicant.getEmail() == null || applicant.getEmail().isBlank()) {
                continue;
            }
            String applicationFormId = applicant.getApplicationFormIdForCourse(course.getId());
            if ((applicationFormId == null || applicationFormId.isBlank()) && i < course.getApplicantFormIds().size()) {
                applicationFormId = course.getApplicantFormIds().get(i);
            }
            if (applicationFormId == null || applicationFormId.isBlank()) {
                continue;
            }
            int status = course.isApplicantPicked(applicant.getEmail())
                    ? ResumeSubmission.STATUS_APPROVED
                    : ResumeSubmission.STATUS_REJECTED;
            applicant.addOrUpdateApplication(course, applicationFormId, status, true);
            course.addApplication(applicant, applicationFormId);
            userProfileService.updateAppliedCourseIds(applicant);
        }
    }

    private String trimValue(String value) {
        return value == null ? "" : value.trim();
    }
}

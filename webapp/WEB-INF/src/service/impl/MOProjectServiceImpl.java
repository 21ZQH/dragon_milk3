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

/**
 * Implementation of the {@link MOProjectService} interface.
 * Provides concrete logic for MO (Module Organiser) project management,
 * including publishing and updating courses, managing course drafts,
 * refreshing owned courses, handling review picks, and publishing
 * review results. Delegates to {@link CourseService} and
 * {@link UserProfileService}.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class MOProjectServiceImpl implements MOProjectService {
    /** Service for course management operations. */
    private final CourseService courseService;

    /** Service for user profile operations. */
    private final UserProfileService userProfileService;

    /**
     * Constructs a new {@code MOProjectServiceImpl} with the given services.
     *
     * @param courseService      the service for course operations
     * @param userProfileService the service for user profile operations
     */
    public MOProjectServiceImpl(CourseService courseService, UserProfileService userProfileService) {
        this.courseService = courseService;
        this.userProfileService = userProfileService;
    }

    /**
     * Publishes a new course for recruitment. Creates a course entity with
     * the provided details and marks recruitment as published.
     *
     * @param mo               the MO publishing the course
     * @param courseName       the name of the course
     * @param jobTitle         the job title for the TA position
     * @param workingHours     the working hours description
     * @param jobDescription   the job description
     * @param jobRequirement   the job requirements
     * @return the newly created and published {@link Course}
     */
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

    /**
     * Saves a course draft without publishing it for recruitment.
     *
     * @param mo               the MO saving the draft
     * @param oldCourse        the existing course to update
     * @param courseName       the name of the course
     * @param jobTitle         the job title for the TA position
     * @param jobDescription   the job description
     * @param jobRequirement   the job requirements
     * @return the updated {@link Course} draft
     */
    @Override
    public Course saveCourseDraft(Mo mo, Course oldCourse, String courseName, String jobTitle,
            String jobDescription, String jobRequirement) {
        return applyCourseUpdate(mo, oldCourse, courseName, jobTitle, "", jobDescription, jobRequirement, false);
    }

    /**
     * Updates a published course and re-publishes it with the new details.
     *
     * @param mo               the MO updating the course
     * @param oldCourse        the existing course to update
     * @param courseName       the name of the course
     * @param jobTitle         the job title for the TA position
     * @param workingHours     the working hours description
     * @param jobDescription   the job description
     * @param jobRequirement   the job requirements
     * @return the updated and re-published {@link Course}
     */
    @Override
    public Course updateCourse(Mo mo, Course oldCourse, String courseName, String jobTitle, String workingHours,
            String jobDescription, String jobRequirement) {
        return applyCourseUpdate(mo, oldCourse, courseName, jobTitle, workingHours, jobDescription, jobRequirement, true);
    }

    /**
     * Applies updates to a course, preserving existing applicant and review data
     * while setting the new recruitment published state.
     *
     * @param mo                  the MO performing the update
     * @param oldCourse           the existing course to update
     * @param courseName          the name of the course
     * @param jobTitle            the job title for the TA position
     * @param workingHours        the working hours description
     * @param jobDescription      the job description
     * @param jobRequirement      the job requirements
     * @param recruitmentPublished whether the course should be marked as published
     * @return the updated {@link Course}
     */
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

    /**
     * Updates the MO's profile with the given personal information.
     *
     * @param mo      the MO whose profile is being updated
     * @param name    the display name
     * @param degree  the degree information
     * @param college the college affiliation
     */
    @Override
    public void updateProfile(Mo mo, String name, String degree, String college) {
        mo.setName(trimValue(name));
        mo.setDegree(trimValue(degree));
        mo.setCollege(trimValue(college));
        userProfileService.updateMoProfile(mo);
    }

    /**
     * Refreshes the MO's owned courses list against the latest course data
     * from the data store.
     *
     * @param mo the MO whose owned courses are to be refreshed
     * @return a fresh list of {@link Course} objects owned by the MO
     */
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

    /**
     * Synchronizes a single course in the MO's owned courses list by
     * replacing the matching entry.
     *
     * @param mo     the MO whose owned courses are to be updated
     * @param course the updated course to replace with
     */
    @Override
    public void syncOwnedCourse(Mo mo, Course course) {
        if (mo == null || course == null) {
            return;
        }
        mo.replaceOwnedCourse(course);
    }

    /**
     * Retrieves a course from the given list by its index parameter string.
     *
     * @param courseList        the list of courses to search
     * @param courseIndexParam  the string representation of the course index
     * @return the {@link Course} at the specified index, or {@code null}
     *         if the index is invalid or the parameter is malformed
     */
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

    /**
     * Resolves a course index for review navigation from a string parameter,
     * defaulting to 0 if the parameter is invalid.
     *
     * @param courseIndexParam the string representation of the course index
     * @param courseList       the list of courses
     * @return the resolved course index, or 0 if the parameter is invalid
     */
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

    /**
     * Resolves a list of valid picked applicant emails, filtering against
     * the actual applicants for the course and removing duplicates.
     *
     * @param course        the course being reviewed
     * @param pickedEmails  an array of candidate email addresses
     * @return a list of valid, unique picked applicant email addresses
     */
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

    /**
     * Saves the MO's review picks for a course without publishing them.
     *
     * @param course        the course whose picks are being saved
     * @param pickedEmails  an array of applicant email addresses that were picked
     * @return {@code true} if the picks were saved successfully,
     *         {@code false} if the course is null or review is already published
     */
    @Override
    public boolean saveReviewPicks(Course course, String[] pickedEmails) {
        if (course == null || course.isReviewPublished()) {
            return false;
        }
        course.setPickedApplicantEmails(resolvePickedApplicantEmails(course, pickedEmails));
        courseService.updateCourse(course);
        return true;
    }

    /**
     * Publishes the review results for a course, setting the picked applicants
     * and updating the status of all applications accordingly.
     *
     * @param course        the course whose review is being published
     * @param pickedEmails  an array of applicant email addresses that were picked
     */
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

    /**
     * Applies the approved or rejected status to all applicants for the
     * given course based on the MO's picks.
     *
     * @param course the course whose statuses are being applied
     */
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

    /**
     * Trims a string value, returning an empty string for null input.
     *
     * @param value the string to trim
     * @return the trimmed string, or an empty string if the input is {@code null}
     */
    private String trimValue(String value) {
        return value == null ? "" : value.trim();
    }
}

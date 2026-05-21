package service;

import java.util.List;

import model.Course;
import model.Mo;

public interface MOProjectService {
    Course publishCourse(Mo mo, String courseName, String jobTitle, String workingHours,
            String jobDescription, String jobRequirement);

    Course saveCourseDraft(Mo mo, Course oldCourse, String courseName, String jobTitle,
            String jobDescription, String jobRequirement);

    Course updateCourse(Mo mo, Course oldCourse, String courseName, String jobTitle, String workingHours,
            String jobDescription, String jobRequirement);

    void updateProfile(Mo mo, String name, String degree, String college);

    List<Course> refreshOwnedCourses(Mo mo);

    void syncOwnedCourse(Mo mo, Course course);

    Course getCourseByIndex(List<Course> courseList, String courseIndexParam);

    int resolveCourseIndexForReview(String courseIndexParam, List<Course> courseList);

    List<String> resolvePickedApplicantEmails(Course course, String[] pickedEmails);

    boolean saveReviewPicks(Course course, String[] pickedEmails);

    void publishReview(Course course, String[] pickedEmails);
}

package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Module organizer account.
 *
 * <p>An MO owns a set of course projects, can edit recruitment information
 * before the MO deadline, and can review submitted TA applications after the TA
 * application deadline.</p>
 *
 * @author BUPT Group33
 * @version 1.0
 * @since 1.0
 */
public class Mo extends User {
    /** The fixed role identifier for module organizer accounts. */
    private String role="Mo";
    /** The academic degree of the module organizer. */
    private String degree;
    /** The college or department the module organizer belongs to. */
    private String college;
    /** The list of courses owned and managed by this module organizer. */
    private List<Course> ownedCourses = new ArrayList<>();

    /**
     * Constructs an MO account with the specified credentials.
     *
     * @param password the login password for the MO account
     * @param email    the email address to associate with this MO account
     */
    public Mo(String password,String email) {
        super(password, email);
    }

    /**
     * Returns the role identifier for this module organizer.
     *
     * @return the string "Mo" indicating this is a module organizer account
     */
    @Override
    public String getRole() {
        return role;



    }

    /**
     * Returns the list of courses owned by this module organizer.
     *
     * @return the list of owned courses
     */
    public List<Course> getOwnedCourses() {
        return ownedCourses;
    }

    /**
     * Sets the list of courses owned by this module organizer.
     * <p>This method clears existing courses and repopulates using
     * {@link #addOwnedCourse(Course)} to prevent duplicate entries.</p>
     *
     * @param ownedCourses the list of courses to set as owned
     */
    public void setOwnedCourses(List<Course> ownedCourses) {
        this.ownedCourses = new ArrayList<>();
        if (ownedCourses != null) {
            for (Course course : ownedCourses) {
                addOwnedCourse(course);
            }
        }
    }

    /**
     * Adds a course to the MO's list of owned courses if not already present.
     *
     * @param course the course to add
     */
    public void addOwnedCourse(Course course) {
        if (course != null && !ownedCourses.contains(course)) {
            ownedCourses.add(course);
        }
    }

    /**
     * Replaces an existing owned course with an updated version.
     * <p>If the course is not already in the list, it will be added.</p>
     *
     * @param course the updated course to replace or add
     */
    public void replaceOwnedCourse(Course course) {
        if (course == null) {
            return;
        }

        for (int i = 0; i < ownedCourses.size(); i++) {
            if (course.equals(ownedCourses.get(i))) {
                ownedCourses.set(i, course);
                return;
            }
        }

        addOwnedCourse(course);
    }

    /**
     * Returns the academic degree of the module organizer.
     *
     * @return the degree, or {@code null} if not set
     */
    public String getDegree() {
        return degree;
    }

    /**
     * Sets the academic degree of the module organizer.
     *
     * @param degree the degree to set
     */
    public void setDegree(String degree) {
        this.degree = degree;
    }

    /**
     * Returns the college or department of the module organizer.
     *
     * @return the college name, or {@code null} if not set
     */
    public String getCollege() {
        return college;
    }

    /**
     * Sets the college or department of the module organizer.
     *
     * @param college the college name to set
     */
    public void setCollege(String college) {
        this.college = college;
    }

    /**
     * Checks whether the module organizer has completed their profile.
     * <p>A complete profile requires a non-empty name, degree, and college.</p>
     *
     * @return {@code true} if name, degree, and college are all non-blank,
     *         {@code false} otherwise
     */
    public boolean hasCompleteProfile() {
        return hasText(getName()) && hasText(degree) && hasText(college);
    }

    /**
     * Checks whether the given string value is non-null and non-blank.
     *
     * @param value the string value to check
     * @return {@code true} if the value is not {@code null} and not blank
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

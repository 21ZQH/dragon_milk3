package service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import model.Course;
import model.Mo;
import model.TA;
import repository.ApplicationFormRepository;
import repository.DeadlineRepository;
import repository.UserRepository;
import repository.impl.TxtApplicationFormRepositoryImpl;
import repository.impl.TxtDeadlineRepositoryImpl;
import repository.impl.TxtUserRepositoryImpl;
import service.AdminManagementService;
import service.CourseService;
import service.ai.MOCourseDraftAiClient;
import service.ai.MOCourseDraftAiClientFactory;
import service.ai.impl.MockMOCourseDraftAiClient;

/**
 * Implementation of the {@link AdminManagementService} interface.
 * Provides concrete logic for admin-level operations such as retrieving
 * user lists, creating MO accounts with AI-assisted course drafting, and
 * resetting the entire recruitment cycle. Delegates to
 * {@link UserRepository}, {@link CourseService},
 * {@link ApplicationFormRepository}, and {@link DeadlineRepository}.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class AdminManagementServiceImpl implements AdminManagementService {
    /** Repository for user persistence operations. */
    private final UserRepository userRepository;

    /** Service for course management operations. */
    private final CourseService courseService;

    /** Repository for application form persistence. */
    private final ApplicationFormRepository applicationFormRepository;

    /** Repository for deadline persistence. */
    private final DeadlineRepository deadlineRepository;

    /** Primary AI client for generating course drafts. */
    private final MOCourseDraftAiClient draftAiClient;

    /** Fallback AI client for generating course drafts. */
    private final MOCourseDraftAiClient fallbackDraftAiClient;

    /**
     * Constructs a new {@code AdminManagementServiceImpl} with default
     * dependencies: {@link TxtUserRepositoryImpl}, {@link CourseServiceImpl},
     * {@link TxtApplicationFormRepositoryImpl}, {@link TxtDeadlineRepositoryImpl},
     * and AI draft clients from {@link MOCourseDraftAiClientFactory} and
     * {@link MockMOCourseDraftAiClient}.
     */
    public AdminManagementServiceImpl() {
        this(new TxtUserRepositoryImpl(), new CourseServiceImpl(),
                new TxtApplicationFormRepositoryImpl(), new TxtDeadlineRepositoryImpl(),
                new MOCourseDraftAiClientFactory().create(), new MockMOCourseDraftAiClient());
    }

    /**
     * Constructs a new {@code AdminManagementServiceImpl} with the given
     * dependencies.
     *
     * @param userRepository             the repository for user data access
     * @param courseService              the service for course operations
     * @param applicationFormRepository  the repository for application form data access
     * @param deadlineRepository         the repository for deadline data access
     * @param draftAiClient              the primary AI client for course drafts
     * @param fallbackDraftAiClient      the fallback AI client for course drafts
     */
    AdminManagementServiceImpl(UserRepository userRepository, CourseService courseService,
            ApplicationFormRepository applicationFormRepository, DeadlineRepository deadlineRepository,
            MOCourseDraftAiClient draftAiClient, MOCourseDraftAiClient fallbackDraftAiClient) {
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.applicationFormRepository = applicationFormRepository;
        this.deadlineRepository = deadlineRepository;
        this.draftAiClient = draftAiClient;
        this.fallbackDraftAiClient = fallbackDraftAiClient;
    }

    /**
     * Retrieves a list of all TA users.
     *
     * @return a list of {@link TA} objects
     */
    @Override
    public List<TA> getTAList() {
        return userRepository.getTAList();
    }

    /**
     * Retrieves a list of all MO users.
     *
     * @return a list of {@link Mo} objects
     */
    @Override
    public List<Mo> getMOList() {
        return userRepository.getMOList();
    }

    /**
     * Creates a new MO account with the given credentials and assigned courses.
     * If a course name does not match any existing course, a new course is
     * created using AI-generated draft content.
     *
     * @param account         the email address for the new MO account
     * @param password        the password for the new MO account
     * @param courseNamesText a text block containing course names, one per line
     * @return a {@link CreateMoResult} indicating success or the reason for failure
     */
    @Override
    public CreateMoResult createMoAccount(String account, String password, String courseNamesText) {
        String cleanAccount = trimValue(account);
        String cleanPassword = trimValue(password);

        if (cleanAccount.isBlank() || cleanPassword.isBlank() || courseNamesText == null || courseNamesText.isBlank()) {
            return CreateMoResult.failure("Please complete account, password, and assigned courses.");
        }

        if (userRepository.isEmailRegistered(cleanAccount)) {
            return CreateMoResult.failure("This account already exists.");
        }

        List<Course> allCourses = courseService.getCourseList();
        List<Course> assignedCourses = new ArrayList<>();
        for (String courseNameLine : courseNamesText.split("\\R")) {
            String courseName = trimValue(courseNameLine);
            if (courseName.isBlank()) {
                continue;
            }

            Course course = findCourseByName(allCourses, courseName);
            if (course == null) {
                MOCourseDraftAiClient.MOCourseDraft draft = generateDraft(courseName);
                course = new Course(
                        "admin-" + UUID.randomUUID(),
                        courseName,
                        defaultValue(draft.getJobTitle(), "Teaching Assistant"),
                        "",
                        "TBD",
                        draft.getJobDescription(),
                        draft.getJobRequirement());
                course.setRecruitmentPublished(false);
                courseService.saveCourse(course);
                allCourses.add(course);
            } else if (!course.isRecruitmentPublished() && isDraftEmpty(course)) {
                MOCourseDraftAiClient.MOCourseDraft draft = generateDraft(courseName);
                course.setJobTitle(defaultValue(draft.getJobTitle(), "Teaching Assistant"));
                course.setWorkingHours("");
                course.setJobDescription(draft.getJobDescription());
                course.setJobRequirement(draft.getJobRequirement());
                courseService.updateCourse(course);
            }
            assignedCourses.add(course);
        }

        if (assignedCourses.isEmpty()) {
            return CreateMoResult.failure("Please assign at least one valid course.");
        }

        Mo mo = new Mo(cleanPassword, cleanAccount);
        mo.setName(cleanAccount);
        mo.setDegree("");
        mo.setCollege("");
        mo.setOwnedCourses(assignedCourses);
        userRepository.saveUser(mo);

        return CreateMoResult.success(mo, assignedCourses);
    }

    /**
     * Resets the entire recruitment cycle. Clears all TA application states,
     * application forms, course recruitment data, and deadlines.
     *
     * @return a {@link ResetRecruitmentCycleResult} indicating success or the
     *         reason for failure
     */
    @Override
    public ResetRecruitmentCycleResult resetRecruitmentCycle() {
        try {
            userRepository.resetTaApplicationState();
            applicationFormRepository.clearAll();
            courseService.resetRecruitmentCycle();
            deadlineRepository.clearDeadlines();
            return ResetRecruitmentCycleResult.success();
        } catch (RuntimeException e) {
            return ResetRecruitmentCycleResult.failure("Reset failed: " + e.getMessage());
        }
    }

    /**
     * Finds a course in the given list by its name (case-insensitive).
     *
     * @param courses    the list of courses to search
     * @param courseName the name of the course to find
     * @return the matching {@link Course}, or {@code null} if not found
     */
    private Course findCourseByName(List<Course> courses, String courseName) {
        if (courses == null || courseName == null) {
            return null;
        }
        for (Course course : courses) {
            if (course != null && courseName.equalsIgnoreCase(trimValue(course.getCourseName()))) {
                return course;
            }
        }
        return null;
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

    /**
     * Generates a course draft using the primary AI client, falling back
     * to the secondary client or a minimal default draft if both fail.
     *
     * @param courseName the name of the course to generate a draft for
     * @return a {@link MOCourseDraftAiClient.MOCourseDraft} with generated content
     */
    private MOCourseDraftAiClient.MOCourseDraft generateDraft(String courseName) {
        try {
            return draftAiClient.generate(courseName);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            try {
                return fallbackDraftAiClient.generate(courseName);
            } catch (IOException | InterruptedException ignored) {
                if (ignored instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                return new MOCourseDraftAiClient.MOCourseDraft(
                        "Teaching Assistant",
                        "Support tutorials, lab sessions, grading, and student questions for " + courseName + ".",
                        "Good understanding of " + courseName + ", clear communication skills, and responsibility.");
            }
        }
    }

    /**
     * Checks whether a course has empty draft fields (job title, description,
     * and requirement).
     *
     * @param course the course to check
     * @return {@code true} if all draft fields are blank, {@code false} otherwise
     */
    private boolean isDraftEmpty(Course course) {
        return isBlank(course.getJobTitle())
                && isBlank(course.getJobDescription())
                && isBlank(course.getJobRequirement());
    }

    /**
     * Checks whether a string value is {@code null} or blank.
     *
     * @param value the string to check
     * @return {@code true} if the value is {@code null} or blank
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Returns the given value if it is not blank, otherwise returns the
     * fallback string.
     *
     * @param value    the primary value
     * @param fallback the fallback value if the primary is blank or {@code null}
     * @return the trimmed primary value, or the fallback
     */
    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}

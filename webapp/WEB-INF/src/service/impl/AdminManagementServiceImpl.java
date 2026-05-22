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

public class AdminManagementServiceImpl implements AdminManagementService {
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final ApplicationFormRepository applicationFormRepository;
    private final DeadlineRepository deadlineRepository;
    private final MOCourseDraftAiClient draftAiClient;
    private final MOCourseDraftAiClient fallbackDraftAiClient;

    public AdminManagementServiceImpl() {
        this(new TxtUserRepositoryImpl(), new CourseServiceImpl(),
                new TxtApplicationFormRepositoryImpl(), new TxtDeadlineRepositoryImpl(),
                new MOCourseDraftAiClientFactory().create(), new MockMOCourseDraftAiClient());
    }

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

    @Override
    public List<TA> getTAList() {
        return userRepository.getTAList();
    }

    @Override
    public List<Mo> getMOList() {
        return userRepository.getMOList();
    }

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

    private String trimValue(String value) {
        return value == null ? "" : value.trim();
    }

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

    private boolean isDraftEmpty(Course course) {
        return isBlank(course.getJobTitle())
                && isBlank(course.getJobDescription())
                && isBlank(course.getJobRequirement());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}

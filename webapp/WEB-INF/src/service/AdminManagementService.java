package service;

import java.util.List;

import model.Course;
import model.Mo;
import model.TA;

/**
 * Service interface for administrative management operations within
 * the TA recruitment system. Provides methods for managing TA and MO
 * accounts, creating new MO accounts with course assignments, and
 * resetting the recruitment cycle.
 *
 * @version 1.0
 * @since 2025
 */
public interface AdminManagementService {
    /**
     * Retrieves a list of all Teaching Assistants in the system.
     *
     * @return a list of {@link TA} objects
     */
    List<TA> getTAList();

    /**
     * Retrieves a list of all Module Officers in the system.
     *
     * @return a list of {@link Mo} objects
     */
    List<Mo> getMOList();

    /**
     * Creates a new Module Officer account with the specified credentials
     * and assigns courses based on the provided course names.
     *
     * @param account          the email or username for the new MO account
     * @param password         the password for the new MO account
     * @param courseNamesText  a text block containing the course names to assign
     * @return a {@link CreateMoResult} indicating success or failure
     */
    CreateMoResult createMoAccount(String account, String password, String courseNamesText);

    /**
     * Resets the entire recruitment cycle, clearing all applications,
     * review results, and resetting course states to draft.
     *
     * @return a {@link ResetRecruitmentCycleResult} indicating the outcome
     */
    ResetRecruitmentCycleResult resetRecruitmentCycle();

    /**
     * Represents the result of a recruitment cycle reset operation,
     * indicating success or providing a failure message.
     */
    final class ResetRecruitmentCycleResult {
        private final boolean success;
        private final String message;

        /**
         * Constructs a ResetRecruitmentCycleResult with the given parameters.
         *
         * @param success whether the reset was successful
         * @param message a descriptive message about the result
         */
        private ResetRecruitmentCycleResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        /**
         * Creates a successful reset result with a default success message.
         *
         * @return a success result
         */
        public static ResetRecruitmentCycleResult success() {
            return new ResetRecruitmentCycleResult(true,
                    "Recruitment cycle has been reset. Courses are now drafts, applications and review results are cleared.");
        }

        /**
         * Creates a failure result with the given error message.
         *
         * @param message the description of the failure
         * @return a failure result
         */
        public static ResetRecruitmentCycleResult failure(String message) {
            return new ResetRecruitmentCycleResult(false, message);
        }

        /**
         * Returns whether the reset was successful.
         *
         * @return {@code true} if successful, {@code false} otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the descriptive message about the result.
         *
         * @return the result message
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Represents the result of creating a new MO account,
     * including the created MO and assigned courses on success.
     */
    final class CreateMoResult {
        private final boolean success;
        private final String message;
        private final Mo mo;
        private final List<Course> assignedCourses;

        /**
         * Constructs a CreateMoResult with the given parameters.
         *
         * @param success         whether the creation was successful
         * @param message         a descriptive message about the result
         * @param mo              the created MO, or {@code null} on failure
         * @param assignedCourses the list of courses assigned to the MO
         */
        private CreateMoResult(boolean success, String message, Mo mo, List<Course> assignedCourses) {
            this.success = success;
            this.message = message;
            this.mo = mo;
            this.assignedCourses = assignedCourses;
        }

        /**
         * Creates a successful MO creation result.
         *
         * @param mo              the created MO account
         * @param assignedCourses the courses assigned to the MO
         * @return a success result with the created MO and assigned courses
         */
        public static CreateMoResult success(Mo mo, List<Course> assignedCourses) {
            return new CreateMoResult(true, "MO account created successfully.", mo, assignedCourses);
        }

        /**
         * Creates a failure result with an error message.
         *
         * @param message the description of the failure
         * @return a failure result
         */
        public static CreateMoResult failure(String message) {
            return new CreateMoResult(false, message, null, List.of());
        }

        /**
         * Returns whether the MO account creation was successful.
         *
         * @return {@code true} if successful, {@code false} otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the descriptive message about the result.
         *
         * @return the result message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns the created MO account.
         *
         * @return the created {@link Mo}, or {@code null} on failure
         */
        public Mo getMo() {
            return mo;
        }

        /**
         * Returns the list of courses assigned to the created MO.
         *
         * @return a list of assigned {@link Course} objects
         */
        public List<Course> getAssignedCourses() {
            return assignedCourses;
        }
    }
}

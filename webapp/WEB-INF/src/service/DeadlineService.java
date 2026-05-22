package service;

import java.time.LocalDateTime;

/**
 * Service interface for managing deadlines within the TA recruitment system.
 * Provides methods for retrieving and saving application and MO modification
 * deadlines, parsing deadline input strings, checking whether various stages
 * of the recruitment process are open, and clearing all deadline data.
 *
 * @version 1.0
 * @since 2025
 */
public interface DeadlineService {
    /**
     * Retrieves the current deadline for TA applications.
     *
     * @return the application deadline as a {@link LocalDateTime},
     *         or {@code null} if not set
     */
    LocalDateTime getApplicationDeadline();

    /**
     * Saves or updates the deadline for TA applications.
     *
     * @param deadline the application deadline to persist
     */
    void saveApplicationDeadline(LocalDateTime deadline);

    /**
     * Retrieves the current deadline for MO modifications to courses.
     *
     * @return the MO modification deadline as a {@link LocalDateTime},
     *         or {@code null} if not set
     */
    LocalDateTime getMoModifyDeadline();

    /**
     * Saves or updates the deadline for MO modifications to courses.
     *
     * @param deadline the MO modification deadline to persist
     */
    void saveMoModifyDeadline(LocalDateTime deadline);

    /**
     * Parses a deadline from separate date and time strings and returns
     * the result, which may include an error message if parsing fails.
     *
     * @param deadlineDate the date portion of the deadline (e.g., "2025-06-01")
     * @param deadlineTime the time portion of the deadline (e.g., "23:59")
     * @return a {@link SaveDeadlineResult} containing the parsed
     *         {@link LocalDateTime} or an error message
     */
    SaveDeadlineResult parseDeadline(String deadlineDate, String deadlineTime);

    /**
     * Checks whether the application period is currently open based on the
     * given deadline.
     *
     * @param deadline the application deadline to evaluate
     * @return {@code true} if the current time is before the deadline,
     *         {@code false} otherwise
     */
    boolean isApplicationOpen(LocalDateTime deadline);

    /**
     * Checks whether the review stage is currently open based on the
     * given deadline.
     *
     * @param deadline the review deadline to evaluate
     * @return {@code true} if the review stage is open, {@code false} otherwise
     */
    boolean isReviewStageOpen(LocalDateTime deadline);

    /**
     * Checks whether the MO modification period is currently open based on the
     * given deadline.
     *
     * @param deadline the MO modification deadline to evaluate
     * @return {@code true} if modifications are allowed, {@code false} otherwise
     */
    boolean isMoModifyOpen(LocalDateTime deadline);

    /**
     * Clears all stored deadline data, resetting them to an unset state.
     */
    void clearDeadlines();

    /**
     * Represents the result of parsing a deadline from user input,
     * containing the parsed {@link LocalDateTime} on success or an
     * error message on failure.
     */
    final class SaveDeadlineResult {
        private final boolean success;
        private final LocalDateTime deadline;
        private final String errorMessage;

        /**
         * Constructs a SaveDeadlineResult with the given parameters.
         *
         * @param success      whether the parsing was successful
         * @param deadline     the parsed deadline, or {@code null} on failure
         * @param errorMessage an error message, or {@code null} on success
         */
        private SaveDeadlineResult(boolean success, LocalDateTime deadline, String errorMessage) {
            this.success = success;
            this.deadline = deadline;
            this.errorMessage = errorMessage;
        }

        /**
         * Creates a successful parse result with the given deadline.
         *
         * @param deadline the successfully parsed deadline
         * @return a success result
         */
        public static SaveDeadlineResult success(LocalDateTime deadline) {
            return new SaveDeadlineResult(true, deadline, null);
        }

        /**
         * Creates a failed parse result with an error message.
         *
         * @param errorMessage the description of the parsing error
         * @return a failure result
         */
        public static SaveDeadlineResult error(String errorMessage) {
            return new SaveDeadlineResult(false, null, errorMessage);
        }

        /**
         * Returns whether the deadline was parsed successfully.
         *
         * @return {@code true} if successful, {@code false} otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the parsed deadline.
         *
         * @return the {@link LocalDateTime} deadline, or {@code null} on failure
         */
        public LocalDateTime getDeadline() {
            return deadline;
        }

        /**
         * Returns the error message if parsing failed.
         *
         * @return the error message, or {@code null} on success
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

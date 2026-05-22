package service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import repository.DeadlineRepository;
import repository.impl.TxtDeadlineRepositoryImpl;
import service.DeadlineService;

/**
 * Implementation of the {@link DeadlineService} interface.
 * Provides concrete logic for managing recruitment deadlines,
 * including application deadlines and MO modification deadlines.
 * Handles parsing of deadline date/time strings and provides
 * checks for whether each recruitment stage is currently open.
 * Delegates persistence to {@link DeadlineRepository}.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class DeadlineServiceImpl implements DeadlineService {
    /** Repository for deadline persistence operations. */
    private final DeadlineRepository deadlineRepository;

    /**
     * Constructs a new {@code DeadlineServiceImpl} with a default
     * {@link TxtDeadlineRepositoryImpl}.
     */
    public DeadlineServiceImpl() {
        this(new TxtDeadlineRepositoryImpl());
    }

    /**
     * Constructs a new {@code DeadlineServiceImpl} with the given repository.
     *
     * @param deadlineRepository the repository for deadline data access
     */
    DeadlineServiceImpl(DeadlineRepository deadlineRepository) {
        this.deadlineRepository = deadlineRepository;
    }

    /**
     * Retrieves the current application submission deadline.
     *
     * @return the application deadline as a {@link LocalDateTime},
     *         or {@code null} if not set
     */
    @Override
    public LocalDateTime getApplicationDeadline() {
        return deadlineRepository.getApplicationDeadline();
    }

    /**
     * Saves the application submission deadline.
     *
     * @param deadline the deadline to save
     */
    @Override
    public void saveApplicationDeadline(LocalDateTime deadline) {
        deadlineRepository.saveApplicationDeadline(deadline);
    }

    /**
     * Retrieves the deadline by which MOs can modify their courses.
     *
     * @return the MO modification deadline as a {@link LocalDateTime},
     *         or {@code null} if not set
     */
    @Override
    public LocalDateTime getMoModifyDeadline() {
        return deadlineRepository.getMoModifyDeadline();
    }

    /**
     * Saves the MO modification deadline.
     *
     * @param deadline the deadline to save
     */
    @Override
    public void saveMoModifyDeadline(LocalDateTime deadline) {
        deadlineRepository.saveMoModifyDeadline(deadline);
    }

    /**
     * Parses a deadline date and time string into a {@link LocalDateTime}.
     *
     * @param deadlineDate the date string in ISO format (e.g., "2024-12-31")
     * @param deadlineTime the time string in ISO format (e.g., "23:59")
     * @return a {@link SaveDeadlineResult} containing the parsed
     *         {@link LocalDateTime} on success, or an error message on failure
     */
    @Override
    public SaveDeadlineResult parseDeadline(String deadlineDate, String deadlineTime) {
        if (deadlineDate == null || deadlineDate.isBlank() || deadlineTime == null || deadlineTime.isBlank()) {
            return SaveDeadlineResult.error("Please complete both deadline date and deadline time.");
        }

        try {
            LocalDate date = LocalDate.parse(deadlineDate);
            LocalTime time = LocalTime.parse(deadlineTime);
            return SaveDeadlineResult.success(LocalDateTime.of(date, time));
        } catch (Exception e) {
            return SaveDeadlineResult.error("Invalid deadline format.");
        }
    }

    /**
     * Checks whether the application submission stage is currently open
     * based on the given deadline.
     *
     * @param deadline the application deadline
     * @return {@code true} if the current time is before or equal to the deadline,
     *         or if no deadline is set; {@code false} otherwise
     */
    @Override
    public boolean isApplicationOpen(LocalDateTime deadline) {
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Checks whether the review stage is currently open based on the given
     * deadline.
     *
     * @param deadline the application deadline
     * @return {@code true} if the current time is after the deadline,
     *         or if no deadline is set; {@code false} otherwise
     */
    @Override
    public boolean isReviewStageOpen(LocalDateTime deadline) {
        return deadline == null || LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Checks whether the MO modification stage is currently open based on
     * the given deadline.
     *
     * @param deadline the MO modification deadline
     * @return {@code true} if the current time is before or equal to the deadline,
     *         or if no deadline is set; {@code false} otherwise
     */
    @Override
    public boolean isMoModifyOpen(LocalDateTime deadline) {
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Clears all stored deadlines from the data store.
     */
    @Override
    public void clearDeadlines() {
        deadlineRepository.clearDeadlines();
    }
}

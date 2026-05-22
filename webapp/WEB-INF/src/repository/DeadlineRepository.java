package repository;

import java.time.LocalDateTime;

/**
 * Repository boundary for recruitment deadline persistence.
 *
 * <p>Deadline values are persisted to text files, while page controllers read
 * the runtime values from {@code ServletContext}. The application listener
 * loads persisted values into the context during startup.</p>
 */
public interface DeadlineRepository {
    /**
     * Loads the TA application deadline.
     *
     * @return deadline date and time, or {@code null} when not configured
     */
    LocalDateTime getApplicationDeadline();

    /**
     * Saves the TA application deadline.
     *
     * @param deadline deadline to persist
     */
    void saveApplicationDeadline(LocalDateTime deadline);

    /**
     * Loads the MO course modification deadline.
     *
     * @return deadline date and time, or {@code null} when not configured
     */
    LocalDateTime getMoModifyDeadline();

    /**
     * Saves the MO course modification deadline.
     *
     * @param deadline deadline to persist
     */
    void saveMoModifyDeadline(LocalDateTime deadline);

    void clearDeadlines();
}

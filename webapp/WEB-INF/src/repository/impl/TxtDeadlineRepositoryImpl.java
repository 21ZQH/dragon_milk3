package repository.impl;

import java.time.LocalDateTime;

import repository.DeadlineRepository;
import store.DeadlineStore;

/**
 * Text-file implementation of {@link DeadlineRepository}.
 *
 * <p>Values are delegated to {@link DeadlineStore}, which stores the TA
 * application deadline in {@code deadline.txt} and the MO course modification
 * deadline in {@code mo-deadline.txt}.</p>
 */
public class TxtDeadlineRepositoryImpl implements DeadlineRepository {
    /**
     * Reads the persisted TA application deadline.
     *
     * @return configured deadline, or {@code null} when no value is stored
     */
    @Override
    public LocalDateTime getApplicationDeadline() {
        return DeadlineStore.getDeadline();
    }

    /**
     * Persists the TA application deadline.
     *
     * @param deadline deadline selected by Admin
     */
    @Override
    public void saveApplicationDeadline(LocalDateTime deadline) {
        DeadlineStore.saveDeadline(deadline);
    }

    /**
     * Reads the persisted MO course modification deadline.
     *
     * @return configured deadline, or {@code null} when no value is stored
     */
    @Override
    public LocalDateTime getMoModifyDeadline() {
        return DeadlineStore.getMoModifyDeadline();
    }

    /**
     * Persists the MO course modification deadline.
     *
     * @param deadline deadline selected by Admin
     */
    @Override
    public void saveMoModifyDeadline(LocalDateTime deadline) {
        DeadlineStore.saveMoModifyDeadline(deadline);
    }

    /**
     * Delegates deadline clearing to {@link DeadlineStore}.
     */
    @Override
    public void clearDeadlines() {
        DeadlineStore.clearDeadlines();
    }
}

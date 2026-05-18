package repository.impl;

import java.time.LocalDateTime;

import repository.DeadlineRepository;
import store.DeadlineStore;

public class TxtDeadlineRepositoryImpl implements DeadlineRepository {
    @Override
    public LocalDateTime getApplicationDeadline() {
        return DeadlineStore.getDeadline();
    }

    @Override
    public void saveApplicationDeadline(LocalDateTime deadline) {
        DeadlineStore.saveDeadline(deadline);
    }

    @Override
    public LocalDateTime getMoModifyDeadline() {
        return DeadlineStore.getMoModifyDeadline();
    }

    @Override
    public void saveMoModifyDeadline(LocalDateTime deadline) {
        DeadlineStore.saveMoModifyDeadline(deadline);
    }
}

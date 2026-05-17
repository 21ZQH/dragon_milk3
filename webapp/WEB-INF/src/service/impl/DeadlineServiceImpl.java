package service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import service.DeadlineService;
import store.DeadlineStore;

public class DeadlineServiceImpl implements DeadlineService {
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

    @Override
    public boolean isApplicationOpen(LocalDateTime deadline) {
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }

    @Override
    public boolean isReviewStageOpen(LocalDateTime deadline) {
        return deadline == null || LocalDateTime.now().isAfter(deadline);
    }

    @Override
    public boolean isMoModifyOpen(LocalDateTime deadline) {
        return deadline == null || !LocalDateTime.now().isAfter(deadline);
    }
}

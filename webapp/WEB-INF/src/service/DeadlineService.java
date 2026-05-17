package service;

import java.time.LocalDateTime;

public interface DeadlineService {
    LocalDateTime getApplicationDeadline();

    void saveApplicationDeadline(LocalDateTime deadline);

    LocalDateTime getMoModifyDeadline();

    void saveMoModifyDeadline(LocalDateTime deadline);

    SaveDeadlineResult parseDeadline(String deadlineDate, String deadlineTime);

    boolean isApplicationOpen(LocalDateTime deadline);

    boolean isReviewStageOpen(LocalDateTime deadline);

    boolean isMoModifyOpen(LocalDateTime deadline);

    final class SaveDeadlineResult {
        private final boolean success;
        private final LocalDateTime deadline;
        private final String errorMessage;

        private SaveDeadlineResult(boolean success, LocalDateTime deadline, String errorMessage) {
            this.success = success;
            this.deadline = deadline;
            this.errorMessage = errorMessage;
        }

        public static SaveDeadlineResult success(LocalDateTime deadline) {
            return new SaveDeadlineResult(true, deadline, null);
        }

        public static SaveDeadlineResult error(String errorMessage) {
            return new SaveDeadlineResult(false, null, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public LocalDateTime getDeadline() {
            return deadline;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

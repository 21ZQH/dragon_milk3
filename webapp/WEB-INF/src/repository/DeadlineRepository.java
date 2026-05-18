package repository;

import java.time.LocalDateTime;

public interface DeadlineRepository {
    LocalDateTime getApplicationDeadline();

    void saveApplicationDeadline(LocalDateTime deadline);

    LocalDateTime getMoModifyDeadline();

    void saveMoModifyDeadline(LocalDateTime deadline);
}

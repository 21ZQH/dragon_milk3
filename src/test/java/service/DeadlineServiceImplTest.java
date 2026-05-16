package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import service.impl.DeadlineServiceImpl;

class DeadlineServiceImplTest {
    private final DeadlineService service = new DeadlineServiceImpl();

    @Test
    void applicationIsOpenUntilDeadlinePasses() {
        assertTrue(service.isApplicationOpen(null));
        assertTrue(service.isApplicationOpen(LocalDateTime.now().plusMinutes(1)));
        assertFalse(service.isApplicationOpen(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void parsesDeadlineFormFields() {
        DeadlineService.SaveDeadlineResult result = service.parseDeadline("2026-05-16", "09:30");

        assertTrue(result.isSuccess());
        assertEquals(LocalDateTime.of(2026, 5, 16, 9, 30), result.getDeadline());
    }

    @Test
    void rejectsInvalidDeadlineFormFields() {
        DeadlineService.SaveDeadlineResult result = service.parseDeadline("invalid", "09:30");

        assertFalse(result.isSuccess());
        assertEquals("Invalid deadline format.", result.getErrorMessage());
    }
}

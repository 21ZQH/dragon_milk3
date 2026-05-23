package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import service.impl.DeadlineServiceImpl;

/**
 * Unit tests for {@link DeadlineServiceImpl} in the TA Recruitment system.
 * Verifies deadline parsing and application window logic.
 */
class DeadlineServiceImplTest {
    /** The service implementation under test. */
    private final DeadlineService service = new DeadlineServiceImpl();

    /**
     * Tests that the application window is considered open when the deadline
     * is null or in the future, and closed once the deadline has passed.
     */
    @Test
    void applicationIsOpenUntilDeadlinePasses() {
        assertTrue(service.isApplicationOpen(null));
        assertTrue(service.isApplicationOpen(LocalDateTime.now().plusMinutes(1)));
        assertFalse(service.isApplicationOpen(LocalDateTime.now().minusMinutes(1)));
    }

    /**
     * Tests that valid date and time strings are correctly parsed into a
     * {@link LocalDateTime} and reported as a successful result.
     */
    @Test
    void parsesDeadlineFormFields() {
        DeadlineService.SaveDeadlineResult result = service.parseDeadline("2026-05-16", "09:30");

        assertTrue(result.isSuccess());
        assertEquals(LocalDateTime.of(2026, 5, 16, 9, 30), result.getDeadline());
    }

    /**
     * Tests that an invalid date string is rejected, returning a failed result
     * with an appropriate error message.
     */
    @Test
    void rejectsInvalidDeadlineFormFields() {
        DeadlineService.SaveDeadlineResult result = service.parseDeadline("invalid", "09:30");

        assertFalse(result.isSuccess());
        assertEquals("Invalid deadline format.", result.getErrorMessage());
    }
}

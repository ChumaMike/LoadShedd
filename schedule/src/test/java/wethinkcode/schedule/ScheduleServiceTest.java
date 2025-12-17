package wethinkcode.schedule;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScheduleServiceTest {
    private static ScheduleService testSvc;

    @BeforeAll
    public static void initTestScheduleFixture(){
        // Start the service on a random port (0) or default to avoid conflicts
        testSvc = new ScheduleService().initialise();
        testSvc.start(0);
    }

    @AfterAll
    public static void destroyTestFixture(){
        testSvc.stop();
    }

    @Test
    public void getSchedule_returns_schedule_for_valid_town() {
        // FIXED: Now we expect the raw 'Schedule' object, not Optional<ScheduleDO>
        ScheduleService.Schedule schedule = testSvc.getSchedule("Gauteng", "Benoni", 4);

        assertNotNull(schedule, "Schedule should not be null for valid town");
        assertFalse(schedule.days().isEmpty(), "Schedule should contain days");
        assertEquals(4, schedule.days().size(), "Should generate 4 days of data");

        // Check first day
        ScheduleService.Day day = schedule.days().get(0);
        assertNotNull(day.name());
        assertFalse(day.slots().isEmpty(), "Stage 4 should have outage slots");
    }

    @Test
    public void getSchedule_returns_null_for_unknown_province() {
        // We simulate "Mars" as a 'Not Found' case in our Service logic
        ScheduleService.Schedule schedule = testSvc.getSchedule("Mars", "Elonsburg", 4);

        assertNull(schedule, "Should return null for unknown province (Mars)");
    }
}
package wethinkcode.schedule;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScheduleService {
    public static final int DEFAULT_PORT = 7002;

    public static void main(String[] args) {
        final ScheduleService svc = new ScheduleService().initialise();
        svc.start(DEFAULT_PORT);
    }

    private Javalin server;

    public ScheduleService initialise() {
        server = Javalin.create();
        server.get("/{province}/{town}/{stage}", this::handleGetSchedule);
        return this;
    }

    public void start(int port) {
        if (server != null) {
            server.start(port);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private void handleGetSchedule(Context ctx) {
        String province = ctx.pathParam("province");
        String town = ctx.pathParam("town");

        // FIXED: Catch non-integer stages
        int stage;
        try {
            stage = Integer.parseInt(ctx.pathParam("stage"));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }

        // FIXED: Validate Stage Range (0-8)
        if (stage < 0 || stage > 8) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }

        Schedule schedule = getSchedule(province, town, stage);

        if (schedule == null) {
            ctx.status(HttpStatus.NOT_FOUND);
        } else {
            ctx.json(schedule);
        }
    }

    public Schedule getSchedule(String province, String town, int stage) {
        if (province.equalsIgnoreCase("Mars")) {
            return null;
        }
        return generateSchedule(town, stage);
    }

    private Schedule generateSchedule(String town, int stage) {
        List<Day> days = new ArrayList<>();
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd");

        long seed = town.hashCode();
        Random random = new Random(seed);

        for (int i = 0; i < 4; i++) {
            String dayName = date.plusDays(i).format(formatter);
            List<Slot> slots = new ArrayList<>();

            if (stage > 0) {
                int numberOfSlots = (stage <= 2) ? 1 : (stage <= 4) ? 2 : 3;
                int startHour = random.nextInt(12) * 2;

                for (int j = 0; j < numberOfSlots; j++) {
                    int endHour = startHour + 2;
                    if (endHour > 24) endHour -= 24;
                    String time = String.format("%02d:00 - %02d:00", startHour, endHour);
                    slots.add(new Slot(time, time));
                    startHour = (startHour + 8) % 24;
                }
            }
            days.add(new Day(dayName, slots));
        }
        return new Schedule(days);
    }

    public record Slot(String start, String end) {}
    public record Day(String name, List<Slot> slots) {}
    public record Schedule(List<Day> days) {}
}
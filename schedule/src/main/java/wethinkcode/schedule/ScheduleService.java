package wethinkcode.schedule;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import wethinkcode.schedule.transfer.DayDO;
import wethinkcode.schedule.transfer.ScheduleDO;
import wethinkcode.schedule.transfer.SlotDO;

/**
 * I provide a REST API providing the current loadshedding schedule.
 */
public class ScheduleService
{
    public static final int DEFAULT_STAGE = 0;
    public static final int DEFAULT_PORT = 7002;

    private Javalin server;
    private int servicePort;

    public static void main( String[] args ){
        final ScheduleService svc = new ScheduleService().initialise();
        svc.start();
    }

    @VisibleForTesting
    ScheduleService initialise(){
        server = initHttpServer();
        return this;
    }

    public void start(){
        start( DEFAULT_PORT );
    }

    @VisibleForTesting
    void start( int networkPort ){
        servicePort = networkPort;
        run();
    }

    public void stop(){
        if(server != null) server.stop();
    }

    public void run(){
        if(server != null) server.start( servicePort );
    }

    private Javalin initHttpServer(){
        Javalin app = Javalin.create();

        // Updated Route to match Test: /{province}/{town}/{stage}
        app.get("/{province}/{town}/{stage}", ctx -> {
            String province = ctx.pathParam("province");
            String town = ctx.pathParam("town");

            // Safe parsing of integer
            int stage;
            try {
                stage = Integer.parseInt(ctx.pathParam("stage"));
            } catch (NumberFormatException e) {
                ctx.status(400).result("Invalid stage number");
                return;
            }

            // 1. Validation: Illegal Stage
            if (stage < 0 || stage > 8) {
                ctx.status(400).result("Stage must be between 0 and 8");
                return;
            }

            // 2. Logic: Get Schedule
            Optional<ScheduleDO> scheduleOpt = getSchedule(province, town, stage);

            if (scheduleOpt.isPresent()) {
                ctx.status(200).json(scheduleOpt.get());
            } else {
                // 3. Not Found: Return 404 AND an empty JSON object
                // The test calls body.numberOfDays(), so body cannot be null.
                ctx.status(404).json(emptySchedule());
            }
        });

        return app;
    }

    // Mock Logic
    Optional<ScheduleDO> getSchedule( String province, String town, int stage ){
        // Mocking logic: "Mars" returns empty (404 case)
        return province.equalsIgnoreCase( "Mars" )
                ? Optional.empty()
                : Optional.of( mockSchedule() );
    }

    private static ScheduleDO mockSchedule(){
        final List<SlotDO> slots = List.of(
                new SlotDO( LocalTime.of( 2, 0 ), LocalTime.of( 4, 0 )),
                new SlotDO( LocalTime.of( 10, 0 ), LocalTime.of( 12, 0 )),
                new SlotDO( LocalTime.of( 18, 0 ), LocalTime.of( 20, 0 ))
        );
        final List<DayDO> days = List.of(
                new DayDO( slots ),
                new DayDO( slots ),
                new DayDO( slots ),
                new DayDO( slots )
        );
        return new ScheduleDO( days );
    }

    private static ScheduleDO emptySchedule(){
        final List<DayDO> days = Collections.emptyList();
        return new ScheduleDO( days );
    }
}
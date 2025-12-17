package wethinkcode.stage;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class StageService
{
    public static final int DEFAULT_STAGE = 0; // no loadshedding. Ha!
    public static final int DEFAULT_PORT = 7001;

    public static void main( String[] args ){
        final StageService svc = new StageService().initialise();
        svc.start();
    }

    private int loadSheddingStage;
    private Javalin server;
    private int servicePort;

    @VisibleForTesting
    StageService initialise(){
        return initialise( DEFAULT_STAGE );
    }

    @VisibleForTesting
    StageService initialise( int initialStage ){
        loadSheddingStage = initialStage;
        if (loadSheddingStage < 0) loadSheddingStage = 0; // Safety clamp

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
        if (server != null) {
            server.stop();
        }
    }

    public void run(){
        if (server != null) {
            server.start( servicePort );
        }
    }

    private Javalin initHttpServer(){
        Javalin app = Javalin.create();

        // 1. GET /stage
        app.get("/stage", ctx -> {
            ctx.json(new StageDO(this.loadSheddingStage));
        });

        // 2. POST /stage
        app.post("/stage", ctx -> {
            // Deserialize JSON body to StageDO
            StageDO newStageData = ctx.bodyAsClass(StageDO.class);
            int newStage = newStageData.getStage();

            // Validate Range (0 to 8)
            if (newStage >= 0 && newStage <= 8) {
                this.loadSheddingStage = newStage;
                ctx.status(200).json(new StageDO(this.loadSheddingStage));
            } else {
                // Invalid stage: Return 400 Bad Request
                ctx.status(400).result("Invalid stage. Must be 0-8.");
            }
        });

        return app;
    }
}
package wethinkcode.stage;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import wethinkcode.loadshed.common.transfer.StageDO;

/**
 * I provide a REST API that reports the current loadshedding "stage".
 */
public class StageService
{
    public static final int DEFAULT_STAGE = 0; // no loadshedding. Ha!
    public static final int DEFAULT_PORT = 7001;
    public static final String MQ_TOPIC_NAME = "stage";

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
        // Clamp to 0 if negative, though validation happens in setter too
        if(loadSheddingStage < 0) loadSheddingStage = 0;

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
        return Javalin.create()
                .get( "/stage", this::getCurrentStage )
                .post( "/stage", this::setNewStage );
    }

    private Context getCurrentStage( Context ctx ){
        return ctx.json( new StageDO( loadSheddingStage ) );
    }

    private Context setNewStage( Context ctx ){
        final StageDO stageData = ctx.bodyAsClass( StageDO.class );
        final int newStage = stageData.getStage();

        // Simple validation
        if( newStage >= 0 && newStage <= 8 ){
            loadSheddingStage = newStage;
            broadcastStageChangeEvent( ctx ); // Now this won't crash!
            ctx.status( HttpStatus.OK );
        }else{
            ctx.status( HttpStatus.BAD_REQUEST );
        }
        return ctx.json( new StageDO( loadSheddingStage ) );
    }

    private void broadcastStageChangeEvent( Context ctx ){
        // throw new UnsupportedOperationException( "TODO" );
        // TODO: Iteration 4 task.
        // We leave this empty for Iteration 3 so the API works without crashing.
    }
}
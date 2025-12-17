package wethinkcode.stage;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import wethinkcode.loadshed.common.mq.MQ;
import wethinkcode.loadshed.common.transfer.StageDO;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StageService
{
    public static final int DEFAULT_STAGE = 0;
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
        int newStage = -1;
        String body = ctx.body().trim();

        // 1. Try parsing as a Raw Number (e.g. "5")
        try {
            newStage = Integer.parseInt(body);
        } catch (NumberFormatException e) {
            // 2. If that fails, try parsing as JSON (e.g. "{\"stage\": 5}")
            try {
                StageDO stageData = ctx.bodyAsClass( StageDO.class );
                newStage = stageData.getStage();
            } catch (Exception jsonEx) {
                ctx.status(HttpStatus.BAD_REQUEST).result("Invalid input. Send a number (0-8).");
                return ctx;
            }
        }

        // 3. Validate Range and Broadcast
        if( newStage >= 0 && newStage <= 8 ){
            loadSheddingStage = newStage;
            broadcastStageChangeEvent( new StageDO(loadSheddingStage) );
            ctx.status( HttpStatus.OK );
        }else{
            ctx.status( HttpStatus.BAD_REQUEST ).result("Stage must be between 0 and 8.");
        }
        return ctx.json( new StageDO( loadSheddingStage ) );
    }

    // --- MQ LOGIC ---

    private void broadcastStageChangeEvent( StageDO stageDO ){
        Connection connection = null;
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ.URL);
            connection = factory.createConnection(MQ.USER, MQ.PASSWD);
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(MQ_TOPIC_NAME);
            MessageProducer producer = session.createProducer(topic);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(stageDO);
            TextMessage message = session.createTextMessage(json);

            producer.send(message);
            System.out.println("MQ: Sent Stage Update -> " + json);

        } catch (Exception e) {
            System.err.println("MQ Error: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (JMSException e) { /* ignore */ }
        }
    }
}
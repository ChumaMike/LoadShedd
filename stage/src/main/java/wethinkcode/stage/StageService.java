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

/**
 * I provide a REST API that reports the current loadshedding "stage".
 */
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
        final StageDO stageData = ctx.bodyAsClass( StageDO.class );
        final int newStage = stageData.getStage();

        if( newStage >= 0 && newStage <= 8 ){
            loadSheddingStage = newStage;
            broadcastStageChangeEvent( new StageDO(loadSheddingStage) );
            ctx.status( HttpStatus.OK );
        }else{
            ctx.status( HttpStatus.BAD_REQUEST );
        }
        return ctx.json( new StageDO( loadSheddingStage ) );
    }

    // --- MQ LOGIC ---

    private void broadcastStageChangeEvent( StageDO stageDO ){
        Connection connection = null;
        try {
            // 1. Create Connection Factory
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ.URL);

            // 2. Create Connection
            connection = factory.createConnection(MQ.USER, MQ.PASSWD);
            connection.start();

            // 3. Create Session (non-transacted, auto-ack)
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 4. Create Topic
            Topic topic = session.createTopic(MQ_TOPIC_NAME);

            // 5. Create Producer
            MessageProducer producer = session.createProducer(topic);

            // 6. Create JSON Message
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(stageDO);
            TextMessage message = session.createTextMessage(json);

            // 7. Send
            producer.send(message);
            System.out.println("MQ: Sent Stage Update -> " + json);

        } catch (Exception e) {
            System.err.println("MQ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 8. Clean up
            try {
                if (connection != null) connection.close();
            } catch (JMSException e) { /* ignore */ }
        }
    }
}
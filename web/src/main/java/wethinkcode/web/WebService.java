package wethinkcode.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import wethinkcode.loadshed.common.mq.MQ; // Uses common MQ config

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.List;
import java.util.Map;

public class WebService
{
    public static final int DEFAULT_PORT = 7003;
    public static final String STAGE_SVC_URL = "http://localhost:7001";
    public static final String PLACES_SVC_URL = "http://localhost:7000";
    public static final String SCHEDULE_SVC_URL = "http://localhost:7002";

    // A local cache of the stage.
    // We update this via MQ so we don't always have to ask the Stage Service via HTTP.
    private static int currentStage = 0;

    public static void main( String[] args ){
        final WebService svc = new WebService().initialise();
        svc.start();
    }

    private Javalin server;
    private int servicePort;

    public WebService initialise(){
        configureHttpClient();
        startStageListener(); // <--- START LISTENING TO MQ
        server = configureHttpServer();
        return this;
    }

    public void start(){
        start( DEFAULT_PORT );
    }

    public void start( int networkPort ){
        servicePort = networkPort;
        run();
    }

    public void stop(){
        if(server != null) server.stop();
    }

    public void run(){
        if(server != null) server.start( servicePort );
    }

    private void configureHttpClient(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Unirest.config().setObjectMapper(new kong.unirest.jackson.JacksonObjectMapper(mapper));
    }

    // --- MQ LISTENER ---
    private void startStageListener() {
        new Thread(() -> {
            try {
                System.out.println("WEB: Connecting to MQ at " + MQ.URL);
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ.URL);
                Connection connection = factory.createConnection(MQ.USER, MQ.PASSWD);
                connection.start();

                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic("stage");
                MessageConsumer consumer = session.createConsumer(topic);

                consumer.setMessageListener(message -> {
                    try {
                        if (message instanceof TextMessage) {
                            String json = ((TextMessage) message).getText();
                            System.out.println("WEB: Received Stage Update! -> " + json);

                            // Parse simple JSON to update local cache
                            // Expected format: {"stage": 4}
                            if (json.contains("\"stage\"")) {
                                String num = json.replaceAll("[^0-9]", "");
                                currentStage = Integer.parseInt(num);
                                System.out.println("WEB: Cache updated to Stage " + currentStage);
                            }
                        }
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("WEB: Could not connect to MQ (Is the broker running?): " + e.getMessage());
            }
        }).start();
    }

    private Javalin configureHttpServer(){
        TemplateEngine engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        engine.setTemplateResolver(resolver);
        JavalinThymeleaf.init(engine);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/html", Location.CLASSPATH);
        });

        // Routes
        app.get("/", ctx -> {
            // OPTIONAL: We can use our cached stage now, or still fetch fresh.
            // Let's use the cache to prove MQ works!
            List<String> provinces = getProvinces();

            ctx.render("index.html", Map.of(
                    "stage", currentStage, // Uses the MQ-updated value
                    "provinces", provinces
            ));
        });

        app.get("/towns/{province}", ctx -> {
            String province = ctx.pathParam("province");
            HttpResponse<JsonNode> response = Unirest.get(PLACES_SVC_URL + "/towns/" + province).asJson();
            ctx.contentType("application/json").result(response.getBody().toString());
        });

        app.post("/schedule", ctx -> {
            String province = ctx.formParam("province");
            String town = ctx.formParam("town");

            Object scheduleData = getSchedule(province, town, currentStage);
            List<String> provinces = getProvinces();

            ctx.render("index.html", Map.of(
                    "stage", currentStage,
                    "provinces", provinces,
                    "selectedProvince", province,
                    "selectedTown", town,
                    "schedule", scheduleData
            ));
        });

        return app;
    }

    private List<String> getProvinces() {
        try {
            HttpResponse<JsonNode> response = Unirest.get(PLACES_SVC_URL + "/provinces").asJson();
            if (response.getStatus() == 200) {
                JSONArray arr = response.getBody().getArray();
                List<Object> list = arr.toList();
                return list.stream().map(Object::toString).toList();
            }
        } catch (Exception e) { /* Ignore */ }
        return List.of();
    }

    private Object getSchedule(String province, String town, int stage) {
        try {
            String url = String.format("%s/%s/%s/%d", SCHEDULE_SVC_URL, province, town, stage);
            HttpResponse<JsonNode> response = Unirest.get(url).asJson();
            if (response.getStatus() == 200) return response.getBody().getObject().toMap();
        } catch (Exception e) { /* Ignore */ }
        return null;
    }
}
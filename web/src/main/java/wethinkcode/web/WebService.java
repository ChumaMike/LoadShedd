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
import wethinkcode.loadshed.common.mq.MQ;

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

    private static int currentStage = 0;

    public static void main( String[] args ){
        new WebService().initialise().start();
    }

    private Javalin server;

    public WebService initialise(){
        configureHttpClient();
        startStageListener();
        server = configureHttpServer();
        return this;
    }

    public void start(){
        server.start( DEFAULT_PORT );
    }

    private void configureHttpClient(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Unirest.config().setObjectMapper(new kong.unirest.jackson.JacksonObjectMapper(mapper));
    }

    private void startStageListener() {
        new Thread(() -> {
            try {
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
                            if (json.contains("\"stage\"")) {
                                String num = json.replaceAll("[^0-9]", "");
                                currentStage = Integer.parseInt(num);
                                System.out.println("WEB: Cache updated to Stage " + currentStage);
                            }
                        }
                    } catch (JMSException e) { e.printStackTrace(); }
                });
            } catch (Exception e) { System.err.println("MQ Ignored (Offline?)"); }
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

        return Javalin.create(config -> {
                    // FIX: Point to the "static" folder where you put js/ and css/
                    // Note: We use "static" (no slash) to be safer with Classpath loading
                    config.staticFiles.add("static", Location.CLASSPATH);
                })
                .get("/", ctx -> {
                    ctx.render("index.html", Map.of("stage", currentStage, "provinces", getProvinces()));
                })
                .get("/api/towns/{province}", ctx -> {
                    String province = ctx.pathParam("province");
                    // Safety: Forward the request properly encoding the URL
                    String url = PLACES_SVC_URL + "/towns/" + province.replace(" ", "%20");
                    HttpResponse<JsonNode> response = Unirest.get(url).asJson();

                    if(response.getStatus() == 200) {
                        ctx.contentType("application/json").result(response.getBody().toString());
                    } else {
                        ctx.status(response.getStatus()).json(new JSONArray());
                    }
                })
                .get("/api/schedule/{province}/{town}", ctx -> {
                    String province = ctx.pathParam("province");
                    String town = ctx.pathParam("town");
                    Object schedule = getSchedule(province, town, currentStage);
                    if(schedule != null) ctx.json(schedule);
                    else ctx.status(404);
                })
                .get("/api/stage", ctx -> ctx.json(Map.of("stage", currentStage)));
    }

    private List<String> getProvinces() {
        try {
            HttpResponse<JsonNode> response = Unirest.get(PLACES_SVC_URL + "/provinces").asJson();
            if (response.getStatus() == 200) {
                JSONArray arr = response.getBody().getArray();
                return arr.toList().stream().map(Object::toString).toList();
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
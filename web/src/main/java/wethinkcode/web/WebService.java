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

import java.util.List;
import java.util.Map;

public class WebService
{
    // Use port 7003 instead of 80 to avoid permission issues
    public static final int DEFAULT_PORT = 7003;

    public static final String STAGE_SVC_URL = "http://localhost:7001";
    public static final String PLACES_SVC_URL = "http://localhost:7000";
    public static final String SCHEDULE_SVC_URL = "http://localhost:7002";

    public static void main( String[] args ){
        final WebService svc = new WebService().initialise();
        svc.start();
    }

    private Javalin server;
    private int servicePort;

    public WebService initialise(){
        configureHttpClient();
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

    private Javalin configureHttpServer(){
        // 1. Setup Thymeleaf for HTML rendering
        TemplateEngine engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        engine.setTemplateResolver(resolver);
        JavalinThymeleaf.init(engine);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/html", Location.CLASSPATH); // fallback for static assets
        });

        // 2. Define Routes

        // Home Page: Load Stage and Provinces
        app.get("/", ctx -> {
            int stage = getStage();
            List<String> provinces = getProvinces();

            ctx.render("index.html", Map.of(
                    "stage", stage,
                    "provinces", provinces
            ));
        });

        // API Proxy: Get Towns for a Province (called via AJAX/Fetch in browser)
        app.get("/towns/{province}", ctx -> {
            String province = ctx.pathParam("province");
            HttpResponse<JsonNode> response = Unirest.get(PLACES_SVC_URL + "/towns/" + province).asJson();
            ctx.contentType("application/json").result(response.getBody().toString());
        });

        // Form Submit: Get Schedule
        app.post("/schedule", ctx -> {
            String province = ctx.formParam("province");
            String town = ctx.formParam("town");
            int stage = getStage(); // Re-fetch current stage

            Object scheduleData = getSchedule(province, town, stage);
            List<String> provinces = getProvinces();

            ctx.render("index.html", Map.of(
                    "stage", stage,
                    "provinces", provinces,
                    "selectedProvince", province,
                    "selectedTown", town,
                    "schedule", scheduleData
            ));
        });

        return app;
    }

    // --- Helper Methods to talk to Backend Services ---

    private int getStage() {
        try {
            HttpResponse<JsonNode> response = Unirest.get(STAGE_SVC_URL + "/stage").asJson();
            if (response.getStatus() == 200) {
                return response.getBody().getObject().getInt("stage");
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to Stage Service: " + e.getMessage());
        }
        return -1; // Error state
    }

    private List<String> getProvinces() {
        try {
            HttpResponse<JsonNode> response = Unirest.get(PLACES_SVC_URL + "/provinces").asJson();
            if (response.getStatus() == 200) {
                JSONArray arr = response.getBody().getArray();
                // Convert JSONArray to List<String>
                List<Object> list = arr.toList();
                return list.stream().map(Object::toString).toList();
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to Places Service: " + e.getMessage());
        }
        return List.of();
    }

    private Object getSchedule(String province, String town, int stage) {
        try {
            // URL: /province/town/stage
            String url = String.format("%s/%s/%s/%d", SCHEDULE_SVC_URL, province, town, stage);
            HttpResponse<JsonNode> response = Unirest.get(url).asJson();

            if (response.getStatus() == 200) {
                // Return the raw JSON object to be rendered by Thymeleaf (or parsed)
                // For simplicity, we pass the Map representation of the JSON
                return response.getBody().getObject().toMap();
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to Schedule Service: " + e.getMessage());
        }
        return null;
    }
}
package wethinkcode.schedule;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleServiceAPITest {
    private static ScheduleService service;
    private static final int TEST_PORT = 8888;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;

    @BeforeAll
    public static void startServer() {
        service = new ScheduleService().initialise();
        service.start(TEST_PORT);
    }

    @AfterAll
    public static void stopServer() {
        service.stop();
    }

    @Test
    public void getSchedule_someTown() {
        // We request a schedule for a valid town
        HttpResponse<JsonNode> response = Unirest.get(BASE_URL + "/Gauteng/Benoni/4").asJson();

        assertEquals(200, response.getStatus());

        // Verify the structure matches our "Smart" logic
        JSONObject json = response.getBody().getObject();
        assertTrue(json.has("days"), "Response should contain 'days'");
        assertEquals(4, json.getJSONArray("days").length(), "Should return 4 days of data");
    }

    @Test
    public void getSchedule_nonexistentTown() {
        // "Mars" is our trigger for 404 in the Service logic
        HttpResponse<JsonNode> response = Unirest.get(BASE_URL + "/Mars/Elonsburg/4").asJson();

        assertEquals(404, response.getStatus());
    }

    @Test
    public void illegalStage() {
        // Stage 99 is invalid
        HttpResponse<JsonNode> response = Unirest.get(BASE_URL + "/Gauteng/Benoni/99").asJson();
        assertEquals(400, response.getStatus());

        // Stage -1 is invalid
        response = Unirest.get(BASE_URL + "/Gauteng/Benoni/-1").asJson();
        assertEquals(400, response.getStatus());
    }
}
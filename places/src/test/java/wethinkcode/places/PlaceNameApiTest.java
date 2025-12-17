package wethinkcode.places;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.*;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;

/**
 * *Functional* tests of the PlaceNameService.
 */
@Tag( "system" )
@TestInstance( TestInstance.Lifecycle.PER_CLASS )
public class PlaceNameApiTest
{
    public static final int TEST_PORT = 7777;
    private static PlaceNameService server;
    private static File testDataFile;

    @BeforeAll
    public static void startServer() throws IOException {
        testDataFile = File.createTempFile("places_test", ".csv");
        testDataFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(testDataFile)) {
            writer.write(PlacesTestData.CSV_DATA);
        }

        server = new PlaceNameService().initialise();

        // 1. Configure the filename via Picocli (but don't execute run)
        String[] args = {"-f", testDataFile.getAbsolutePath()};
        new CommandLine(server).parseArgs(args);

        // 2. Manually load data
        server.loadData();

        // 3. Start on the custom test port
        server.start(TEST_PORT);
    }

    @AfterAll
    public static void stopServer(){
        if(server != null) server.stop();
    }

    @Test
    public void getProvincesJson(){
        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/provinces" ).asJson();
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));

        JSONArray jsonArray = response.getBody().getArray();
        assertTrue(jsonArray.length() > 0);

        boolean found = false;
        for(int i=0; i<jsonArray.length(); i++){
            if(jsonArray.getString(i).equals("KwaZulu-Natal")) found = true;
        }
        assertTrue(found, "Expected KwaZulu-Natal in provinces list");
    }

    @Test
    public void getTownsInAProvince_provinceExistsInDb(){
        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/towns/KwaZulu-Natal" ).asJson();
        assertEquals(200, response.getStatus());

        JSONArray jsonArray = response.getBody().getArray();
        assertTrue(jsonArray.length() > 0);

        boolean found = false;
        for(int i=0; i<jsonArray.length(); i++){
            JSONObject town = jsonArray.getJSONObject(i);
            if(town.getString("name").equals("Amatikulu")) found = true;
        }
        assertTrue(found, "Expected Amatikulu in KZN towns list");
    }

    @Test
    public void getTownsInAProvince_noSuchProvinceInDb(){
        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/towns/Oregon" ).asJson();
        assertEquals(200, response.getStatus());
        JSONArray jsonArray = response.getBody().getArray();
        assertEquals(0, jsonArray.length());
    }

    private String serverUrl(){
        return "http://localhost:" + TEST_PORT;
    }
}
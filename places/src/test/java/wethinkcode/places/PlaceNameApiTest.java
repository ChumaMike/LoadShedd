package wethinkcode.places;

import java.io.IOException;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;

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

    @BeforeAll
    public static void startServer() throws IOException{
        throw new UnsupportedOperationException( "TODO" );
    }

    @AfterAll
    public static void stopServer(){
        throw new UnsupportedOperationException( "TODO" );
    }

    @Test
    public void getProvincesJson(){
        fail( "TODO" );
    }

    @Test
    public void getTownsInAProvince_provinceExistsInDb(){
        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/towns/KwaZulu-Natal" ).asJson();
        fail( "TODO" );
    }

    @Test
    public void getTownsInAProvince_noSuchProvinceInDb(){
        HttpResponse<JsonNode> response = Unirest.get( serverUrl() + "/towns/Oregon" ).asJson();
        fail( "TODO" );
    }

    private String serverUrl(){
        return "http://localhost:" + TEST_PORT;
    }
}

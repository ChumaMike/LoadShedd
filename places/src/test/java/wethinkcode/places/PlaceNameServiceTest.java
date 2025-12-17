package wethinkcode.places;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for the PlaceNameService.
 */
public class PlaceNameServiceTest
{
    @Test
    public void getACsvFileIntoTheServer(){
        try{

            final File csvFile = createTestCsvFile();
            final String[] args = {"-f", csvFile.getPath()};

            fail( "TODO" );
            
        }catch( IOException ex ){
            fail( ex );
        }

    }

    private File createTestCsvFile() throws IOException{
        final File f = File.createTempFile( "places", "csv" );
        f.deleteOnExit();

        try(  FileWriter out = new FileWriter( f ) ){
            out.write( PlacesTestData.CSV_DATA );
            return f;
        }
    }
}

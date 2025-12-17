package wethinkcode.places;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.*;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for the PlaceNameService.
 */
public class PlaceNameServiceTest
{
    @Test
    public void getACsvFileIntoTheServer(){
        try {
            // 1. Create a dummy CSV file
            final File csvFile = createTestCsvFile();

            // 2. Prepare the arguments (-f matches the Option we added to the Service)
            final String[] args = {"-f", csvFile.getPath()};

            // 3. Initialise the Service
            final PlaceNameService svc = new PlaceNameService().initialise();

            // 4. Run the Picocli command logic (parses args -> calls svc.run())
            int exitCode = new CommandLine( svc ).execute( args );

            // 5. Verify it succeeded
            assertEquals( 0, exitCode, "Service failed to start or parse arguments" );

            // 6. Cleanup: Stop the server to free up port 7000
            svc.stop();

        } catch( IOException ex ){
            fail( ex );
        }
    }

    private File createTestCsvFile() throws IOException{
        final File f = File.createTempFile( "places", "csv" );
        f.deleteOnExit();

        try( FileWriter out = new FileWriter( f ) ){
            out.write( PlacesTestData.CSV_DATA );
            return f;
        }
    }
}
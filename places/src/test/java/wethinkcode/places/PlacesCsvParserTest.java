package wethinkcode.places;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

import org.junit.jupiter.api.*;
import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit-test suite for the CSV parser.
 */
public class PlacesCsvParserTest
{
    private final LineNumberReader input = new LineNumberReader( new StringReader( PlacesTestData.CSV_DATA ));
    private final PlacesCsvParser parser = new PlacesCsvParser();

    // The first 3 tests are about checking the basic parsing of a line of CSV data.

    @Test
    public void firstLineGetsSkipped() throws IOException {
        // We create a reader with ONLY the header line
        String headerOnly = "Name,Feature_Description,pklid,Latitude,Longitude,Date,MapInfo,Province,fklFeatureSubTypeID,Previous_Name,fklMagisterialDistrictID,ProvinceID,fklLanguageID,fklDisteral,Local Municipality,Sound,District Municipality,fklLocalMunic,Comments,Meaning\n";
        LineNumberReader headerInput = new LineNumberReader(new StringReader(headerOnly));

        // Attempt to parse
        Places db = parser.parseDataLines(headerInput);

        // Should be empty because the first line is always skipped
        assertEquals(0, db.size());
    }

    @Test
    public void splittingALineIntoValuesProducesCorrectNumberOfValues(){
        // Note: We need the header first, otherwise the parser skips this valid line thinking it's the header
        final String testLine = "Name,Feature...,Province...\n" +
                "Brakpan,Urban Area,92797,-26.60444444,26.34,01-06-1992,,North West,66,,262,8,16,DC40,Matlosana,,,NW403,,";

        LineNumberReader oneLineInput = new LineNumberReader(new StringReader(testLine));
        Places db = parser.parseDataLines(oneLineInput);

        assertEquals(1, db.size());

        Town t = db.townsIn("North West").iterator().next();
        assertEquals("Brakpan", t.getName());
    }

    @Test
    public void splittingALineWithTheWrongNumberOfValuesReportsAndErrorLine(){
        // A line missing most columns (broken data)
        final String testLine = "Name,Feature...,Province...\n" +
                "Brakpan,Urban Area,92797";

        LineNumberReader badInput = new LineNumberReader(new StringReader(testLine));
        Places db = parser.parseDataLines(badInput);

        // Should skip the bad line
        assertEquals(0, db.size());
    }

    @Test
    public void urbanAreasAreWanted(){
        final String testLine = "Name,Feature...,Province...\n" +
                "Brakpan,Urban Area,92799,-26.23527778,28.37,31-05-1995,,Gauteng,114,,280,3,16,EKU,Ekurhuleni Metro,,,EKU,,\n";

        LineNumberReader urbanInput = new LineNumberReader(new StringReader(testLine));
        Places db = parser.parseDataLines(urbanInput);

        assertEquals(1, db.size());
    }

    // The next 3 tests are about filtering the bulk data to extract only data that is
    // relevant to our needs.

    @Test
    public void townsAreWanted(){
        final String testLine = "Name,Feature...,Province...\n" +
                "Brakpan,Town,92802,-27.95111111,26.53333333,30-05-1975,,Free State,68,,155,2,16,DC18,Matjhabeng,,,FS184,,";

        LineNumberReader townInput = new LineNumberReader(new StringReader(testLine));
        Places db = parser.parseDataLines(townInput);

        assertEquals(1, db.size());
    }

    @Test
    public void otherFeaturesAreNotWanted(){
        // "Station" should be filtered out
        final String testLine = "Name,Feature...,Province...\n" +
                "Amatikulu,Station,95756,-29.05111111,31.53138889,31-05-1989,,KwaZulu-Natal,79,,237,4,16,DC28,uMlalazi,,,KZ284,,";

        LineNumberReader stationInput = new LineNumberReader(new StringReader(testLine));
        Places db = parser.parseDataLines(stationInput);

        assertEquals(0, db.size());
    }

    @Test
    public void parseBulkTestData(){
        // Uses the big block of data from PlacesTestData
        final Places db = parser.parseDataLines( input );
        assertEquals( 5, db.size() );
    }
}
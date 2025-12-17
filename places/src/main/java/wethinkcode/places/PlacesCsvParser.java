package wethinkcode.places;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import com.google.common.annotations.VisibleForTesting;
import wethinkcode.places.db.memory.PlacesDb;
import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

/**
 * PlacesCsvParser : I parse a CSV file with each line containing the fields (in order):
 * <code>Name, Feature_Description, ...</code>.
 */
public class PlacesCsvParser
{
    public Places parseCsvSource( File csvFile ) throws IOException {
        try (FileReader fr = new FileReader(csvFile);
             LineNumberReader lnr = new LineNumberReader(fr)) {
            return parseDataLines(lnr);
        }
    }

    @VisibleForTesting
    Places parseDataLines( final LineNumberReader in ) {
        // Instantiate the concrete implementation
        PlacesDb places = new PlacesDb();
        String line;

        try {
            while ((line = in.readLine()) != null) {
                if (in.getLineNumber() == 1) continue;

                String[] columns = line.split(",");
                if (columns.length < 8) continue;

                String name = columns[0].trim();
                String featureType = columns[1].trim();
                String province = columns[7].trim();

                if (isOccupiedPlace(featureType)) {
                    places.add(new Town(name, province));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV data lines", e);
        }

        return places;
    }

    private boolean isOccupiedPlace(String featureType) {
        return "Town".equalsIgnoreCase(featureType)
                || "Urban Area".equalsIgnoreCase(featureType)
                || "Township".equalsIgnoreCase(featureType);
    }
}
package wethinkcode.places;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import wethinkcode.places.db.memory.PlacesDb;
import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

import static java.util.Objects.requireNonNull;

/**
 * I parse the PlaceNamesZA CSV file.
 */
public class PlacesCsvParser
{
    // Restored Constants required by Tests
    static final int NAME_COLUMN = 0;
    static final int FEATURE_COLUMN = 1;
    static final int PROVINCE_COLUMN = 7;
    static final int MIN_COLUMNS = PROVINCE_COLUMN + 1; // Used by tests
    static final int MAX_COLUMNS = 20;                  // Used by tests

    private static final Set<String> WANTED_FEATURES = Set.of(
            "Urban Area".toLowerCase(),
            "Town".toLowerCase(),
            "Township".toLowerCase()
    );

    public Places parseCsvSource( File csvFile ) throws IOException {
        requireNonNull( csvFile );
        if( ! (csvFile.exists() && csvFile.canRead() )){
            throw new FileNotFoundException( "Required CSV input file " + csvFile.getPath() + " not found." );
        }

        return parseCsvSource( new LineNumberReader( new FileReader( csvFile ) ) );
    }

    public Places parseCsvSource( LineNumberReader reader ) throws IOException {
        try( final LineNumberReader in = Objects.requireNonNull( reader )){
            in.readLine();  // Skip header line
            return parseDataLines( in );
        }
    }

    @VisibleForTesting
    Places parseDataLines( final LineNumberReader in ){
        final Set<Town> allTowns = in.lines()
                .map( this::splitLineIntoValues )
                .filter( this::isLineAWantedFeature )
                .map( this::asTown )
                .collect( Collectors.toSet() );
        return new PlacesDb( allTowns );
    }

    @VisibleForTesting
    boolean isLineAWantedFeature( String[] csvValue ){
        // Safety: Ensure line has enough columns using the restored constant
        if (csvValue.length < MIN_COLUMNS) return false;

        String feature = csvValue[FEATURE_COLUMN].trim().toLowerCase();
        return WANTED_FEATURES.contains( feature );
    }

    @VisibleForTesting
    String[] splitLineIntoValues( String aCsvLine ){
        // Manual split required for this specific dataset structure
        return aCsvLine.trim().split( "," );
    }

    @VisibleForTesting
    Town asTown( String[] values ){
        String name = values[NAME_COLUMN].trim();
        String province = values[PROVINCE_COLUMN].trim();
        return new Town( name, province );
    }
}
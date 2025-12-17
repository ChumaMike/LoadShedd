package wethinkcode.places;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import wethinkcode.places.db.memory.PlacesDb;
import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

import static java.util.Objects.requireNonNull;

public class PlacesCsvParser
{
    // Common column mappings for PlaceNamesZA
    // 0: Name, 1: Feature, 2: Province (Usually)
    // If your CSV is different, we will rely on VALID_PROVINCES to filter garbage.
    static final int NAME_COLUMN = 0;
    static final int FEATURE_COLUMN = 1;
    static final int PROVINCE_COLUMN = 7; // Try 2 first. If it fails, try 3 or 4.

    // A "Allow List" of the 9 real provinces
    private static final Set<String> VALID_PROVINCES = Set.of(
            "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal",
            "Limpopo", "Mpumalanga", "North West", "Northern Cape", "Western Cape"
    );

    private static final Set<String> WANTED_FEATURES = Set.of(
            "urban area", "town", "township", "populated place"
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
            in.readLine();  // Skip header
            return parseDataLines( in );
        }
    }

    @VisibleForTesting
    Places parseDataLines( final LineNumberReader in ){
        final Set<Town> allTowns = in.lines()
                .map( this::splitLineIntoValues )
                .filter( this::isLineAWantedFeature )
                .filter( this::hasValidProvince ) // <--- NEW SMART CHECK
                .map( this::asTown )
                .collect( Collectors.toSet() );
        return new PlacesDb( allTowns );
    }

    @VisibleForTesting
    boolean isLineAWantedFeature( String[] csvValue ){
        if (csvValue.length <= PROVINCE_COLUMN) return false;
        String feature = csvValue[FEATURE_COLUMN].trim().toLowerCase();
        return WANTED_FEATURES.contains( feature );
    }

    // --- NEW METHOD: Checks if the column actually contains a real province ---
    private boolean hasValidProvince( String[] csvValue ){
        if (csvValue.length <= PROVINCE_COLUMN) return false;

        // Clean up the text (remove quotes, trim spaces)
        String rawValue = csvValue[PROVINCE_COLUMN].trim().replace("\"", "");

        // Check if this text is in our list of 9 provinces
        return VALID_PROVINCES.contains(rawValue);
    }

    @VisibleForTesting
    String[] splitLineIntoValues( String aCsvLine ){
        return aCsvLine.trim().split( "," );
    }

    @VisibleForTesting
    Town asTown( String[] values ){
        String name = values[NAME_COLUMN].trim().replace("\"", "");
        String province = values[PROVINCE_COLUMN].trim().replace("\"", "");
        return new Town( name, province );
    }
}
package wethinkcode.places;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;

import com.google.common.annotations.VisibleForTesting;
import wethinkcode.places.model.Places;


/**
 * PlacesCsvParser : I parse a CSV file with each line containing the fields (in order):
 * <code>Name, Feature_Description, pklid, Latitude, Longitude, Date, MapInfo, Province,
 * fklFeatureSubTypeID, Previous_Name, fklMagisterialDistrictID, ProvinceID, fklLanguageID,
 * fklDisteral, Local Municipality, Sound, District Municipality, fklLocalMunic, Comments, Meaning</code>.
 * <p>
 * For the PlaceNameService we're only really interested in the <code>Name</code>,
 * <code>Feature_Description</code> and <code>Province</code> fields.
 * <code>Feature_Description</code> allows us to distinguish towns and urban areas from
 * (e.g.) rivers, mountains, etc. since our PlaceNameService is only concerned with occupied places.
 */
public class PlacesCsvParser
{
    public Places parseCsvSource( File csvFile ) throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException( "TODO" );
    }

    @VisibleForTesting
    Places parseDataLines( final LineNumberReader in ){
        throw new UnsupportedOperationException( "TODO" );
    }
}

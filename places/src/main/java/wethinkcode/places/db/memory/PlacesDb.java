package wethinkcode.places.db.memory;

import java.util.Collection;

import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

/**
 * I am a concrete database that implements the {@code Places} API. I do
 * not persist anything to permanent storage, but simply hold the entire
 * dataset in memory. Accordingly, I must get initialised with the data
 * for every instance of me that gets created.
 */
public class PlacesDb implements Places
{
    @Override
    public Collection<String> provinces(){
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Collection<Town> townsIn( String aProvince ){
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public int size(){
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
package wethinkcode.places.db.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final Set<Town> towns = new HashSet<>();

    @Override
    public Collection<String> provinces(){
        return towns.stream()
                .map(Town::getProvince)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Town> townsIn( String aProvince ){
        return towns.stream()
                .filter(town -> town.getProvince().equalsIgnoreCase(aProvince))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public int size(){
        return towns.size();
    }

    // This method is not in the Interface but is required by the Parser to load data.
    public void add( Town town ){
        towns.add(town);
    }
}
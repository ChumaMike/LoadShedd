package wethinkcode.places;


import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import picocli.CommandLine;
import wethinkcode.places.model.Places;

/**
 * I provide a Place-names Service for places in South Africa.
 * <p>
 * I read place-name data from a CSV file that I read and
 * parse into the objects (domain model) that I use,
 * discarding unwanted data in the file (things like mountain/river names). With my "database"
 * built, I then serve-up place-name data as JSON to clients.
 * <p>
 * Clients can request:
 * <ul>
 * <li>a list of available Provinces
 * <li>a list of all Towns/PlaceNameService in a given Province
 * <li>a list of all neighbourhoods in a given Town
 * </ul>
 * I understand the following command-line arguments:
 * <dl>
 * <dt>-c | --config &lt;configfile&gt;
 * <dd>a file pathname referring to an (existing!) configuration file in standard Java
 *      properties-file format
 * <dt>-d | --datadir &lt;datadirectory&gt;
 * <dd>the name of a directory where CSV datafiles may be found. This option <em>overrides</em>
 *      and data-directory setting in a configuration file.
 * <dt>-p | --places &lt;csvdatafile&gt;
 * <dd>a file pathname referring to a CSV file of place-name data. This option
 *      <em>overrides</em> any value in a configuration file and will bypass any
 *      data-directory set via command-line or configuration.
 */
public class PlaceNameService implements Runnable {

    public static final int DEFAULT_SERVICE_PORT = 7000;

    // Configuration keys
    public static final String CFG_CONFIG_FILE = "config.file";
    public static final String CFG_DATA_DIR = "data.dir";
    public static final String CFG_DATA_FILE = "data.file";
    public static final String CFG_SERVICE_PORT = "server.port";

    public static void main( String[] args ){
        final PlaceNameService svc = new PlaceNameService().initialise();
        final int exitCode = new CommandLine( svc ).execute( args );
        System.exit( exitCode );
    }

    // Instance state

    private Javalin server;

    private Places places;

    public PlaceNameService(){
    }

    public void start(){
        throw new UnsupportedOperationException( "TODO" );
    }

    public void stop(){
        throw new UnsupportedOperationException( "TODO" );
    }

    /**
     * Why not put all of this into the constructor? Well, this way makes
     * it easier (possible!) to test an instance of PlaceNameService without
     * starting up all the big machinery (i.e. without calling initialise()).
     */
    @VisibleForTesting
    PlaceNameService initialise(){
        places = initPlacesDb();
        server = initHttpServer();
        return this;
    }

    @Override
    public void run(){
        throw new UnsupportedOperationException( "TODO" );
    }

    private Places initPlacesDb(){
        throw new UnsupportedOperationException( "TODO" );
    }

    private Javalin initHttpServer(){
        throw new UnsupportedOperationException( "TODO" );
    }
}
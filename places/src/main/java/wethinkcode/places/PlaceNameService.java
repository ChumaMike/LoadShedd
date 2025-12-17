package wethinkcode.places;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

/**
 * I provide a Place-names Service for places in South Africa.
 */
@Command( name = "PlaceNameService", mixinStandardHelpOptions = true )
public class PlaceNameService implements Runnable {

    public static final int DEFAULT_PORT = 7000;

    // Configuration keys
    public static final String CFG_CONFIG_FILE = "config.file";
    public static final String CFG_DATA_DIR = "data.dir";
    public static final String CFG_DATA_FILE = "data.file";
    public static final String CFG_SERVICE_PORT = "server.port";

    public static void main( String[] args ){
        // FIXED: Do NOT call .initialise() here.
        // Just create the instance.
        final PlaceNameService svc = new PlaceNameService();

        // Parse args. This populates -f / -p and then calls run().
        final int exitCode = new CommandLine( svc ).execute( args );

        // Only exit if there was a CLI error.
        // If success, keep JVM alive for the server.
        if (exitCode != 0) {
            System.exit( exitCode );
        }
    }

    private final Properties config;
    private Javalin server;
    private Places places;

    @Option( names = { "-c", "--config" }, description = "Configuration file path" )
    private File configFile;

    @Option( names = { "-d", "--datadir" }, description = "Directory pathname" )
    private File dataDir;

    @Option( names = { "-f", "--datafile" }, description = "CSV Data file path" )
    private File dataFile;

    @Option( names = { "-p", "--port" }, description = "Service network port number" )
    private int svcPort;

    public PlaceNameService(){
        config = initConfig();
    }

    public void start(){
        start( servicePort() );
    }

    @VisibleForTesting
    void start( int port ){
        server.start( port );
    }

    public void stop(){
        if (server != null) server.stop();
    }

    @VisibleForTesting
    PlaceNameService initialise(){
        // Reload config in case -c was passed via CLI
        if (configFile != null) {
            Properties overrides = initConfig();
            config.putAll(overrides);
        }

        places = initPlacesDb();
        server = initHttpServer();
        return this;
    }

    @VisibleForTesting
    PlaceNameService initialise( Places aPlaceDb ){
        places = aPlaceDb;
        server = initHttpServer();
        return this;
    }

    @Override
    public void run(){
        // FIXED: Initialise HERE, after arguments are parsed
        initialise();
        server.start( servicePort() );
    }

    private Properties initConfig(){
        try( FileReader in = new FileReader( configFile() )){
            final Properties p = new Properties( defaultConfig() );
            p.load( in );
            return p;
        }catch( IOException ex ){
            return defaultConfig();
        }
    }

    private Places initPlacesDb(){
        try{
            return new PlacesCsvParser().parseCsvSource( dataFile() );
        }catch( IOException ex ){
            System.err.println( "Error reading CSV file " + dataFile() + ": " + ex.getMessage() );
            throw new RuntimeException( ex );
        }
    }

    private Javalin initHttpServer(){
        return Javalin.create()
                .get( "/provinces", ctx -> ctx.json( places.provinces() ))
                .get( "/towns/{province}", this::getTowns );
    }

    private Context getTowns( Context ctx ){
        final String province = ctx.pathParam( "province" );
        final Collection<Town> towns = places.townsIn( province );
        return ctx.json( towns );
    }

    @VisibleForTesting
    File configFile(){
        return configFile != null
                ? configFile
                : new File( defaultConfig().getProperty( CFG_CONFIG_FILE ));
    }

    @VisibleForTesting
    File dataFile(){
        return dataFile != null
                ? dataFile
                : new File( getConfig( CFG_DATA_FILE ));
    }

    @VisibleForTesting
    File dataDir(){
        return dataDir != null
                ? dataDir
                : new File( getConfig( CFG_DATA_DIR ));
    }

    int servicePort(){
        return svcPort > 0
                ? svcPort
                : Integer.valueOf( getConfig( CFG_SERVICE_PORT ));
    }

    @VisibleForTesting
    String getConfig( String aKey ){
        return config.getProperty( aKey );
    }

    @VisibleForTesting
    Places getDb(){
        return places;
    }

    private static Properties defaultConfig(){
        final Properties p = new Properties();
        p.setProperty( CFG_CONFIG_FILE, System.getProperty( "user.dir" ) + "/places.properties" );
        p.setProperty( CFG_DATA_DIR, System.getProperty( "user.dir" ));
        // Default fallbacks
        p.setProperty( CFG_DATA_FILE, System.getProperty( "user.dir" ) + "/places/resources/PlaceNamesZA2008.csv" );
        p.setProperty(CFG_SERVICE_PORT, Integer.toString(DEFAULT_PORT ));
        return p;
    }
}
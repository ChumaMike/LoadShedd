package wethinkcode.places;

import java.io.File;
import java.io.IOException;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import wethinkcode.places.db.memory.PlacesDb;
import wethinkcode.places.model.Places;

/**
 * I provide a Place-names Service for places in South Africa.
 */
@Command(name = "PlaceNameService", mixinStandardHelpOptions = true, version = "1.0")
public class PlaceNameService implements Runnable {

    public static final int DEFAULT_SERVICE_PORT = 7000;

    public static final String CFG_CONFIG_FILE = "config.file";
    public static final String CFG_DATA_DIR = "data.dir";
    public static final String CFG_DATA_FILE = "data.file";
    public static final String CFG_SERVICE_PORT = "server.port";

    public static void main( String[] args ){
        final PlaceNameService svc = new PlaceNameService().initialise();
        final int exitCode = new CommandLine( svc ).execute( args );
        System.exit( exitCode );
    }

    @Option(names = { "-c", "--config" }, description = "Config file path")
    private File configFile;

    @Option(names = { "-d", "--datadir" }, description = "Data directory")
    private File dataDir;

    @Option(names = { "-p", "--places", "-f" }, description = "CSV Data file")
    private File placesFile;

    private Javalin server;
    private Places places;

    public PlaceNameService(){
    }

    public void start() {
        start(DEFAULT_SERVICE_PORT);
    }

    public void start(int port) {
        if (server != null) {
            server.start(port);
        }
    }

    public void stop(){
        if (server != null) {
            server.stop();
        }
    }

    @VisibleForTesting
    PlaceNameService initialise(){
        places = initPlacesDb();
        server = initHttpServer();
        return this;
    }

    @Override
    public void run(){
        loadData();
        start();
    }

    /**
     * Loads the CSV data if a file was specified via CLI options.
     * Separated from run() for better testing.
     */
    public void loadData() {
        if (placesFile != null) {
            try {
                this.places = new PlacesCsvParser().parseCsvSource(placesFile);
                System.out.println("Loaded " + places.size() + " places from " + placesFile.getName());
            } catch (IOException e) {
                System.err.println("Failed to read CSV file: " + e.getMessage());
            }
        } else {
            System.out.println("No CSV file provided via -p or -f. Database is empty.");
        }
    }

    private Places initPlacesDb(){
        return new PlacesDb();
    }

    private Javalin initHttpServer(){
        Javalin app = Javalin.create();
        app.get("/provinces", ctx -> ctx.json(this.places.provinces()));
        app.get("/towns/{province}", ctx -> {
            String provinceName = ctx.pathParam("province");
            ctx.json(this.places.townsIn(provinceName));
        });
        return app;
    }
}
You need to run 4 separate terminals simultaneously. Each terminal will run one specific microservice.

Terminal 1: The Database (PlaceName Service)

```bash
mvn -f places/pom.xml exec:java -Dexec.mainClass="wethinkcode.places.PlaceNameService" -Dexec.args="-f places/resources/PlaceNamesZA2008.csv"
```

Terminal 2: The Status (Stage Service)
This service holds the current Load Shedding stage (0-8).

```bash
mvn -f stage/pom.xml exec:java -Dexec.mainClass="wethinkcode.stage.StageService"
```

Terminal 3: The Brain (Schedule Service)
This service calculates the outage times based on the stage and town.

```bash
mvn -f schedule/pom.xml exec:java -Dexec.mainClass="wethinkcode.schedule.ScheduleService"
```

Terminal 4: The Face (Web Service)
This service talks to the other three and shows you the HTML page.

```bash
mvn -f mvn -f web/pom.xml exec:java -Dexec.mainClass="wethinkcode.web.WebService"web/pom.xml exec:java -Dexec.mainClass="wethinkcode.web.WebService"
```
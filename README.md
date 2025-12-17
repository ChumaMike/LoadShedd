# Place Name Service

A lightweight Microservice that provides South African location data (Provinces and Towns) via a REST API. This service loads data from a CSV file into memory upon startup.

## Prerequisites

* Java 17+ (or JDK specific to your curriculum version)
* Maven 3.6+

## Project Structure

* `places/src/main/resources`: Contains the source data (`PlaceNamesZA2008.csv`).
* `places/src/main/java`: Source code.
* `places/src/test/java`: Unit and Integration tests.

## How to Build

Navigate to the project root and run:

```bash
mvn clean install

How to Run
You can run the service using the Maven exec plugin

Start the Service: Run this command from your project root (LoadShedd). It tells Maven to run the app and pass the CSV file path.


mvn -f places/pom.xml exec:java -Dexec.mainClass="wethinkcode.places.PlaceNameService" -Dexec.args="-f places/resources/PlaceNamesZA2008.csv"
Test It (in a new terminal):

You should see a JSON list of towns.

Once the service is running, you can interact with it using curl or a browser.


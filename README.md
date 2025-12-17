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
You can run the service using the Maven exec plugin or by running the generated JAR file.

Option 1: Via Maven:
cd places
mvn exec:java -Dexec.mainClass="wethinkcode.places.PlaceNameService"

Option 2: Via JAR:
java -jar places/target/places-1.0-SNAPSHOT.jar

Once the service is running, you can interact with it using curl or a browser.
# Stage Service

A simple Microservice that maintains the current state of Load Shedding (Stage 0 to 8).

## Prerequisites
* Java 17+
* Maven 3.6+

## How to Run
Navigate to the project root and run:

```bash
mvn -f stage/pom.xml exec:java -Dexec.mainClass="wethinkcode.stage.StageService"
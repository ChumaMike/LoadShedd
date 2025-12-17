# Stage Service

A simple Microservice that maintains the current state of Load Shedding (Stage 0 to 8).

## Prerequisites
* Java 17+
* Maven 3.6+

## How to Run
Navigate to the project root and run:

```bash

mvn -f stage/pom.xml exec:java

```

Interact with it (New Terminal) Open a second terminal window to act as the "Client".

Check the Stage:

```Bash

curl -v http://localhost:7001/stage
```

Change the Stage (to Stage 4):

```Bash

curl -v -X POST -d '{"stage": 4}' http://localhost:7001/stage
```


# Schedule Service

The calculation engine for the Load Shedding system. It generates a 4-day outage schedule for a given town.

## Prerequisites
* Java 17+
* Maven 3.6+

## How to Run

```bash
mvn -f schedule/pom.xml exec:java -Dexec.mainClass="wethinkcode.schedule.ScheduleService"
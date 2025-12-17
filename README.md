# Load Shedding Distributed System

A Microservices-based distributed system that simulates a Load Shedding management application. It consists of four distinct services communicating via REST APIs and Asynchronous Messaging (ActiveMQ).

## 🏗 System Architecture

The system is composed of four isolated services:

1.  **Places Service (Port 7000):** * **Role:** The Database / Address Book.
    * **Function:** Parses a CSV dataset of 5000+ South African towns and provides search capabilities via REST.
2.  **Stage Service (Port 7001):**
    * **Role:** State Management.
    * **Function:** Stores the current Load Shedding stage (0-8). Acts as a **Publisher** to notify other services of stage changes via ActiveMQ.
3.  **Schedule Service (Port 7002):**
    * **Role:** The Calculation Engine.
    * **Function:** Accepts a town and stage, and calculates the specific outage time slots.
4.  **Web Service (Port 7003):**
    * **Role:** The User Interface.
    * **Function:** Aggregates data from all services and renders the HTML frontend. Listens to ActiveMQ to update the stage in real-time.

---

## 🚀 Prerequisites

* **Java 17+**
* **Maven 3.6+**
* **ActiveMQ** (Running on `tcp://localhost:61616`)
* *Docker command:* `docker run -p 61616:61616 -p 8161:8161 rmohr/activemq`

---

## ⚡ How to Run

You need to run each service in its own terminal window. Run them in this specific order:

### 1. Start the ActiveMQ Broker
Ensure your message broker is running.
*(If installed locally, it usually runs in the background. If using Docker, ensure the container is up.)*

### 2. Start the Places Service

```bash
mvn -f places/pom.xml clean compile exec:java -Dexec.mainClass="wethinkcode.places.PlaceNameService" -Dexec.args="-f places/resources/PlaceNamesZA2008.csv"
```

### 3. Start the Stage Service

```bash
mvn -f stage/pom.xml clean compile exec:java -Dexec.mainClass="wethinkcode.stage.StageService"
```

### 4. Start the Schedule Service

```Bash
mvn -f schedule/pom.xml clean compile exec:java -Dexec.mainClass="wethinkcode.schedule.ScheduleService"
```

### 5. Start the Web Service

```Bash
mvn -f web/pom.xml clean compile exec:java -Dexec.mainClass="wethinkcode.web.WebService"
```

Usage
Open your browser to: http://localhost:7003

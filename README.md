# Web Service

The user-facing web server for the Load Shedding system. It aggregates data from the backend services and renders HTML pages.

## Prerequisites
* Java 17+
* Maven 3.6+
* Backend Services Running:
    * PlaceName Service (Port 7000)
    * Stage Service (Port 7001)
    * Schedule Service (Port 7002)

## How to Run

1. **Ensure all backend services are running** in separate terminals.
2. **Start the Web Service:**
   ```bash
   mvn -f web/pom.xml exec:java -Dexec.mainClass="wethinkcode.web.WebService"
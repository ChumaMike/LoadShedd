## 🧩 System Components

### 1️⃣ Stage Service

**Responsibility:**

* Stores and serves the current load-shedding stage

**Endpoints:**

* `GET /stage` – Returns the current stage
* `POST /stage` – Updates the stage

**Messaging (Exercise 4):**

* Publishes **JSON-formatted messages** to a JMS **Topic** whenever the stage changes

**Fault Handling:**

* Returns HTTP 500 on internal failure
* Handles broker unavailability without crashing

---

### 2️⃣ Schedule Service

**Responsibility:**

* Provides load-shedding schedules based on location

**Endpoints:**

* `GET /{province}/{place}` – Returns a schedule using the *internally stored stage*

**Messaging (Exercise 5):**

* Subscribes to the stage **Topic**
* Maintains its own local copy of the current stage

**Key Design Rule:**

* Does **not** require the stage to be passed as a request parameter

---

### 3️⃣ Web Service

**Responsibility:**

* Acts as the front-end entry point
* Coordinates data from StageService and ScheduleService

**Architecture Change:**

* No longer polls StageService
* Subscribes to the stage **Topic** and tracks stage locally

**Resilience:**

* Handles HTTP failures from downstream services
* Continues running if MQ broker is temporarily offline

---

## 📨 Messaging Infrastructure

* **Broker:** ActiveMQ 6 Classic
* **Protocol:** JMS
* **Pattern Used:** Publish / Subscribe
* **Destination Type:** Topic (NOT Queue)

### Why Topics?

Stage changes are **broadcast events**. All interested services (Web & Schedule) must receive updates simultaneously. Using a Queue would cause state desynchronization.

---

## ⚙️ Technology Stack

* Java
* Javalin (HTTP server)
* JMS (Java Message Service)
* ActiveMQ 6 Classic
* Maven

---

## 🚀 How to Run the System

### 1️⃣ Start ActiveMQ

```bash
docker run -p 61616:61616 -p 8161:8161 rmohr/activemq
```

Web Console: [http://localhost:8161](http://localhost:8161)

---

### 2️⃣ Start Services (in order)

```bash
# Stage Service
mvn exec:java

# Schedule Service
mvn exec:java

# Web Service
mvn exec:java
```

Each service runs independently and communicates via HTTP and JMS.

---

## 🛡️ Defensive Coding Principles

This project explicitly follows:

* **Precision over speed** – exact endpoints, JSON keys, ports
* **Graceful failure handling** – no crashes on ConnectionRefused or broker downtime
* **State isolation** – each service maintains its own internal state
* **No magic dependencies** – only specified versions and defaults

---

## ❌ Common Failure Points Avoided

* ❌ Using Artemis instead of ActiveMQ Classic
* ❌ Sending plain text instead of JSON messages
* ❌ Using Queues instead of Topics
* ❌ Incorrect endpoint casing or paths
* ❌ Crashing when a dependent service is offline

---

## ✅ Curriculum Alignment

This project aligns directly with:

* Interprocess Communication (Part 1 & 2)
* Fault Handling & Resilience
* Message-Oriented Middleware
* Asynchronous System Design

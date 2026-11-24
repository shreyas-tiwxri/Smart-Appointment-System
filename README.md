# Doctor Smart Appointment & Clinical Workflow System

A modular healthcare coordination system developed using **100% Core Java (JDK 17+)** with zero external libraries or frameworks. Developed as an academic project for **UCS553: Enterprise Web Application / Core Java** at **Thapar Institute of Engineering & Technology, Patiala**.

- **Live Project Web Portal:** [https://shreyas-tiwxri.github.io/Smart-Appointment-System/](https://shreyas-tiwxri.github.io/Smart-Appointment-System/)
- **Local Micro-Server:** `http://localhost:8080/` (built-in Java HTTP server)
- **Standalone Runnable:** `doctor-smart-system.jar` (Includes Desktop GUI, CLI, and Web Server)
- **Detailed Academic Report:** [docs/MINI_PROJECT_REPORT.md](docs/MINI_PROJECT_REPORT.md)

---

## Academic & Team Details

- **Course:** UCS553 - Enterprise Web Application / Core Java
- **Under the Guidance of:** Dr. Jayendra Barua, Assistant Professor, Computer Science & Engineering Department
- **Institution:** Thapar Institute of Engineering & Technology, Patiala
- **Batch:** 3C41

### Team Members & Division of Work

| Student Name | Roll Number | Primary Modules & Responsibilities |
|---|---|---|
| **Mohit Srivastav** | `1024030368` | Patient Portal CLI (`PatientMenu.java`), Patient domain model (`Patient.java`), Web UI prototype (`web/`), input validation & date parsing (`InputHelper.java`). |
| **Shreyas Tiwari** | `1024030384` | Doctor Clinical Workspace (`DoctorMenu.java`), Slot Collision Guard (30-min buffer), Triage Priority Queue (`TriageQueueService.java`), Consultation & auto follow-up booking, Invoicing & GST (`Invoice.java`), File persistence (`FilePersistenceService.java`), Core Java HTTP Server (`SimpleHttpServer.java`). |
| **Collaborative** | Both Members | Domain abstraction (`Person.java`), DAO abstraction (`IDataRepository.java`), Core Java Swing Desktop GUI (`MainWindow.java`), 21-scenario test suite (`SystemWorkflowTest.java`), documentation. |

---

## Key Features

1. **Patient Booking & Slot Collision Prevention:**
   - Patients can view available doctors and schedule appointments.
   - Built-in scheduling guard enforces a **30-minute buffer** between appointments to prevent double-booking.
2. **Emergency Triage Queue (`TriageQueueService`):**
   - Implements a priority queue (`PriorityQueue<Appointment>`) with a custom `Comparator` to process emergency and urgent patients ahead of routine appointments.
3. **Doctor Workspace & Decisioning:**
   - Doctors review pending queues to approve or reject visits (with mandatory clinical reasons for rejections).
   - Real-time clinical stats: total visits, approval rate percentage, and completed consultations.
4. **Clinical Consultation & Automatic Follow-Up:**
   - Doctors record clinical diagnoses and medication instructions.
   - When a review date is specified, the system automatically schedules the next follow-up appointment with `APPROVED` status.
5. **Medical Invoicing with GST:**
   - Automatically computes consultation fees + 5% healthcare GST and writes itemized receipts to `records/invoices/`.
6. **Prescription Slips & Persistence:**
   - Formats and exports printable clinical prescription notes to `records/prescriptions/`.
   - Saves database diagnostic snapshots to disk with a JVM shutdown hook to prevent accidental data loss.
7. **Observer Pattern Real-Time Notifications:**
   - `NotificationService` publishes event alerts for bookings, approvals, rejections, cancellations, and consultations.
8. **Multiple Interfaces in One Package:**
   - **Interactive Console CLI** with formatted menus.
   - **Desktop GUI** built with 100% Core Java Swing (`MainWindow.java`).
   - **Embedded Local Web Server** (`SimpleHttpServer.java`) serving REST APIs and the web dashboard on port `8080`.
   - **GitHub Pages Portal** hosted at `https://shreyas-tiwxri.github.io/Smart-Appointment-System/`.

---

## Project Structure

```
doctor-smart-appointment-system/
├── src/
│   ├── Main.java                        # Application entry point, CLI routing & JVM shutdown hook
│   ├── DemoRunner.java                  # 10-stage automated walkthrough for project evaluation
│   ├── SystemWorkflowTest.java          # 21-scenario test suite (runs without JUnit)
│   ├── model/                           # Domain Entities & DTOs
│   │   ├── Person.java                  # Abstract base class (encapsulation & polymorphism)
│   │   ├── Patient.java                 # Patient entity extending Person
│   │   ├── Doctor.java                  # Doctor entity extending Person
│   │   ├── Appointment.java             # Appointment entity with state and priority
│   │   ├── AppointmentStatus.java       # Enum: PENDING, APPROVED, REJECTED, CANCELLED
│   │   ├── TriagePriority.java          # Enum: EMERGENCY, URGENT, ROUTINE
│   │   ├── Consultation.java            # Clinical notes, diagnosis, and prescription details
│   │   ├── Invoice.java                 # Billing computation with 5% healthcare GST
│   │   ├── NotificationEvent.java       # Observer alert data model
│   │   └── DoctorStats.java             # DTO aggregating practitioner analytics
│   ├── repository/                      # Data Access & Storage
│   │   ├── IDataRepository.java         # Repository interface (Dependency Inversion Principle)
│   │   ├── InMemoryRepository.java      # Thread-safe repository (ConcurrentHashMap, AtomicInteger)
│   │   └── FilePersistenceService.java  # File I/O snapshot exporter and audit logger
│   ├── server/                          # Embedded Networking
│   │   └── SimpleHttpServer.java        # Standard library HTTP server & REST API (:8080)
│   ├── service/                         # Business Logic Layer
│   │   ├── AppointmentService.java      # Slot collision guard, booking, and doctor workload stats
│   │   ├── TriageQueueService.java      # PriorityQueue triage ordering
│   │   ├── ConsultationService.java     # Diagnosis, auto follow-up, and invoicing
│   │   └── NotificationService.java     # Event publisher (Observer Pattern)
│   ├── ui/                              # User Interfaces
│   │   ├── MainWindow.java              # Core Java Swing Desktop Application
│   │   ├── PatientMenu.java             # Terminal menu for patient workflows
│   │   ├── DoctorMenu.java              # Terminal menu for doctor workflows
│   │   └── InputHelper.java             # Input validation, sanitization, and date parsing
│   ├── util/                            # Shared Utilities
│   │   ├── Config.java                  # System constants (slot buffer, GST rate, formats)
│   │   └── Logger.java                  # Timestamped terminal logger
│   └── exception/                       # Custom Exception Hierarchy
│       ├── InvalidInputException.java   # Bad input or past date validation
│       ├── InvalidStateException.java   # State transition and slot collision errors
│       └── NotFoundException.java       # Missing doctor/patient identifier errors
├── web/                                 # Static web dashboard & client assets
│   ├── index.html                       # Web presentation dashboard
│   ├── css/style.css                    # Styling
│   └── js/app.js                        # Client-side data interaction
├── docs/                                # Project Documentation
│   ├── MINI_PROJECT_REPORT.md           # Formal college mini-project report
│   ├── TEACHER_PROGRESS_REPORT.md       # Milestone progress and architecture details
│   ├── VIVA_PREPARATION_GUIDE.md        # Viva defense questions and answers
│   └── PRESENTATION_SLIDES.html         # HTML presentation slide deck
├── records/                             # Generated runtime output
│   ├── prescriptions/                   # Exported prescription slips (.txt)
│   ├── invoices/                        # Exported medical receipts (.txt)
│   └── database_snapshot.txt            # System snapshot
├── index.html                           # Root redirect for GitHub Pages deployment
├── build.bat                            # 1-Click build & test script for Windows
├── build.sh                             # 1-Click build & test script for Linux/macOS
├── doctor-smart-system.jar              # Runnable standalone executable JAR
└── README.md
```

---

## How to Build and Run

### Prerequisites
- Java Development Kit: **JDK 17 or higher** (Tested on JDK 17, 21, and 26)
- No build tools (Maven, Gradle) needed. Standard `javac` and `java` are used.

---

### Method 1: One-Click Build Script

- **Windows:**
  ```cmd
  build.bat
  ```
- **Linux / macOS:**
  ```bash
  chmod +x build.sh
  ./build.sh
  ```
*This compiles all source files, runs all 21 verification tests, and packages `doctor-smart-system.jar`.*

---

### Method 2: Running the Standalone Executable JAR

- **Desktop GUI Mode (Default):**
  Double-click `doctor-smart-system.jar` in file explorer, or run:
  ```bash
  java -jar doctor-smart-system.jar --gui
  ```
- **Interactive Console CLI:**
  ```bash
  java -jar doctor-smart-system.jar
  ```
- **Local Web Server (Port 8080):**
  ```bash
  java -jar doctor-smart-system.jar --web
  ```
  *Then open `http://localhost:8080/` in your browser.*
- **Automated 10-Stage Evaluation Walkthrough:**
  ```bash
  java -jar doctor-smart-system.jar --demo
  ```

---

### Method 3: Manual Compilation & Direct Execution

```bash
# Compile all source files into the 'out' directory:
javac -d out src/exception/*.java src/model/*.java src/repository/*.java src/service/*.java src/server/*.java src/ui/*.java src/util/*.java src/*.java

# Run interactive console:
java -cp out Main

# Run Desktop Swing GUI:
java -cp out Main --gui

# Run embedded local web server:
java -cp out Main --web

# Run 21-scenario verification test suite:
java -cp out SystemWorkflowTest

# Run 10-stage evaluation demo:
java -cp out Main --demo
```

---

## Automated Test Suite (21 / 21 Tests Passing)

All tests are implemented in Core Java inside `src/SystemWorkflowTest.java` without relying on JUnit:

| Test ID | Scenario Tested | Checked Invariant | Status |
|:---:|---|---|:---:|
| **TC-01** | Appointment Creation | Initial status is `PENDING`, `isFollowUp == false` | PASS |
| **TC-02** | Past Date Scheduling | Rejects past timestamp via `InvalidInputException` | PASS |
| **TC-03** | Doctor Approval | Updates status to `APPROVED` | PASS |
| **TC-04** | Doctor Rejection | Updates status to `REJECTED` | PASS |
| **TC-05** | Decision Locking | Re-deciding an already approved/rejected visit throws `InvalidStateException` | PASS |
| **TC-06** | Consultation & Follow-up | Creates consultation note and auto-books follow-up with status `APPROVED` | PASS |
| **TC-07** | Consultation Guard | Consultation blocked for non-`APPROVED` visits | PASS |
| **TC-08** | Duplicate Consultation | Re-recording consultation on same visit throws `InvalidStateException` | PASS |
| **TC-09** | Follow-Up Date Sequence | Next-visit date before current appointment is rejected | PASS |
| **TC-10** | Data Isolation | Accessing invalid or unmapped patient/doctor IDs throws `NotFoundException` | PASS |
| **TC-11** | Doctor Workload Analytics | Accurate calculation of total visits, approval %, and completed consults | PASS |
| **TC-12** | Doctor Record Isolation | Restricts consultation query results to the attending doctor | PASS |
| **TC-13** | Rejection Reason Audit | Captures and persists clinical justification for rejected visits | PASS |
| **TC-14** | Polymorphism Verification | Polymorphic dispatch on `Person` methods (`getRole()`, `getSummaryDetails()`) | PASS |
| **TC-15** | Prescription Slip Export | Formats and saves readable prescription file to `records/prescriptions/` | PASS |
| **TC-16** | Thread Safety & Atomic IDs | Concurrent appointment creation with zero duplicate IDs | PASS |
| **TC-17** | 30-Minute Slot Buffer | Prevents booking when doctor has overlapping appointment within 30 mins | PASS |
| **TC-18** | Patient Cancellation | Transitions status to `CANCELLED` and logs cancellation reason | PASS |
| **TC-19** | Appointment Rescheduling | Modifies appointment time and validates against schedule collisions | PASS |
| **TC-20** | Invoicing & GST Calculation | Computes consultation fee + 5% healthcare GST, exports to `records/invoices/` | PASS |
| **TC-21** | Triage Priority Queue | `PriorityQueue` processes `EMERGENCY` appointments before `ROUTINE` | PASS |

---

## Core Java Concepts Implemented

- **Object-Oriented Programming (OOP):**
  - **Abstraction & Inheritance:** Abstract base class `Person` extended by `Patient` and `Doctor`.
  - **Polymorphism:** Method overriding in `getRole()` and dynamic method dispatch.
  - **Encapsulation:** Strongly typed entities with private fields, validation rules, and immutability where appropriate.
- **Java Collections Framework:**
  - `PriorityQueue<Appointment>` with custom `Comparator` for patient triage.
  - `ConcurrentHashMap<Integer, T>` for thread-safe repository lookups.
  - `ArrayList<T>` and sorted chronological streams for appointment histories.
- **Concurrency & Thread Safety:**
  - `AtomicInteger` counters for lock-free, collision-free unique ID generation.
  - Thread-safe repository operations preventing race conditions.
- **File I/O & Serialization:**
  - `BufferedWriter`, `BufferedReader`, `File` operations for persistent audit logs, prescription slips, and database snapshots.
  - `Runtime.getRuntime().addShutdownHook(...)` ensuring clean snapshotting on JVM shutdown.
- **Built-in HTTP Networking (`jdk.httpserver`):**
  - Standard library `com.sun.net.httpserver.HttpServer` serving REST API endpoints without third-party web frameworks.
- **Custom Exception Handling:**
  - Exception hierarchy extending `RuntimeException` (`InvalidInputException`, `InvalidStateException`, `NotFoundException`).
- **GUI Programming:**
  - Multi-tab graphical interface built with standard Java Swing (`javax.swing.*`, `java.awt.*`).

---

## Evaluation Documents

- **[Formal Mini-Project Report](docs/MINI_PROJECT_REPORT.md)**: Formatted according to academic project submission standards.
- **[Teacher Progress Report](docs/TEACHER_PROGRESS_REPORT.md)**: Weekly milestones, component breakdown, and verification results.
- **[Viva Preparation & Defense Guide](docs/VIVA_PREPARATION_GUIDE.md)**: Expected evaluation questions, technical justifications, and code explanations.
- **[Presentation Slide Deck](docs/PRESENTATION_SLIDES.html)**: Interactive presentation slides for project demonstration.

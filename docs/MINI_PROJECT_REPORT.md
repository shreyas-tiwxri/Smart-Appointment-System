# UCS553: Enterprise Web Application
## Mini Project Report

---

# DOCTOR SMART APPOINTMENT & CLINICAL WORKFLOW SYSTEM

**Under the Guidance of:**  
**Dr. Jayendra Barua**  
Assistant Professor  
Computer Science & Engineering Department  
Thapar Institute of Engineering & Technology, Patiala  

---

### Submitted By:
| Roll Number | Student Name | Group / Sub-Group |
|---|---|---|
| 1024030368 | **Mohit Srivastav** | 3C41 |
| 1024030384 | **Shreyas Tiwari** | 3C41 |

**Computer Science and Engineering Department**  
**Thapar Institute of Engineering & Technology, Patiala** 

*In Partial Fulfilment of the Requirements for the Degree of*  
**Bachelor of Engineering in Computer Engineering**  

**November 2026**

---

<div style="page-break-after: always;"></div>

## Abstract

In modern outpatient healthcare facilities, manual appointment scheduling and fragmented clinical record management lead to operational inefficiencies such as practitioner double-booking, clinical consultation delays, uncoordinated patient follow-ups, and manual billing errors. The **Doctor Smart Appointment & Clinical Workflow System** is an enterprise-grade Core Java desktop and console coordination application developed to streamline the complete outpatient consultation lifecycle. 

The system implements a decoupled, 4-tier layered architecture adhering strictly to Object-Oriented Programming (OOP) and SOLID principles without relying on third-party frameworks or external libraries. Key architectural features include:
1. An **Abstract Domain Model** with polymorphic behavior (`Person`, `Patient`, `Doctor`).
2. An **Interface-Driven Data Access Layer** (`IDataRepository`, `InMemoryRepository`) leveraging thread-safe data structures (`ConcurrentHashMap`, `AtomicInteger`) to prevent race conditions during concurrent bookings.
3. A **Clinical Collision Prevention Guard** that enforces an automated 30-minute buffer around practitioner schedules to eliminate overlapping appointments.
4. An **Automated Clinical Consultation & Follow-up Engine** that records diagnosis and prescriptions, immediately registers a pre-approved follow-up visit based on the doctor's recommended review timeline, issues an official itemized medical invoice with Healthcare GST, exports digital prescription discharge slips with verification hashes to disk, and dispatches decoupled real-time alerts via an Observer notification engine.
5. A **Multi-Tab Desktop GUI** developed using 100% Core Java Swing (`javax.swing.*`, `java.awt.*`) providing interactive visual workflows alongside the interactive command-line interface.
6. A **Graceful JVM Shutdown Hook** ensuring zero-loss state snapshotting to disk upon program termination.

The system was verified against a custom-engineered 21-scenario automated test suite (`SystemWorkflowTest.java`) evaluating unit behavior, domain business rules, slot conflict constraints, and multi-threaded race conditions, achieving a 100% pass rate.

---

<div style="page-break-after: always;"></div>

## Table of Contents

- **Abstract**
- **Chapter 1: Introduction**
  - 1.1 Introduction to the Project
  - 1.2 Problem Statement
  - 1.3 Objectives of the Project
  - 1.4 Scope of the Project
- **Chapter 2: System Design**
  - 2.1 System Architecture
  2.2 Use Case Diagram & Actor Roles
  - 2.3 Class Diagram & Domain Modeling
  - 2.4 Activity Diagram (Clinical Lifecycle)
  - 2.5 In-Memory Data Structure & Storage Design
  - 2.6 Module Descriptions
- **Chapter 3: Implementation**
  - 3.1 Technologies and Tools Used
  - 3.2 Project Directory & Package Structure
  - 3.3 Description of Classes and Interfaces
  - 3.4 Implementation of Major Modules
  - 3.5 Custom Exception Handling Hierarchy
  - 3.6 Java Collection Framework & Concurrency Usage
  - 3.7 Data Persistence & File I/O Management
  - 3.8 Important Code Snippets
- **Chapter 4: Results and Output Demonstrations**
  - 4.1 Main Application Interfaces (CLI & Core Java Swing GUI)
  - 4.2 Functional Workflow Verification
  - 4.3 Automated 10-Stage Evaluation Walkthrough Output
  - 4.4 Clinical Business Rule & Error Handling Validation
  - 4.5 Automated 21-Scenario Test Suite Execution
- **References**

---

<div style="page-break-after: always;"></div>

# Chapter 1: Introduction

### 1.1 Introduction to the Project
Healthcare facilities, polyclinics, and private outpatient practices face daily coordination challenges between front-desk receptionists, consulting physicians, and visiting patients. Traditional appointment booking methods—ranging from paper registers to disconnected spreadsheets—suffer from human scheduling oversights, duplicate slot assignments, untracked patient cancellations, and lost consultation notes. 

The **Doctor Smart Appointment & Clinical Workflow System** was designed and implemented as an end-to-end clinical workflow management tool built purely in **Core Java (Java SE 17+)**. The software bridges the coordination gap between patients seeking medical attention and practitioners managing active patient queues. Rather than acting as a simple booking diary, the system manages the entire clinical consultation pipeline: from request submission, schedule conflict detection, doctor decisioning, diagnosis and prescription generation, automated follow-up appointment booking, and healthcare billing, to local disk record persistence.

### 1.2 Problem Statement
Small-to-medium healthcare clinics face four major operational bottlenecks:
1. **Schedule Collisions & Double-Booking:** When multiple patients request appointments for the same practitioner without slot validation, schedules overlap, resulting in extended patient waiting room times and practitioner fatigue.
2. **Disconnected Follow-Up Scheduling:** When a doctor advises a patient to return in two weeks for review, the patient often fails to book the appointment at the front desk, or the desired slot becomes unavailable, breaking treatment continuity.
3. **Manual Billing & Invoicing Errors:** Calculating practitioner consultation fees, applying municipal or healthcare service goods and services tax (GST), and generating patient receipts manually introduces arithmetic discrepancies and administrative overhead.
4. **Data Loss During Unplanned Shutdowns:** In-memory applications risk total state loss if a power failure or abrupt application exit occurs before records are manually committed.

### 1.3 Objectives of the Project
The primary engineering objectives of this project are:
- **Zero Third-Party Dependencies:** Implement all clinical models, business logic, user interfaces, testing frameworks, and file persistence using standard Java Development Kit (JDK 17+) libraries.
- **Enforce Clinical Slot Buffers:** Develop an automated collision detection algorithm (`checkSlotAvailability`) enforcing a strict 30-minute buffer around practitioner appointments.
- **Automate Follow-Up Coordination:** Upon recording a completed clinical consultation, automatically generate a follow-up appointment with pre-approved status (`APPROVED`) linked to the attending doctor and patient.
- **Automated Medical Billing & Invoicing:** Calculate doctor consultation fees, apply a 5% healthcare service tax, and generate itemized billing slips saved to disk.
- **Observer Pattern Alerts:** Implement a decoupled event-driven notification engine that alerts patients and doctors upon request creation, approval, cancellation, and consultation completion.
- **Thread-Safe Concurrent Architecture:** Employ `ConcurrentHashMap`, `AtomicInteger`, and thread synchronization to ensure data integrity under simulated simultaneous multi-user interactions.
- **Dual Presentation Layer:** Provide an interactive command-line interface (CLI) with ANSI color logging alongside a full-featured multi-tab desktop graphical user interface (GUI) developed in 100% Core Java Swing.
- **100% Automated Test Coverage:** Write and verify 20 independent unit, integration, collision, and concurrency test scenarios without JUnit or external test runners.

### 1.4 Scope of the Project
The scope of this project encompasses:
- Outpatient appointment lifecycle management: `PENDING`, `APPROVED`, `REJECTED`, and `CANCELLED`.
- Dual practitioner and patient authentication portals.
- Doctor workload analytics: total assigned visits, approval ratios, completed consultations, and follow-up ratios.
- Local filesystem persistence: archiving state snapshots, formatted medical prescription slips (`records/prescriptions/`), and itemized billing invoices (`records/invoices/`).
- Automated shutdown safety via JVM shutdown hooks.

*Out of Scope:* Direct integration with hospital pharmacy hardware dispensing units, external online payment gateways (e.g., Stripe/Razorpay API calls requiring external HTTP SDKs), and multi-hospital distributed database clustering, which require third-party frameworks outside the Core Java mandate.

---

<div style="page-break-after: always;"></div>

# Chapter 2: System Design

### 2.1 System Architecture
The application follows a **4-tier layered architectural pattern** based on the **Separation of Concerns (SoC)** and **Single Responsibility Principle (SRP)**.

```
+-------------------------------------------------------------------------+
|                        PRESENTATION LAYER                               |
|  +--------------------+   +-------------------+   +------------------+  |
|  | PatientMenu (CLI)  |   | DoctorMenu (CLI)  |   | MainWindow (GUI) |  |
|  +--------------------+   +-------------------+   +------------------+  |
|  | InputHelper (Sanitization, Date Parsing)   |   | DemoRunner       |  |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                       BUSINESS LOGIC LAYER                              |
|  +---------------------------+       +-------------------------------+  |
|  |    AppointmentService     |       |      ConsultationService      |  |
|  | - Request Appointment     |       | - Record Diagnosis & Rx       |  |
|  | - Slot Collision Guard    |       | - Auto Follow-up Booking      |  |
|  | - Approve / Reject Queue  |       | - Medical Billing Calculation |  |
|  | - Cancellation & Resched  |       +-------------------------------+  |
|  | - Doctor Workload Stats   |                      |                   |
|  +---------------------------+                      v                   |
|                                      +-------------------------------+  |
|                                      |      NotificationService      |  |
|                                      | (Observer Pub-Sub Alerts)     |  |
|                                      +-------------------------------+  |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                    DATA ACCESS & PERSISTENCE LAYER                      |
|  +-------------------------------------------------------------------+  |
|  |               IDataRepository (Interface Abstraction)             |  |
|  +-------------------------------------------------------------------+  |
|          |                                                    |         |
|          v                                                    v         |
|  +----------------------------+             +------------------------+  |
|  |     InMemoryRepository     |             | FilePersistenceService |  |
|  | - ConcurrentHashMap stores |             | - Snapshot Export      |  |
|  | - AtomicInteger sequences  |             | - Prescription Slips   |  |
|  | - Synchronized chron lists |             | - Invoices & Audit Log |  |
|  |                            |             | - JVM Shutdown Hook    |  |
|  +----------------------------+             +------------------------+  |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                          DOMAIN MODEL LAYER                             |
|  +--------------------+   +-------------------+   +------------------+  |
|  |   Person (Abstr)   |   |   Patient / Doctor|   |   Appointment    |  |
|  +--------------------+   +-------------------+   +------------------+  |
|  | Consultation / Rx  |   | Invoice (Billing) |   | NotificationEvent|  |
+-------------------------------------------------------------------------+
```

### 2.2 Use Case Diagram & Actor Roles
The system defines three distinct actors:
1. **Patient:** Requests appointment slots, views scheduled visits and statuses, reviews consultation diagnoses, cancels or reschedules upcoming visits, and reviews billing invoices.
2. **Doctor:** Reviews incoming pending appointment queues, approves visits or rejects them with documented clinical reasons, inspects clinical schedule rosters, conducts consultations, records diagnoses and prescriptions, reviews workload analytics, and issues invoices.
3. **Automated System Daemon:** Enforces 30-minute collision buffers, auto-schedules follow-up visits upon consultation completion, calculates healthcare GST, logs audit trails, and exports database snapshots upon shutdown.

```
                             USE CASE OVERVIEW
                             
     +----------+                                      +----------+
     |  Patient |                                      |  Doctor  |
     +----------+                                      +----------+
          |                                                 |
          +--> [Request Appointment Slot]                   |
          |           |                                     |
          |           +--<<include>>--> [Check 30m Buffer]  |
          |                                                 |
          +--> [Cancel / Reschedule Appt]                   |
          |                                                 |
          +--> [View Scheduled Appts]                       +--> [View Pending Queue]
          |                                                 |
          +--> [View Prescriptions & Slips]                 +--> [Approve / Reject Request]
          |                                                 |           |
          +--> [View Invoices & Receipts]                   |           +--<<audit>>-- [Record Reason]
                                                            |
                                                            +--> [Conduct Consultation]
                                                            |           |
                                                            |           +--<<auto>>-- [Schedule Follow-up]
                                                            |           +--<<auto>>-- [Generate Invoice]
                                                            |           +--<<auto>>-- [Export Rx Slip]
                                                            |
                                                            +--> [View Clinical Analytics]
```

### 2.3 Class Diagram & Domain Modeling

```
+-------------------------------------------------------------------------+
|                        <<abstract>> Person                              |
+-------------------------------------------------------------------------+
| - id: int                                                               |
| - name: String                                                          |
| - contact: String                                                       |
+-------------------------------------------------------------------------+
| + getId(): int                                                          |
| + getName(): String                                                     |
| + getContact(): String                                                  |
| + {abstract} getRole(): String                                          |
| + {abstract} getSummaryDetails(): String                                |
+-------------------------------------------------------------------------+
                    ^                                   ^
                    |                                   |
+-----------------------------------+   +---------------------------------+
|              Patient              |   |             Doctor              |
+-----------------------------------+   +---------------------------------+
| (inherits all Person fields)      |   | - specialty: String             |
|                                   |   | - consultationFee: double       |
+-----------------------------------+   +---------------------------------+
| + getPatientId(): int             |   | + getDoctorId(): int            |
| + getRole(): String               |   | + getSpecialty(): String        |
| + getSummaryDetails(): String     |   | + getConsultationFee(): double  |
+-----------------------------------+   | + getSummaryDetails(): String   |
                                        +---------------------------------+
                                                        ^
                                                        | references
+-------------------------------------------------------+-----------------+
|                               Appointment                               |
+-------------------------------------------------------------------------+
| - appointmentId: int                                                    |
| - patient: Patient                                                      |
| - doctor: Doctor                                                        |
| - dateTime: LocalDateTime                                               |
| - status: AppointmentStatus {PENDING, APPROVED, REJECTED, CANCELLED}    |
| - followUp: boolean                                                     |
| - rejectionReason: String                                               |
| - cancellationReason: String                                            |
+-------------------------------------------------------------------------+
| + updateDecision(status: AppointmentStatus, reason: String): void       |
| + cancel(reason: String): void                                          |
| + reschedule(newTime: LocalDateTime): void                              |
| + isFollowUp(): boolean                                                 |
+-------------------------------------------------------------------------+
                    ^
                    | 1..1
+-------------------------------------------------------------------------+
|                               Consultation                              |
+-------------------------------------------------------------------------+
| - consultationId: int                                                   |
| - appointment: Appointment                                              |
| - diagnosis: String                                                     |
| - prescription: String                                                  |
| - nextVisitDateTime: LocalDateTime                                      |
+-------------------------------------------------------------------------+
| + generatePrescriptionSlip(): String                                    |
| + generateDigitalHash(): String                                         |
+-------------------------------------------------------------------------+
                    ^
                    | 1..1
+-------------------------------------------------------------------------+
|                                 Invoice                                 |
+-------------------------------------------------------------------------+
| - invoiceId: String (INV-2026-XXXX)                                     |
| - consultation: Consultation                                            |
| - baseFee: double                                                       |
| - taxRate: double (5% Healthcare GST)                                   |
| - taxAmount: double                                                     |
| - totalAmount: double                                                   |
| - paymentMode: String                                                   |
+-------------------------------------------------------------------------+
| + generateReceipt(): String                                             |
+-------------------------------------------------------------------------+
```

### 2.4 Activity Diagram (Clinical Consultation Lifecycle)
The end-to-end appointment lifecycle follows a sequential state progression:

```
[Start: Patient]
       |
       v
(Select Practitioner & Slot)
       |
       v
<Is Slot within 30 min of existing active appt?>
      / \
    Yes   No
    /       \
   v         v
[Throw Slot   [Create Appointment with status PENDING]
 Conflict]           |
                     v
             (Doctor Reviews Queue)
                     |
                     v
             <Doctor Decision>
                  /     \
             Reject     Approve
              /             \
             v               v
   [Set REJECTED with    [Set status APPROVED]
    Clinical Reason]         |
                             v
                     (Patient Attends Visit)
                             |
                             v
                     [Doctor Records Diagnosis, Rx & Next Visit]
                             |
             +---------------+---------------+
             |                               |
             v                               v
    [Auto-Create Follow-Up]         [Generate Invoice with 5% GST]
    (Status: APPROVED,              (Base fee + Tax calculated)
     isFollowUp = true)                      |
             |                               v
             +---------------> [Export Prescription Slip to Disk]
                                     |
                                     v
                               [Dispatch Real-Time Alerts]
                                     |
                                     v
                                   [End]
```

### 2.5 In-Memory Data Structure & Storage Design
To avoid external database overhead while guaranteeing thread safety under multi-user concurrency, the storage architecture was designed using Java's `java.util.concurrent` primitives:

1. **`ConcurrentHashMap<Integer, T>`:** Used for `patients`, `doctors`, `appointments`, `consultations`, and `invoices`. Provides lock-striping for read/write operations without locking the entire map.
2. **Synchronized Insertion Lists:** Maintains insertion-ordered collections for deterministic tabular rendering in UI tables and disk snapshotting.
3. **`AtomicInteger` Sequence Counters:** Generates atomic IDs (`nextAppointmentId()`, `nextConsultationId()`, `nextInvoiceId()`) guaranteeing uniqueness without blocking threads.
4. **Zero-Dependency Flat File Snapshotting:** State is saved to `records/database_snapshot.txt`, prescription slips to `records/prescriptions/Rx_Patient<X>_Appt<Y>.txt`, and receipts to `records/invoices/INV-2026-<ID>_Patient<X>.txt`.

### 2.6 Module Descriptions
- **Patient Portal (`PatientMenu.java`):** Allows patients to choose a practitioner, submit an appointment request, view chronological statuses, review diagnosis notes, and cancel or reschedule existing appointments.
- **Doctor Workspace (`DoctorMenu.java`):** Provides practitioners with queue review, approval/rejection with mandatory audit reason logging, consultation entry, full schedule rosters, and workload analytics.
- **Appointment Lifecycle Service (`AppointmentService.java`):** Implements state validation guards, slot availability verification, cancellation logic, and doctor analytics aggregation (`DoctorStats`).
- **Consultation & Billing Engine (`ConsultationService.java`):** Validates that consultations occur only on approved visits, prevents duplicate consultation entries, coordinates auto-booking of follow-up visits, and creates medical invoices.
- **Desktop Swing GUI (`MainWindow.java`):** A multi-tab desktop application (`javax.swing.JFrame`) providing visual forms and tabular representations for non-CLI users.

---

<div style="page-break-after: always;"></div>

# Chapter 3: Implementation

### 3.1 Technologies and Tools Used
- **Programming Language:** Core Java (Java SE 17 / JDK 26-tested). Strictly standard libraries (`java.lang.*`, `java.util.*`, `java.time.*`, `java.io.*`, `javax.swing.*`, `java.awt.*`).
- **Operating Environment:** Windows 10/11 x64, PowerShell 7, CMD.
- **IDE & Development Tools:** Visual Studio Code (Java Extension Pack), Eclipse JDT Language Server.
- **Version Control:** Git, GitHub repository (`shreyas-tiwxri/Smart-Appointment-System`).
- **Build System:** Custom zero-dependency batch automation script (`build.bat`) invoking `javac -Xlint:all` directly.

### 3.2 Project Directory & Package Structure

```
doctor-smart-appointment-system/
│
├── build.bat                        # Automated compile & test verification script
├── config.properties                # Configuration file (buffers, GST rate, date format)
├── README.md                        # Project documentation and evaluation guide
├── .gitignore                       # Git ignore file for binaries and temp files
│
├── src/                             # Source code root
│   ├── Main.java                    # Entry point, CLI router & JVM shutdown hook
│   ├── DemoRunner.java              # 10-stage automated presentation demo runner
│   ├── SystemWorkflowTest.java      # 20-test automated verification suite
│   │
│   ├── exception/                   # Custom unchecked exception hierarchy
│   │   ├── InvalidInputException.java
│   │   ├── InvalidStateException.java
│   │   └── NotFoundException.java
│   │
│   ├── model/                       # Domain Entities & Data Transfer Objects
│   │   ├── Person.java              # Abstract base class
│   │   ├── Patient.java             # Concrete patient model
│   │   ├── Doctor.java              # Concrete doctor model
│   │   ├── Appointment.java         # Appointment state & transitions
│   │   ├── AppointmentStatus.java   # State enum: PENDING, APPROVED, REJECTED, CANCELLED
│   │   ├── Consultation.java        # Clinical notes, diagnosis & Rx generator
│   │   ├── Invoice.java             # Billing receipt & healthcare GST model
│   │   ├── NotificationEvent.java   # Event notification message DTO
│   │   └── DoctorStats.java         # Doctor analytics DTO
│   │
│   ├── repository/                  # Data access layer
│   │   ├── IDataRepository.java     # DAO interface abstraction
│   │   ├── InMemoryRepository.java  # Thread-safe ConcurrentHashMap store
│   │   └── FilePersistenceService.java # Flat-file export & snapshot engine
│   │
│   ├── service/                     # Business services
│   │   ├── AppointmentService.java  # Slot check, booking & doctor stats
│   │   ├── ConsultationService.java # Consultation recording & invoicing
│   │   └── NotificationService.java # Observer pattern event dispatcher
│   │
│   ├── ui/                          # Presentation interfaces
│   │   ├── MainWindow.java          # Core Java Swing multi-tab desktop GUI
│   │   ├── PatientMenu.java         # Patient console CLI
│   │   ├── DoctorMenu.java          # Doctor console CLI
│   │   └── InputHelper.java         # Input validation & date parser
│   │
│   └── util/                        # Utility & configuration helpers
│       ├── Config.java              # Central configuration reader
│       └── Logger.java              # ANSI color-coded diagnostic logger
│
├── docs/                            # Documentation & presentation slides
│   ├── MINI_PROJECT_REPORT.md       # Full project report (this document)
│   ├── TEACHER_PROGRESS_REPORT.md   # Weekly milestone progress report
│   ├── VIVA_PREPARATION_GUIDE.md    # Viva questions and defense answers
│   └── PRESENTATION_SLIDES.html     # Interactive browser presentation slides
│
├── records/                         # Exported persistence records
│   ├── database_snapshot.txt        # Full state archive
│   ├── system_audit_log.txt         # Chronological system audit log
│   ├── prescriptions/               # Generated Rx slips (e.g. Rx_Patient1_Appt1.txt)
│   └── invoices/                    # Generated billing receipts (e.g. INV-2026-1001.txt)
│
└── web/                             # Web simulator (HTML5/CSS3/JavaScript)
    ├── index.html                   # Interactive browser simulator
    ├── style.css                    # Responsive stylesheet
    └── app.js                       # Frontend state logic
```

### 3.3 Description of Classes and Interfaces

| Class / Interface | Package | Design Role & OOP Principles |
|---|---|---|
| `Person` | `model` | **Abstract Base Class:** Encapsulates common attributes (`id`, `name`, `contact`) and declares abstract methods `getRole()` and `getSummaryDetails()`. |
| `Patient` | `model` | **Inheritance & Encapsulation:** Extends `Person`. Represents patients seeking appointments. |
| `Doctor` | `model` | **Inheritance & Specialization:** Extends `Person`. Adds `specialty` and `consultationFee`. |
| `Appointment` | `model` | **Encapsulation & State Pattern:** Encapsulates scheduling state, rejection/cancellation reasons, and transition guards. |
| `AppointmentStatus` | `model` | **Enum Type:** Strongly typed statuses: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`. |
| `Consultation` | `model` | **Domain Entity:** Holds diagnosis, prescriptions, next-visit time, and generates formatted Rx slips with verification hashes. |
| `Invoice` | `model` | **Financial Entity:** Calculates specialist fee, 5% healthcare GST, and formats printable receipts. |
| `DoctorStats` | `model` | **Data Transfer Object (DTO):** Immutable record aggregating doctor performance and workload metrics. |
| `NotificationEvent` | `model` | **Observer Message DTO:** Captures recipient, event type, timestamp, and human-readable message. |
| `IDataRepository` | `repository` | **Interface Abstraction (DIP):** Declares data access methods, decoupling business services from storage mechanisms. |
| `InMemoryRepository` | `repository` | **Thread-Safe Data Access Object:** Implements `IDataRepository` using `ConcurrentHashMap` and `AtomicInteger`. |
| `FilePersistenceService` | `repository` | **Persistence & File I/O:** Exports database snapshots, prescription slips, and audit logs using `FileWriter` and `PrintWriter`. |
| `AppointmentService` | `service` | **Domain Service:** Manages request queues, cancels/reschedules visits, enforces 30-min slot buffer, and computes doctor stats. |
| `ConsultationService` | `service` | **Domain Service:** Validates consultations, coordinates auto follow-up scheduling, and generates invoices. |
| `NotificationService` | `service` | **Observer Pattern:** Thread-safe pub-sub event dispatcher alerting patients and doctors. |
| `MainWindow` | `ui` | **Desktop GUI:** Multi-tab visual application built with 100% Core Java Swing (`JFrame`, `JTable`, `JComboBox`). |
| `PatientMenu` & `DoctorMenu` | `ui` | **Console Presentation Layer:** Command-line interfaces for patients and practitioners. |
| `InputHelper` | `ui` | **Defensive Input Validation:** Parses dates, handles `NumberFormatException`, and wraps scanner operations. |
| `Config` & `Logger` | `util` | **Configuration & Diagnostics:** Reads properties file and outputs timestamped ANSI color logs. |

### 3.4 Implementation of Major Modules

#### 3.4.1 Slot Collision Prevention Algorithm
To prevent double-booking, `AppointmentService.checkSlotAvailability()` inspects all existing `PENDING` and `APPROVED` appointments for the requested doctor and computes the absolute minute difference between the requested time and existing bookings using `java.time.Duration.between()`. If the difference is under 30 minutes, an `InvalidStateException` is thrown:

```java
public void checkSlotAvailability(int doctorId, LocalDateTime dateTime, int excludeApptId) {
    Doctor d = repo.getDoctor(doctorId);
    for (Appointment existing : repo.getAllAppointments()) {
        if (existing.getAppointmentId() == excludeApptId) continue;
        if (existing.getDoctor().getDoctorId() != doctorId) continue;
        if (existing.getStatus() != AppointmentStatus.PENDING && 
            existing.getStatus() != AppointmentStatus.APPROVED) continue;

        long minuteDiff = Math.abs(Duration.between(existing.getDateTime(), dateTime).toMinutes());
        if (minuteDiff < Config.SLOT_BUFFER_MINUTES) {
            throw new InvalidStateException(String.format(
                "Slot Conflict: Dr. %s already has an active appointment (#%d at %s). " +
                "A %d-minute consultation buffer is required.",
                d.getName(), existing.getAppointmentId(), existing.getDateTime().format(FMT),
                Config.SLOT_BUFFER_MINUTES
            ));
        }
    }
}
```

#### 3.4.2 Automatic Follow-Up Booking Engine
During consultation recording, the doctor specifies a diagnosis, prescription, and recommended next visit date. The system automatically creates a follow-up appointment with status `APPROVED` directly, ensuring continuity of care without requiring front-desk re-approval:

```java
// Auto-create follow-up appointment (same patient + doctor)
Appointment followUp = new Appointment(repo.nextAppointmentId(), a.getPatient(), a.getDoctor(),
                                       nextVisit, AppointmentStatus.APPROVED, true);
repo.saveAppointment(followUp);
```

#### 3.4.3 Automated Medical Invoicing & Healthcare GST
Upon consultation completion, an `Invoice` is generated. It retrieves the doctor's base consultation fee, applies a 5% healthcare service tax, rounds the total, and saves an official receipt to disk:

```java
this.baseFee = consultation.getAppointment().getDoctor().getConsultationFee();
this.taxRate = 0.05; // 5% Healthcare Service GST
this.taxAmount = Math.round(this.baseFee * this.taxRate * 100.0) / 100.0;
this.totalAmount = this.baseFee + this.taxAmount;
```

### 3.5 Custom Exception Handling Hierarchy
The project uses custom unchecked exceptions extending `RuntimeException` to handle validation and business rule violations cleanly:
- **`InvalidInputException`:** Thrown when user inputs fail format or domain validation (e.g., appointment dates in the past, empty diagnosis strings, blank contact numbers).
- **`InvalidStateException`:** Thrown when an operation violates state transition rules (e.g., approving an already decided appointment, scheduling conflicting slots within 30 minutes, recording duplicate consultations).
- **`NotFoundException`:** Thrown when a lookup fails (e.g., patient or doctor ID does not exist in repository).

### 3.6 Java Collection Framework & Concurrency Usage
- **`ConcurrentHashMap<Integer, T>`:** Used in `InMemoryRepository` for all primary entity stores. Guarantees safe concurrent reads and thread-safe updates.
- **`Collections.synchronizedList(new ArrayList<>())`:** Used in `NotificationService` and `InMemoryRepository` to preserve chronological ordering during iteration.
- **`AtomicInteger`:** Used for thread-safe sequential primary key generation (`nextAppointmentId`, `nextConsultationId`, `nextInvoiceId`) without locking overhead.
- **Streams API:** Employed across services for filtering and aggregating collections (e.g., filtering appointments by patient ID, isolating approved visits, computing doctor approval percentages).

### 3.7 Data Persistence & File I/O Management
`FilePersistenceService` provides zero-dependency flat-file storage:
- **Prescription Slips:** Saved as formatted text files in `records/prescriptions/Rx_Patient<ID>_Appt<ID>.txt` containing patient details, diagnoses, medications, review dates, and verification hashes (`[SEC-MED-XXXXXXXX]`).
- **Billing Invoices:** Saved to `records/invoices/INV-2026-<ID>_Patient<ID>.txt`.
- **System Snapshot:** Full state export to `records/database_snapshot.txt`.
- **JVM Shutdown Hook:** Registered via `Runtime.getRuntime().addShutdownHook(...)` in `Main.java`, automatically writing a diagnostic state snapshot when the application terminates.

---

<div style="page-break-after: always;"></div>

# Chapter 4: Results and Output Demonstrations

### 4.1 Main Application Interfaces

#### 4.1.1 Interactive Console Selection Menu (`Main.java`)
```text
=================================================================
            DOCTOR SMART APPOINTMENT & CLINICAL SYSTEM           
          Developed by Mohit Srivastav & Shreyas Tiwari          
=================================================================

MAIN SELECTION MENU:
  1. Login as Patient (Member 1 Portal)
  2. Login as Doctor  (Member 2 Clinical Workspace)
  3. Run Automated Demo Walkthrough (--demo mode)
  4. Export System Diagnostic Snapshot to Disk
  5. Launch Desktop Clinical GUI (Swing Core Java)
  0. Exit
-----------------------------------------------------------------
Enter Choice: 
```

#### 4.1.2 100% Core Java Swing Desktop GUI (`MainWindow.java`)
Launched via `java -cp out Main --gui`:
- **Tab 1: Patient Appointment Portal:** Select patient from dropdown, select attending doctor, pick a date/time slot, click "Submit Appointment Request". Live JTable displays active bookings.
- **Tab 2: Doctor Clinical Workspace:** View schedule table, select appointment row, click "Approve", "Reject" (with reason dialog), "Cancel", or "Record Consultation".
- **Tab 3: Consultations & Invoices:** Tabular view of all completed visits, clinical diagnoses, and review dates.
- **Tab 4: System Audit & Log:** Real-time log area with buttons to export database snapshots and view active notification event counts.

### 4.2 Functional Workflow Verification

#### 4.2.1 Patient Booking & Doctor Approval
```text
  Patient 'Aman Sharma' submitted request for: 13-11-2026 10:30
  ✔ Created Appointment: Appointment #1 | Aman Sharma with Dr. Rajesh Sharma (Senior General Physician - Fee: ₹600) | 13-11-2026 10:30 | PENDING
    Status: PENDING (Awaiting doctor review)

  Doctor's pending queue count: 1
  ✔ Doctor action: APPROVED
    Updated State: Appointment #1 | Aman Sharma with Dr. Rajesh Sharma (Senior General Physician - Fee: ₹600) | 13-11-2026 10:30 | APPROVED
```

#### 4.2.2 Consultation Recording & Automatic Follow-Up
```text
  ✔ Consultation Record Created (ID #1):
    Diagnosis:    Acute Upper Respiratory Infection & Seasonal Asthma flare-up
    Prescription: 
                     1. Tab Amoxicillin 500mg TDS x 5 days
                     2. Tab Cetirizine 10mg OD x 7 days
                     3. Levolin inhaler 2 puffs PRN
    Next Visit:   27-11-2026 11:00

  ★ AUTOMATIC FOLLOW-UP APPOINTMENT CREATED BY SYSTEM ★
    ID:           #3
    Patient:      Aman Sharma
    Doctor:       Dr. Rajesh Sharma (Senior General Physician - Fee: ₹600)
    Scheduled At: 27-11-2026 11:00
    Status:       APPROVED (Directly APPROVED by Doctor)
    Is Follow-up: true
```

#### 4.2.3 Official Medical Billing Invoice Generation
```text
================================================================================
                       SMART CLINIC MEDICAL INVOICE & RECEIPT                   
================================================================================
Invoice Number  : INV-2026-1001             Date: 12-11-2026 14:04
Consultation Ref: #1                        Appointment Ref: #1
--------------------------------------------------------------------------------
PATIENT NAME    : Aman Sharma                    ID  : #1
CONTACT NUMBER  : +91 98765 00001                Type: Primary Visit
ATTENDING DOCTOR: Dr. Rajesh Sharma              Specialty: Senior General Physician
--------------------------------------------------------------------------------
ITEMIZED CLINICAL CHARGES:
  1. Specialist Consultation Fee (Dr. Rajesh Sharma  ) : ₹  600.00
  2. Healthcare Service GST (5%)                          : ₹   30.00
--------------------------------------------------------------------------------
TOTAL AMOUNT PAID                                        : ₹  630.00
PAYMENT STATUS : [PAID - UPI Online Payment] (Confirmed)
================================================================================
```

#### 4.2.4 Exported Digital Prescription Slip
```text
================================================================================
                     SMART CLINIC MEDICAL PRESCRIPTION SLIP                     
================================================================================
Consultation Ref: #1                       Date: 13-11-2026 10:30
Appointment Ref : #1                       Type: PRIMARY CONSULTATION
--------------------------------------------------------------------------------
DOCTOR : Dr. Rajesh Sharma                  Specialty: Senior General Physician (ID: #1)
PATIENT: Aman Sharma                        Contact  : +91 98765 00001 (ID: #1)
--------------------------------------------------------------------------------
CLINICAL DIAGNOSIS:
  Acute Upper Respiratory Infection & Seasonal Asthma flare-up

RX / PRESCRIBED MEDICATIONS & INSTRUCTIONS:
  1. Tab Amoxicillin 500mg TDS x 5 days
  2. Tab Cetirizine 10mg OD x 7 days
  3. Levolin inhaler 2 puffs PRN

NEXT FOLLOW-UP REVIEW:
  27-11-2026 11:00 [Automated Status: APPROVED]
--------------------------------------------------------------------------------
Digital Verification Hash: [SEC-MED-7FBEA343]
================================================================================
```

### 4.3 Error Handling & Business Rule Validation Demonstrations
The system was tested against deliberate invalid operations:
```text
  ✔ Passed: Rule 1: Past appointment date rejected -> caught expected InvalidInputException
            ("Appointment date/time cannot be in the past.")

  ✔ Passed: Rule 2: Cannot re-decide already APPROVED appointment -> caught expected InvalidStateException
            ("Appointment #1 is already APPROVED, cannot change it.")

  ✔ Passed: Rule 3: Duplicate consultation prevented -> caught expected InvalidStateException
            ("Consultation already recorded for this appointment.")

  ✔ Passed: Rule 4: Next visit before appointment date rejected -> caught expected InvalidInputException
            ("Next visit must be after the current appointment.")

  ✔ Passed: Rule 5: Double-booking within 30-minute buffer rejected -> caught expected InvalidStateException
            ("Slot Conflict: Dr. Rajesh Sharma already has an active appointment (#4 at 15-11-2026 14:00). A 30-minute consultation buffer is required.")
```

### 4.4 Automated 21-Scenario Verification Test Suite
Executed via `build.bat` or `java -cp out SystemWorkflowTest`:

| Test ID | Test Scenario | Verified System Behavior | Status |
|---|---|---|---|
| **TC-01** | Initial booking creation | Appointment initialized with `PENDING` status | **PASS** |
| **TC-02** | Past-date booking validation | Throws `InvalidInputException` on past timestamp | **PASS** |
| **TC-03** | Doctor approval transition | Transition from `PENDING` to `APPROVED` | **PASS** |
| **TC-04** | Doctor rejection transition | Transition from `PENDING` to `REJECTED` | **PASS** |
| **TC-05** | State finality guard | Modifying approved appointment throws `InvalidStateException` | **PASS** |
| **TC-06** | Consultation & auto follow-up | Creates consultation + auto `APPROVED` follow-up | **PASS** |
| **TC-07** | Non-approved consultation check | Recording on unapproved visit throws `InvalidStateException` | **PASS** |
| **TC-08** | Duplicate consultation guard | Second consultation on same visit throws `InvalidStateException` | **PASS** |
| **TC-09** | Next-visit chronology check | Next visit prior to consultation throws `InvalidInputException` | **PASS** |
| **TC-10** | Data isolation & boundary check | Invalid patient ID throws `NotFoundException` | **PASS** |
| **TC-11** | Doctor clinical analytics | Verifies total, pending, approved, and consultation counts | **PASS** |
| **TC-12** | Doctor consultation isolation | Ensures doctor consultation records are strictly partitioned | **PASS** |
| **TC-13** | Rejection clinical reason audit | Rejection reason preserved in appointment record | **PASS** |
| **TC-14** | Polymorphic Person hierarchy | Validates polymorphic dynamic method dispatch | **PASS** |
| **TC-15** | Digital Rx disk export | Prescription slip generated and written to disk | **PASS** |
| **TC-16** | Multi-threaded sequence counter | Concurrent threads generate sequential IDs without collision | **PASS** |
| **TC-17** | 30-minute slot collision guard | Double-booking within 30 minutes throws `InvalidStateException` | **PASS** |
| **TC-18** | Patient appointment cancellation | Cancels visit and records audit reason | **PASS** |
| **TC-19** | Appointment rescheduling | Updates date/time while enforcing slot collision buffer | **PASS** |
| **TC-20** | Medical billing invoice & GST | Verifies base fee + 5% GST computation | **PASS** |
| **TC-21** | Triage Priority Queue | `PriorityQueue` prioritizes `EMERGENCY` appointments ahead of `ROUTINE` | **PASS** |

**Final Verification Result:** `ALL 21 TESTS PASSED SUCCESSFULLY! (100% Passing)`

---

<div style="page-break-after: always;"></div>

# References

1. Schildt, Herbert. *Java: The Complete Reference*, 12th Edition, McGraw-Hill Education, 2021.
2. Martin, Robert C. *Clean Code: A Handbook of Agile Software Craftsmanship*, Prentice Hall, 2008.
3. Bloch, Joshua. *Effective Java*, 3rd Edition, Addison-Wesley Professional, 2017.
4. Freeman, Eric, and Elisabeth Robson. *Head First Design Patterns*, 2nd Edition, O'Reilly Media, 2020.
5. Oracle Corporation. *Java Platform, Standard Edition 17 API Specification (java.time, java.util.concurrent, javax.swing)*, Oracle Documentation, 2021.
6. Course Curriculum & Laboratory Guidelines, *UCS553: Enterprise Web Application / Core Java Programming Laboratory*, Department of Computer Science & Engineering, Thapar Institute of Engineering & Technology, Patiala, 2026.

# Course Project Progress Report: Doctor Smart Appointment System

**Course:** UCS553: Enterprise Web Application / Core Java  
**Project Title:** Doctor Smart Appointment & Clinical Workflow System  
**Guide:** Dr. Jayendra Barua, Assistant Professor, CSED, TIET Patiala  
**Current Milestone:** Final Course Evaluation (Complete & Verified)  
**Status:** All 21 Automated Verification Tests Passing (100%)  
**Submitted By:**
- **Mohit Srivastav** (Roll No: 1024030368, Batch: 3C41) — Patient Domain, Patient Portal CLI, Web Presentation Dashboard, Input Validation
- **Shreyas Tiwari** (Roll No: 1024030384, Batch: 3C41) — Doctor Workspace, Slot Collision Guard, Triage Priority Queue, Consultation & Follow-up, Invoicing & GST, File Persistence, Core Java HTTP Server

---

## 1. Executive Summary

The **Doctor Smart Appointment System** is a modular Core Java application designed to coordinate outpatient appointment scheduling, clinical consultations, follow-up automation, and medical billing between **Patients** and **Doctors**.

### Key Deliverables Completed:
- **Polymorphic Domain Architecture:** Abstract base class `Person` extended by specialized actors `Patient` and `Doctor` with dynamic method dispatch.
- **Interface-Driven Design (SOLID / DIP):** Data access decoupled via `IDataRepository`, ensuring services depend strictly on storage abstractions.
- **Complete Clinical Lifecycle:**
  1. **Request:** Patient requests an appointment with preferred date/time (initial status `PENDING`), guarded by a **30-minute collision buffer**.
  2. **Review & Decision:** Doctor approves or rejects the request (with mandatory clinical audit reasons).
  3. **Emergency Triage Queue:** `PriorityQueue<Appointment>` with custom `Comparator` prioritizing EMERGENCY and URGENT cases.
  4. **Consultation Entry:** Doctor records clinical diagnosis and prescription for the approved visit.
  5. **Automatic Follow-Up Booking:** Doctor enters next-visit date/time, triggering automatic creation of a follow-up appointment with status `APPROVED`.
  6. **Medical Invoicing & Healthcare GST:** Automatically generates itemized medical bills with 5% healthcare GST stored in `records/invoices/`.
  7. **Prescription Slip & Real-Time Alerts:** Generates formatted Rx slips stored in `records/prescriptions/` and dispatches Observer pattern alerts.
- **Thread-Safety & Concurrency:** Lock-free sequence generation via `AtomicInteger` and thread-safe collections (`ConcurrentHashMap`).
- **File Persistence & Safe Shutdown:** Automated snapshot export and state preservation via a JVM shutdown hook (`FilePersistenceService.java`).
- **Automated Testing Suite:** 21/21 automated unit, integration, collision, and concurrency tests passing (`SystemWorkflowTest.java`) with zero third-party dependencies.
- **Embedded Web Server & REST API:** Built-in Java HTTP server (`SimpleHttpServer.java`) serving REST APIs and dashboard on port 8080.
- **Desktop GUI:** Multi-tab clinical application built using 100% Core Java Swing (`MainWindow.java`).

---

## 2. System Architecture & Class Design

The project strictly adheres to the **Separation of Concerns (SoC)** and **Layered Architecture** principles:

```
src/
├── Main.java                        # System bootstrap, CLI routing & JVM shutdown hook
├── DemoRunner.java                  # Automated 10-stage live evaluator presentation walkthrough
├── SystemWorkflowTest.java          # 20-point unit, integration, collision & concurrency suite
├── model/                           # Domain Entities, DTOs & Enums
│   ├── Person.java                  # Abstract base class (Abstraction & Inheritance)
│   ├── Patient.java                 # Patient entity (Inherits Person)
│   ├── Doctor.java                  # Doctor entity (Inherits Person, includes consultation fee)
│   ├── Appointment.java             # Scheduling entity, cancellation & transition guards
│   ├── AppointmentStatus.java       # Strongly typed Enum (PENDING, APPROVED, REJECTED, CANCELLED)
│   ├── Consultation.java            # Clinical notes & digital Rx slip generator
│   ├── Invoice.java                 # Itemized medical billing & GST calculation
│   ├── NotificationEvent.java       # Observer pattern audit alert message
│   └── DoctorStats.java             # DTO aggregating practitioner analytics
├── repository/                      # Data Access Layer & Storage Engines
│   ├── IDataRepository.java         # DAO interface abstraction (Dependency Inversion Principle)
│   ├── InMemoryRepository.java      # Thread-safe repository (ConcurrentHashMap, AtomicInteger)
│   └── FilePersistenceService.java  # Zero-dependency file persistence & audit logger
├── service/                         # Business Logic & Clinical Validation
│   ├── AppointmentService.java      # Queue management, slot collision guard & doctor analytics
│   ├── ConsultationService.java     # Clinical consultation recording & invoicing
│   └── NotificationService.java     # Real-time event dispatcher (Observer Pattern)
└── ui/                              # Presentation Layer (Console CLI)
    ├── PatientMenu.java             # Patient portal, appointment modification & billing
    ├── DoctorMenu.java              # Doctor workspace, analytics, decisioning & revenue
    └── InputHelper.java             # Date parsing, input sanitation & exception wrappers
```

---

## 3. Team Member Division & Responsibilities

| Contributor | Roll Number | Concrete Modules Implemented |
|---|---|---|
| **Mohit Srivastav** | `1024030368` | - `Patient.java` domain model.<br>- `PatientMenu.java` interactive patient CLI.<br>- `web/` HTML5/CSS3/JS presentation dashboard.<br>- Input validation & date parsing (`InputHelper.java`). |
| **Shreyas Tiwari** | `1024030384` | - `Doctor.java`, `Appointment.java`, `Consultation.java`, `Invoice.java` models.<br>- `DoctorMenu.java` practitioner workspace.<br>- `DoctorStats.java` clinical performance analytics.<br>- Automatic follow-up & slot collision engine (30-min buffer).<br>- `TriageQueueService.java` PriorityQueue triage sorting.<br>- `SimpleHttpServer.java` Core Java embedded HTTP server.<br>- `NotificationService.java` Observer event dispatcher.<br>- `FilePersistenceService.java` snapshot persistence & Rx exporter. |
| **Collaborative** | Both | - `Person.java` abstract base class.<br>- `IDataRepository.java` DAO interface abstraction.<br>- `MainWindow.java` Core Java Swing Desktop GUI.<br>- `SystemWorkflowTest.java` 21-scenario automated test suite. |

---

## 4. Automated Verification & Testing Matrix (21/21 PASS)

Every unit, integration, collision, and concurrency test executes without third-party frameworks:

| Test ID | Test Scenario | Verified Rule & Invariant | Result |
|---|---|---|:---:|
| **TC-01** | Create appointment request | Initial status is `PENDING`, `isFollowUp == false` | ✅ **PASS** |
| **TC-02** | Schedule appointment in the past | Throws `InvalidInputException` ("cannot be in the past") | ✅ **PASS** |
| **TC-03** | Doctor approves pending request | Status updates cleanly to `APPROVED` | ✅ **PASS** |
| **TC-04** | Doctor rejects pending request | Status updates cleanly to `REJECTED` | ✅ **PASS** |
| **TC-05** | Attempt to re-decide decided appointment | Guard prevents state mutation; throws `InvalidStateException` | ✅ **PASS** |
| **TC-06** | Record consultation & auto follow-up | Creates Consultation + Follow-up with status `APPROVED` | ✅ **PASS** |
| **TC-07** | Attempt consultation on PENDING visit | Throws `InvalidStateException` ("only for APPROVED appointment") | ✅ **PASS** |
| **TC-08** | Attempt duplicate consultation on same visit | Throws `InvalidStateException` ("already recorded") | ✅ **PASS** |
| **TC-09** | Set next-visit date before appointment date | Throws `InvalidInputException` ("must be after current appointment") | ✅ **PASS** |
| **TC-10** | Verify patient data isolation & invalid ID checks | Throws `NotFoundException` for unmapped patient/doctor IDs | ✅ **PASS** |
| **TC-11** | Doctor clinical analytics calculation | Aggregates approval %, completed consults, and follow-ups | ✅ **PASS** |
| **TC-12** | Doctor consultation history isolation | Restricts consultation query results to calling doctor | ✅ **PASS** |
| **TC-13** | Rejection with clinical audit reason | Captures and persists clinical justification for rejection | ✅ **PASS** |
| **TC-14** | Polymorphic `Person` abstraction checks | Verifies dynamic dispatch of `getRole()` and `getSummaryDetails()` | ✅ **PASS** |
| **TC-15** | Prescription slip export & file generation | Generates formatted Rx slip and saves to `records/prescriptions/` | ✅ **PASS** |
| **TC-16** | Concurrency & Atomic sequence ID verification | 500 concurrent appointment creations with zero ID collisions | ✅ **PASS** |
| **TC-17** | Double-booking doctor within 30 minutes | Guard prevents slot clash; throws `InvalidStateException` | ✅ **PASS** |
| **TC-18** | Patient appointment cancellation workflow | Status transitions to `CANCELLED` with reason audit trail | ✅ **PASS** |
| **TC-19** | Appointment rescheduling to new valid slot | Updates slot time and validates against schedule collisions | ✅ **PASS** |
| **TC-20** | Medical billing invoice & GST calculation | Itemized receipt computed and stored in `records/invoices/` | ✅ **PASS** |
| **TC-21** | Emergency triage PriorityQueue ordering | `PriorityQueue` with `Comparator` sorts `EMERGENCY` before `ROUTINE` | ✅ **PASS** |

---

## 5. How to Run the Demonstration

### 1. Run Automated Test Suite (Fast 21-Point Verification)
```bash
# Compile and run test suite:
javac -d out src/exception/*.java src/model/*.java src/repository/*.java src/service/*.java src/server/*.java src/ui/*.java src/util/*.java src/*.java
java -cp out SystemWorkflowTest
```

### 2. Run Presentation Walkthrough (For Teacher Demo)
```bash
java -cp out Main --demo
# OR
java -cp out DemoRunner
```

### 3. Run Interactive Console System
```bash
java -cp out Main
```

### 4. Launch Desktop GUI Application (Swing)
```bash
java -cp out Main --gui
```

### 5. Launch Embedded Web Server (:8080)
```bash
java -cp out Main --web
```

### 4. Open Evaluator Slide Deck
```bash
start docs/PRESENTATION_SLIDES.html   # Windows
open docs/PRESENTATION_SLIDES.html    # macOS
```

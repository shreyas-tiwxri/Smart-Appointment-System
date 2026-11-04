# Doctor Smart Appointment & Clinical Workflow System

A modular, enterprise-grade Core Java clinical coordination project engineered with strict Object-Oriented principles, multi-layered architecture, zero-dependency file persistence, slot conflict guards, medical invoicing, and 20 automated tests.

---

## 👥 Engineering Team & Division of Responsibilities

| Role | Engineer | Core Responsibilities & Modules |
|---|---|---|
| **Member 1** | **Mohit Srivastav** | Patient domain model, Patient Portal CLI (`PatientMenu.java`), Web Presentation & interactive visual simulator showcase (`web/`), and Input Sanitation (`InputHelper.java`). |
| **Member 2** | **Shreyas Tiwari** | Appointment & Consultation models, Doctor Workspace CLI (`DoctorMenu.java`), Follow-up automation engine, Slot Collision Guard, Medical Invoicing (`Invoice.java`), Clinical analytics (`DoctorStats.java`), Event Notification Engine (`NotificationService.java`), and File Persistence (`FilePersistenceService.java`). |
| **Collaborative** | **Mohit & Shreyas** | Domain abstraction (`Person.java`), DAO abstraction (`IDataRepository.java`), Automated verification test suite (20/20 test scenarios), evaluation defense & presentation slides. |

---

## 🏥 Clinical Workflow Lifecycle (6-Step Pipeline)
1. **Request Submission:** Patient requests an appointment with a chosen practitioner and preferred date/time (initial status `PENDING`). Enforces a **30-minute collision buffer** to eliminate double-booking.
2. **Clinical Review & Decisioning:** Doctor reviews pending queue and decides:
   - **Approve:** Updates status to `APPROVED`.
   - **Reject:** Updates status to `REJECTED` with a documented clinical reason.
3. **Clinical Consultation & Prescription:** Following the physical visit, doctor records:
   - Official clinical diagnosis.
   - Prescriptions, medications & patient instructions.
   - Recommended next follow-up date and time.
4. **Automated Follow-up Booking:** The system automatically schedules the follow-up appointment with status `APPROVED` directly (no secondary approval required, as it was established by the practitioner).
5. **Medical Invoicing & Healthcare GST:** System computes specialist fees + 5% healthcare GST, generating an official itemized `Invoice` receipt stored in `records/invoices/`.
6. **Digital Prescription Slip & Real-Time Alerts:** Formats official Rx discharge slips with digital cryptographic verification hashes in `records/prescriptions/` and dispatches live alerts via the Observer Pattern.

---

## 📁 Layered Architecture & Codebase Map

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
docs/
├── PRESENTATION_SLIDES.html         # Interactive slide deck for evaluator defense
├── TEACHER_PROGRESS_REPORT.md       # Comprehensive milestone report with architecture & metrics
├── VIVA_PREPARATION_GUIDE.md        # 1-Hour strict evaluation defense script & answers
└── TODO.md                          # 8-Week milestone tracker (100% Complete)
records/                             # Local persistence snapshots, invoices & exported Rx slips
web/                                 # Interactive presentation simulator dashboard
```

---

## ⚡ Run & Evaluation Commands

Requires JDK 17+ (Tested on Java 26 / Java 17).

### 1. Run Automated Test Suite (20/20 Core Java Tests)
```bash
# Compile all sources:
javac -d out src/exception/*.java src/model/*.java src/repository/*.java src/service/*.java src/ui/*.java src/*.java

# Run 20-test verification suite:
java -cp out SystemWorkflowTest
```

### 2. One-Click Evaluator Presentation Demo (--demo mode)
```bash
java -cp out Main --demo
# OR directly
java -cp out DemoRunner
```

### 3. Run Interactive Clinical Console Application
```bash
java -cp out Main
```
- Select `1` for Patient Portal (Aman: ID 1, Riya: ID 2, Kabir: ID 3).
- Select `2` for Doctor Clinical Workspace (Dr. Sharma: ID 1, Dr. Verma: ID 2).
- Select `3` for 1-Click Automated Presentation Walkthrough (10 Stages).
- Select `4` to export system diagnostic database snapshot to disk.

### 4. Interactive Presentation Slides (For Teacher Viva)
Open [`docs/PRESENTATION_SLIDES.html`](docs/PRESENTATION_SLIDES.html) in your browser:
```bash
start docs/PRESENTATION_SLIDES.html   # On Windows
open docs/PRESENTATION_SLIDES.html    # On macOS
```

### 5. Interactive Web Presentation Dashboard & Simulator
Open [`web/index.html`](web/index.html) in your browser:
```bash
start web/index.html  # On Windows
open web/index.html   # On macOS
```

---

## 🔬 Core Java Principles & Technical Highlights

- **Object-Oriented Abstraction & Inheritance:** `Person` base class inherited by `Patient` and `Doctor` with polymorphic methods `getRole()` and `getSummaryDetails()`.
- **Interface-Driven Design (SOLID / DIP):** `IDataRepository` cleanly decouples business services (`AppointmentService`, `ConsultationService`) from physical storage.
- **Slot Collision & Double-Booking Guard:** `AppointmentService.checkSlotAvailability()` enforces a 30-minute collision buffer around every active doctor slot.
- **Observer Pattern Event Dispatching:** `NotificationService` dispatches decoupled alerts to actors on appointment scheduling, decisions, cancellations, and completed visits.
- **Thread-Safety & Concurrency Handling:** `ConcurrentHashMap`, synchronized chronological wrappers, and `AtomicInteger` lock-free primary key counters ensure race condition immunity.
- **Zero-Dependency Disk Persistence:** `FilePersistenceService` snapshots state to `records/database_snapshot.txt`, exports printable clinical Rx slips, and stores billing receipts in `records/invoices/`.
- **Graceful JVM Shutdown Hook:** `Runtime.getRuntime().addShutdownHook(...)` guarantees automated database snapshotting even if the program terminates unexpectedly.
- **Custom Exception Hierarchy:** Unchecked exceptions extending `RuntimeException`:
  - `InvalidInputException` (e.g., past dates, missing fields).
  - `InvalidStateException` (e.g., slot collisions, re-deciding visits, duplicate consultations).
  - `NotFoundException` (e.g., unmapped patient or doctor identifiers).

---

## 📚 Evaluation & Viva Defense Documentation
- **[Interactive Defense Slide Deck](docs/PRESENTATION_SLIDES.html)**: Clean, slide-based presentation for course instructors.
- **[Comprehensive Viva Preparation & Defense Guide](docs/VIVA_PREPARATION_GUIDE.md)**: Word-for-word explanations, tricky examiner questions, and design justifications.
- **[Teacher Progress Report & UML Diagrams](docs/TEACHER_PROGRESS_REPORT.md)**: Complete progress audit, class diagrams, and 20-test matrix.
- **[8-Week Milestone Checklist](docs/TODO.md)**: All milestones verified.

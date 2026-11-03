# Doctor Smart Appointment & Clinical Workflow System

A modular, enterprise-grade Core Java clinical coordination project engineered with strict Object-Oriented principles, multi-layered architecture, zero-dependency file persistence, and automated testing.

---

## 👥 Engineering Team & Division of Responsibilities

| Role | Engineer | Core Responsibilities & Modules |
|---|---|---|
| **Member 1** | **Mohit Srivastav** | Patient domain model, Patient Portal CLI (`PatientMenu.java`), Web Presentation & interactive visual simulator showcase (`web/`). |
| **Member 2** | **Shreyas Tiwari** | Appointment & Consultation models, Doctor Workspace CLI (`DoctorMenu.java`), Follow-up automation engine, Clinical analytics (`DoctorStats.java`), Prescription generation & File Persistence (`FilePersistenceService.java`). |
| **Collaborative** | **Mohit & Shreyas** | Domain abstraction (`Person.java`), DAO abstraction (`IDataRepository.java`), Automated verification test suite (16/16 test scenarios), evaluation defense & documentation. |

---

## 🏥 Clinical Workflow Lifecycle (4-Step Pipeline)
1. **Request Submission:** Patient requests an appointment with a chosen practitioner and preferred date/time (initial status `PENDING`).
2. **Clinical Review & Decisioning:** Doctor reviews pending queue and decides:
   - **Approve:** Updates status to `APPROVED`.
   - **Reject:** Updates status to `REJECTED` with an optional documented clinical reason.
3. **Clinical Consultation & Prescription:** Following the physical visit, doctor records:
   - Official clinical diagnosis.
   - Prescriptions, medications & patient instructions.
   - Recommended next follow-up date and time.
4. **Automated Follow-up Booking:** The system automatically schedules the follow-up appointment with status `APPROVED` directly (no secondary approval required, as it was established by the practitioner).
5. **Digital Prescription Slip & Local Archive:** Generates standardized digital clinical discharge slips saved to `records/prescriptions/Rx_Patient#_Appt#.txt` and automatically backups state to disk.

---

## 📁 Layered Architecture & Codebase Map

```
src/
├── Main.java                        # System bootstrap, CLI routing & JVM shutdown hook
├── DemoRunner.java                  # Automated 8-stage live evaluator presentation walkthrough
├── SystemWorkflowTest.java          # 16-point unit, integration & concurrency verification suite
├── model/                           # Domain Entities, DTOs & Enums
│   ├── Person.java                  # Abstract base class (Abstraction & Inheritance)
│   ├── Patient.java                 # Patient entity (Inherits Person)
│   ├── Doctor.java                  # Doctor entity (Inherits Person)
│   ├── Appointment.java             # Scheduling entity & status transition guards
│   ├── AppointmentStatus.java       # Strongly typed Enum (PENDING, APPROVED, REJECTED)
│   ├── Consultation.java            # Clinical notes & digital Rx slip generator
│   └── DoctorStats.java             # DTO aggregating practitioner analytics
├── repository/                      # Data Access Layer & Storage Engines
│   ├── IDataRepository.java         # DAO interface abstraction (Dependency Inversion Principle)
│   ├── InMemoryRepository.java      # Thread-safe repository (ConcurrentHashMap, AtomicInteger)
│   └── FilePersistenceService.java  # Zero-dependency file persistence & audit logger
├── service/                         # Business Logic & Clinical Validation
│   ├── AppointmentService.java      # Queue management, decisioning & doctor analytics
│   └── ConsultationService.java     # Clinical consultation recording & follow-up generator
├── ui/                              # Presentation Layer (Console CLI)
│   ├── PatientMenu.java             # Patient portal & prescription download
│   ├── DoctorMenu.java              # Doctor workspace, analytics & decisioning
│   └── InputHelper.java             # Date parsing, input sanitation & exception wrappers
docs/
├── TEACHER_PROGRESS_REPORT.md       # Comprehensive milestone report with architecture & metrics
├── VIVA_PREPARATION_GUIDE.md        # 1-Hour strict evaluation defense script & answers
└── TODO.md                          # 8-Week milestone tracker (100% Complete)
records/                             # Local persistence snapshots & exported Rx slips
web/                                 # Interactive presentation simulator dashboard
```

---

## ⚡ Run & Evaluation Commands

Requires JDK 17+ (Tested on Java 26 / Java 17).

### 1. Run Automated Test Suite (16/16 Core Java Tests)
```bash
# Compile all sources:
javac -d out src/exception/*.java src/model/*.java src/repository/*.java src/service/*.java src/ui/*.java src/*.java

# Run test suite:
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
- Select `3` for 1-Click Automated Presentation Walkthrough.
- Select `4` to export system diagnostic database snapshot to disk.

### 4. Interactive Web Presentation Dashboard & Simulator
Open [`web/index.html`](web/index.html) in your browser:
```bash
# Direct browser launch
open web/index.html   # On macOS
start web/index.html  # On Windows

# Or via lightweight HTTP server
python -m http.server 4567 --directory web
```

---

## 🔬 Core Java Principles & Technical Highlights

- **Object-Oriented Abstraction & Inheritance:** `Person` base class inherited by `Patient` and `Doctor` with polymorphic methods `getRole()` and `getSummaryDetails()`.
- **Interface-Driven Design (SOLID / DIP):** `IDataRepository` cleanly decouples business services (`AppointmentService`, `ConsultationService`) from physical storage.
- **Thread-Safety & Concurrency Handling:** `ConcurrentHashMap`, synchronized chronological wrappers, and `AtomicInteger` lock-free primary key counters ensure race condition immunity.
- **Zero-Dependency Disk Persistence:** `FilePersistenceService` snapshots state to `records/database_snapshot.txt` and exports printable clinical Rx slips upon consultation.
- **Graceful JVM Shutdown Hook:** `Runtime.getRuntime().addShutdownHook(...)` guarantees automated database snapshotting even if the program terminates unexpectedly.
- **Custom Exception Hierarchy:** Unchecked exceptions extending `RuntimeException`:
  - `InvalidInputException` (e.g., past dates, missing fields).
  - `InvalidStateException` (e.g., re-deciding approved visits, duplicate consultations).
  - `NotFoundException` (e.g., unmapped patient or doctor identifiers).

---

## 📚 Evaluation & Viva Defense Documentation
- **[Comprehensive Viva Preparation & Defense Guide](docs/VIVA_PREPARATION_GUIDE.md)**: Word-for-word explanations, tricky examiner questions, and design justification.
- **[Teacher Progress Report & UML Diagrams](docs/TEACHER_PROGRESS_REPORT.md)**: Complete progress audit, class diagrams, and test matrix.
- **[8-Week Milestone Checklist](docs/TODO.md)**: All milestones verified.

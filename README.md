# Doctor Smart Appointment System

A Core Java console project (two roles: **Patient** and **Doctor**).

## Workflow
1. Patient requests an appointment (status `PENDING`)
2. Doctor approves / rejects it; patient sees the decision
3. After the visit, doctor records **diagnosis + prescription**; patient can view them
4. Doctor gives the **next-visit date/time** -> a **follow-up appointment is created automatically**

## Project structure
```
src/
  Main.java
  model/        Patient, Doctor, Appointment, Consultation, AppointmentStatus (enum)
  exception/    InvalidInputException, InvalidStateException, NotFoundException
  repository/   InMemoryRepository (ArrayList / HashMap / LinkedHashMap)
  service/      AppointmentService, ConsultationService  (business logic)
  ui/           PatientMenu, DoctorMenu, InputHelper     (console UI)
docs/           proposal + notes
```

## Run & Presentation Commands
Needs JDK 17+.

### 1. One-Click Teacher Demo (Automated Walkthrough)
```bash
javac -d out $(find src -name "*.java")
java -cp out Main --demo
# or
java -cp out DemoRunner
```

### 2. Run Automated Test Suite (10/10 Core Java Tests)
```bash
java -cp out SystemWorkflowTest
```

### 3. Run Interactive Console Application
```bash
java -cp out Main
```

### 4. Interactive Web Presentation & Simulator
Open [`web/index.html`](web/index.html) in your browser:
```bash
open web/index.html
# or with python server
python3 -m http.server 4567 --directory web
```

Date format everywhere: `dd-MM-yyyy HH:mm` (e.g. `25-12-2026 10:30`).

## Documentation & Progress Tracking
- [Teacher Progress Report & UML Diagrams](docs/TEACHER_PROGRESS_REPORT.md)
- [Weekly Milestone Checklist (8-Week Plan)](docs/TODO.md)


## Core Java concepts used
Classes/objects, encapsulation, constructors, collections, enums, `java.time.LocalDateTime`,
custom exceptions + validation, separation of concerns (model / service / repository / ui).

## Rules enforced
- Only `PENDING` requests can be approved/rejected
- Consultation only for `APPROVED` appointments, once per appointment
- No past dates for requests; next visit must be after the current appointment
- Follow-up is created with status `APPROVED` (doctor scheduled it)

## Team split
| Member | Work |
|---|---|
| Member 1 | Patient/Doctor models, patient-side UI |
| Member 2 | Appointment/Consultation models, doctor-side workflow, follow-up logic |
| Both | Shared interfaces, integration, final review |

## Scope
Only what is in the proposal. Extras (login, cancel/reschedule, DB, notifications...) need approval first.

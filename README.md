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

## Run
Needs JDK 17+.
```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out Main
```
Windows (PowerShell):
```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```
Date format everywhere: `dd-MM-yyyy HH:mm` (e.g. `25-12-2026 10:30`).

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

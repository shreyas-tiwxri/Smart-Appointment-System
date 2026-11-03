# Weekly Plan & Progress Tracking (8 Weeks)

- [x] **W1: Finalize design, class diagram, repo setup**
  - Project scope definition, git repository structure, layered package architecture (`model`, `service`, `repository`, `ui`, `exception`).
- [x] **W2: Models + Enum**
  - Created polymorphic hierarchy (`Person` -> `Patient`, `Doctor`), `Appointment`, `Consultation`, `DoctorStats`, and `AppointmentStatus` (PENDING, APPROVED, REJECTED).
- [x] **W3: Repository + AppointmentService**
  - DAO Interface `IDataRepository`, Thread-safe collection storage (`ConcurrentHashMap`, `LinkedHashMap`, `ArrayList`, `AtomicInteger`), request booking, pending queue filters, approval/rejection logic.
- [x] **W4: ConsultationService + Exceptions & Validation**
  - Consultation recording, prescription/diagnosis linkage, automatic follow-up appointment generation, custom exceptions (`InvalidInputException`, `InvalidStateException`, `NotFoundException`).
- [x] **W5: Patient & Doctor UI**
  - Interactive terminal menus for Patients (`PatientMenu`) and Doctors (`DoctorMenu`) with `InputHelper` date parsing, doctor clinical analytics, and digital Rx generation.
- [x] **W6: Integration + Testing**
  - End-to-end integration test suite (`SystemWorkflowTest.java` - expanded to 16/16 test scenarios including concurrency and analytics).
- [x] **W7: Edge Cases, Cleanup, Comments & Demo Runner**
  - Guard clauses for past dates, status lock after decision, duplicate consultation protection, next-visit sequencing.
  - Interactive demonstration runner (`DemoRunner.java`) and CLI presentation flag (`--demo`).
- [x] **W8: Report, Viva Prep, Zero-Dependency Persistence & Showcase**
  - Zero-dependency file persistence engine (`FilePersistenceService.java`) with JVM Shutdown Hook.
  - Comprehensive progress report (`docs/TEACHER_PROGRESS_REPORT.md`), in-depth 1-hour defense guide (`docs/VIVA_PREPARATION_GUIDE.md`), and interactive web simulator dashboard (`web/`).

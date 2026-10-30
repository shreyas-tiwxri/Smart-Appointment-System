# Weekly Plan & Progress Tracking (8 Weeks)

- [x] **W1: Finalize design, class diagram, repo setup**
  - Project scope definition, git repository structure, layered package architecture (`model`, `service`, `repository`, `ui`, `exception`).
- [x] **W2: Models + Enum**
  - Created `Patient`, `Doctor`, `Appointment`, `Consultation`, and `AppointmentStatus` (PENDING, APPROVED, REJECTED).
- [x] **W3: Repository + AppointmentService**
  - In-memory collection storage (`HashMap`, `LinkedHashMap`, `ArrayList`), request booking, pending queue filters, approval/rejection logic.
- [x] **W4: ConsultationService + Exceptions & Validation**
  - Consultation recording, prescription/diagnosis linkage, automatic follow-up appointment generation, custom exceptions (`InvalidInputException`, `InvalidStateException`, `NotFoundException`).
- [x] **W5: Patient & Doctor UI**
  - Interactive terminal menus for Patients (`PatientMenu`) and Doctors (`DoctorMenu`) with `InputHelper` date parsing and input validation.
- [x] **W6: Integration + Testing**
  - End-to-end integration test suite (`SystemWorkflowTest.java` - 10/10 test cases passing without external dependencies).
- [x] **W7: Edge Cases, Cleanup, Comments & Demo Runner**
  - Guard clauses for past dates, status lock after decision, duplicate consultation protection, next-visit sequencing.
  - Interactive demonstration runner (`DemoRunner.java`) and CLI presentation flag (`--demo`).
- [ ] **W8: Report, PPT, Viva Prep, Buffer**
  - Comprehensive progress report generated (`docs/TEACHER_PROGRESS_REPORT.md`), visual showcase interactive dashboard, and viva defense notes.

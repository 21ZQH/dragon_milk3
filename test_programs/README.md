# Test Programs

This folder is provided as a clear submission entry point for the coursework
requirement "Test programs".

## Source Locations

The test source files are copied from the Maven standard test directory.

Original location:

```text
src/test/java/
```

Submission copy:

```text
test_programs/java/
```

## Important

This folder includes its own `pom.xml`, so the tests can be run either from
this `test_programs` folder or from the project root. The `test_programs/pom.xml`
points back to `../webapp/WEB-INF/src` for the application source code.

## Run Tests from test_programs

1. Open a terminal in the `test_programs` folder.
2. Run:

```cmd
mvn test
```

## Alternative from the Project Root

```cmd
mvn test
```

Expected result:

- Tests run: 138
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

## Test Coverage Overview

```text
controller/   Servlet controller workflow tests
listener/     ServletContext initialization tests
model/        Model behavior tests
repository/   Text repository tests
service/      Business service tests
service/ai/   AI fallback and resume extraction tests
store/        File-store and CSV persistence tests
testsupport/  Shared utilities for isolated store tests
```

## Detailed Test Sections

### controller/

Controller tests verify servlet routing, request validation, session handling,
forward/redirect targets, and the data placed into request attributes before JSP
rendering.

- `AccountControllerTest`
  - Rejects MO self-registration because MOs must be created by Admin.
  - Checks duplicate registration handling.
  - Verifies TA login, invalid login, session replacement, and unknown action behavior.
- `EntryControllerTest`
  - Verifies `/ta`, `/mo`, and `/admin` entry routing.
  - Checks the public TA entry page, TA auth page, course detail page, and deadline values read from `ServletContext`.
- `AdminControllerTest`
  - Verifies Admin authentication and forbidden access for non-Admin users.
  - Covers dashboard, candidate management, MO creation, deadline pages, deadline saving, reset recruitment cycle, logout, resume streaming, and unknown action handling.
- `MOClassControllerTest`
  - Covers MO dashboard, personal centre, my project, project detail, draft publish, published course save changes, deadline locking, review page access, saved review picks, and published review results.
  - Includes regression coverage that `Save Changes` can store a shortlist while final `Publish` is still controlled by the TA Positions limit.
- `TAClassControllerTest`
  - Covers TA home routing, course detail, application page setup, resume upload, PDF validation, generated application form flow, personal centre, review notification clearing, resume viewing, withdrawal, and deadline restrictions.

### listener/

- `AppContextListenerTest`
  - Verifies that application and MO modification deadlines are loaded from file storage into `ServletContext` when the web application starts.

### model/

- `TATest`
  - Verifies TA domain behavior such as avoiding duplicate course applications, replacing application forms for the same course, clearing unread review flags, and withdrawing applications.

### repository/

Repository tests verify that repository implementations preserve the architecture
boundary by delegating persistence work to Store classes.

- `TxtApplicationFormRepositoryImplTest`
  - Checks application form persistence delegation.
- `TxtCourseRepositoryImplTest`
  - Checks course persistence delegation.
- `TxtDeadlineRepositoryImplTest`
  - Checks application and MO deadline persistence delegation.
- `TxtUserRepositoryImplTest`
  - Checks user persistence, lookup, and update delegation.

### service/

Service tests verify business rules independently from JSP pages.

- `AccountServiceImplTest`
  - Covers registration, duplicate email rejection, login with optional role, BUPT TA email validation, generated TA access keys, and built-in Admin seeding.
- `ApplicationReviewServiceImplTest`
  - Verifies that review uses submitted forms only, publishing updates submitted TA statuses, saved picks can exceed the quota, and final publish cannot approve more candidates than TA Positions.
- `DeadlineServiceImplTest`
  - Verifies open/closed deadline logic and parsing of deadline form fields.
- `ResumeStorageServiceImplTest`
  - Verifies stable resume file names and deletion of stored resume files/directories.
- `TAApplicationServiceImplTest`
  - Covers master resume detection, submitted form markers, application submission, the three-course TA application limit, personal centre application counts, resubmission to an existing course, withdrawal behavior, and PDF-only resume validation.

### service/ai/

AI tests focus on fallback behavior and resume text extraction so the workflow can
still be tested without a live API key.

- `MockApplicationFormAiClientTest`
  - Verifies local fallback form generation from TA profile, course information, and resume text.
- `PdfBoxResumeTextExtractorTest`
  - Verifies text extraction from valid PDFs, empty output for missing files, and rejection of invalid PDFs.

### store/

Store tests verify file-based persistence and regression cases that can corrupt
runtime data if not handled carefully.

- `CourseStoreTest`
  - Covers saving/loading courses, ignoring malformed rows, missing file behavior, applicant reconstruction from users, review metadata restoration, TA Positions restoration, and CSV-safe values containing commas or quotes.
- `DeadlineStoreTest`
  - Covers application deadline persistence, MO modification deadline persistence, and blank deadline files.
- `UserStoreTest`
  - Covers role-based login, MO owned courses, MO profile fields, TA applied courses, submitted application reconstruction, TA profile updates, unread review flags, missing user file behavior, and CSV-safe values containing commas or quotes.

### testsupport/

- `StoreTestSupport`
  - Provides shared temporary-file helpers so Store tests can run in isolation without changing the real runtime data files.

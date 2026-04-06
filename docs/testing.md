# Testing Setup

This project now includes a basic Maven + JUnit testing setup for the Java classes under `webapp/WEB-INF/src`.

## Added Test Coverage

- `store.UserStoreTest`
  Verifies saving users, validating login credentials, and duplicate email checks with isolated test files.
- `store.CourseStoreTest`
  Verifies saving courses, loading course lists, and ignoring malformed rows.
- `controller.AccountControllerTest`
  Covers register, login, duplicate registration, and invalid action flows.
- `controller.MOClassControllerTest`
  Covers create project navigation, personal centre navigation, and publishing a course.
- `controller.TAClassControllerTest`
  Covers loading the course list into session and forwarding to the TA job list page.

## How To Run

Run the Maven test phase from the repository root:

```bash
mvn test
```

If Maven is not installed globally on your machine, use any local Maven distribution and run its `mvn` or `mvn.cmd` executable against this project root.

## Notes

- `UserStore` and `CourseStore` now support test-only file overrides through system properties so tests do not touch Tomcat data.
- This setup is a clean starting point for the unit-test part of the coursework. You can extend it later with manual system tests and acceptance-test tables in `docs/`.

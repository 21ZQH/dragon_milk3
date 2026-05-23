# Testing Notes

The project uses Maven, JUnit 5, and Mockito for automated tests. Tests live in
the standard Maven test directory and are also copied into `test_programs/` for
coursework submission inspection.

## Test Locations

- Primary test source: `src/test/java/`
- Submission copy: `test_programs/java/`
- Test-program instructions: `test_programs/README.md`

## Current Coverage

- Controller tests for Account, Entry, Admin, MO, and TA workflows.
- Service tests for account registration, deadlines, TA applications, review publication, resume storage, and AI fallback behavior.
- Store tests for CSV-safe persistence, deadlines, users, courses, and application forms.
- Repository tests for text-file-backed repository adapters.
- Listener tests for ServletContext initialization.

Recent regression coverage includes:

- CSV fields containing commas and quotes are preserved.
- Built-in MO seed accounts/courses are not recreated automatically.
- Published MO courses show `Save Changes` instead of `Save Draft`.
- MO review `Save Changes` can save a shortlist above the TA position limit.
- MO review `Publish` cannot approve more applicants than the configured TA Positions.
- TA review notification dots are shown only for logged-in TAs and are cleared when Personal Centre is opened.

## How To Run

Run from the repository root:

```bash
mvn test
```

Expected latest result:

```text
Tests run: 138, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The web application compilation helper can also be checked on Windows:

```bat
cmd /c webapp\command2.bat
```

Expected output:

```text
Compilation completed.
```

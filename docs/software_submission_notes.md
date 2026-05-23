# Software Submission Notes

This file maps the required `Software_groupXXX.zip` contents to the current project files.

## Required Items

| Requirement | Current Draft Status | Project Location |
|---|---|---|
| Source code | Ready as project source | `webapp/WEB-INF/src/`, `webapp/WEB-INF/views/`, `webapp/css/`, `pom.xml` |
| Test programs | Ready as JUnit tests, with a submission copy for easy inspection | `src/test/java/`, `test_programs/java/`, `test_programs/README.txt` |
| Code documentation, such as JavaDocs | Initial JavaDoc generated successfully | `target/reports/apidocs/`, `docs/controller-service-outline.md` |
| User manual with key screenshots | English Word user manual is ready; screenshots should be checked against the final running app | `user manual.docx`, `docs/images/` |
| README setup/config/run instructions | Ready as text README | `readme.txt` |

## Current Feature Notes

- MOs manually set `TA Positions` in the project detail page; AI draft generation does not set the approval quota.
- TAs can see `TA Positions` on the public job list, course detail page, and application pages.
- MO review `Save Changes` stores a shortlist and can exceed `TA Positions`; saved picks appear first the next time the review page is opened.
- MO review `Publish` enforces `TA Positions`, so final approved applicants cannot exceed the configured quota.
- AI features are optional. If Groq configuration or the current PowerShell transport is unavailable, local fallback generation is used so TA and MO workflows remain usable.

## Current Verification

The current version has been checked with:

```text
mvn test
cmd /c webapp\command2.bat
mvn -q -DskipTests javadoc:javadoc
```

Latest verified unit-test result:

```text
Tests run: 138, Failures: 0, Errors: 0
```

Generated JavaDoc entry page:

```text
target/reports/apidocs/index.html
```

## Suggested Screenshot List

The user manual asks for key screenshots. At least one screenshot should be captured for each main frame:

- TA public jobs page with recruitment timeline.
- TA login/register page.
- TA Personal Centre showing applications and notification behavior.
- TA application form page.
- MO dashboard with recruitment timeline.
- MO My Project page.
- MO Project Detail page.
- MO Review Applications page.
- Admin dashboard.
- Admin MO Accounts page.
- Admin Candidates page.
- Admin TA Application Deadline page.
- Admin MO Course Modification Deadline page.

## Notes for Final ZIP

- Use the required filename pattern: `Software_groupXXX.zip`.
- Replace `XXX` with the actual group number.
- Do not include real API keys.
- AI keys should be configured through `GROQ_API_KEY`; do not store them in source files or data files.
- Runtime test data can be cleared before packaging if a clean demonstration state is preferred.
- Keep `readme.txt` and `user manual.docx` at the project root so they are easy to find after unzip.

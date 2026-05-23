TA Recruitment System
BUPT Teaching Assistant Recruitment Platform

1. Project Summary

This project is a role-based teaching assistant recruitment system for BUPT International School.
It is built with Java Servlet, JSP, Maven, JUnit 5, Mockito, Apache PDFBox, and file-based persistence.

The system supports three independent entry pages and three roles:

- TA: view published jobs, register with a BUPT email, upload a profile resume, generate and submit application forms, manage submitted applications, withdraw applications, and view review results.
- MO: manage assigned course projects, edit and publish recruitment information before the MO deadline, review applications after the TA deadline, save picked applicants, and publish final results.
- Admin: manage MO accounts, assign courses, view candidates, set TA application deadlines, set MO course modification deadlines, and reset the yearly recruitment cycle.

The project intentionally uses text files instead of a database so that it is easy to deploy, inspect, reset, and demonstrate for coursework.


2. Software ZIP Contents

According to the coursework requirement, the software ZIP should include the following items:

- Source code
  - webapp/WEB-INF/src/ contains Java source code.
  - webapp/WEB-INF/views/ contains JSP pages.
  - webapp/css/ contains shared styling.
  - pom.xml defines Maven dependencies and test configuration.
- Test programs
  - src/test/java/ contains JUnit 5 and Mockito tests for controllers, services, repositories, stores, models, AI fallback behavior, and listener initialization.
  - test_programs/java/ is a submission copy of the same test source files, provided so the marker can find the test programs immediately after unzipping.
  - test_programs/README.txt explains how to inspect and run the tests.
- Code documentation
  - JavaDoc comments are included in the Java source files.
  - Generate JavaDocs with: mvn javadoc:javadoc
  - Generated entry page: target/reports/apidocs/index.html
- User manual with screenshots
  - user manual.docx is the Word user manual and includes key screenshots for the main TA, MO, and Admin frames.
- Readme
  - readme.txt explains setup, configuration, testing, JavaDocs, running, data reset, and demo workflows.


3. Technology Stack

- Java 17 source target, tested locally with JDK 21
- Jakarta Servlet API 6.0
- JSP pages under webapp/WEB-INF/views
- Apache Tomcat 11 for local deployment
- Maven for compilation, tests, and JavaDocs
- JUnit 5 and Mockito for tests
- Apache PDFBox for resume text extraction
- Optional Groq API integration for AI-generated TA application forms and MO course drafts


4. Main Project Structure

.
|-- pom.xml
|-- README.md
|-- readme.txt
|-- user manual.docx
|-- test_programs/
|-- start_SE.bat
|-- start_se.sh
|-- docs/
|-- src/test/java/
`-- webapp/
    |-- css/
    |-- images/
    `-- WEB-INF/
        |-- file/
        |-- src/
        |   |-- controller/
        |   |-- listener/
        |   |-- model/
        |   |-- repository/
        |   |-- service/
        |   |-- service/ai/
        |   `-- store/
        |-- views/
        |   |-- admin/
        |   |-- entry/
        |   |-- mo/
        |   `-- ta/
        `-- web.xml


5. Architecture

The current architecture is:

JSP page -> Servlet Controller -> Service -> Repository -> Store -> text file

Important design rule:

Repository interfaces and repository-to-store method calls are preserved. Persistence improvements are implemented inside the Store layer, so the high-level architecture is not changed.

Examples:

- TxtUserRepositoryImpl calls UserStore methods.
- TxtCourseRepositoryImpl calls CourseStore methods.
- TxtApplicationFormRepositoryImpl calls ApplicationFormStore methods.
- TxtDeadlineRepositoryImpl calls DeadlineStore methods.
- CSV parsing and escaping are handled by CsvRecord, UserStore, and CourseStore.

Deadline state:

- Admin saves the TA application deadline into applicationDeadline in ServletContext.
- Admin saves the MO course modification deadline into moCourseModifyDeadline in ServletContext.
- TA and MO pages read deadline status from ServletContext.
- AppContextListener rebuilds ServletContext deadline values when the web application starts by loading deadline.txt and mo-deadline.txt.

Review notification state:

- When an MO publishes final review results, affected TA applications are marked as unread.
- A logged-in TA sees a red notification dot on the Personal Center link.
- In Personal Centre, each course with a newly published result also shows a course-level red dot.
- Opening Personal Centre clears all unread review notifications for that TA.
- Guests and logged-out users do not see notification dots.

Annual reset state:

- Admin can use Reset Recruitment Cycle at the beginning of a new recruitment year.
- Reset clears TA applications, generated application forms, MO review picks, final review results, unread review notifications, and both deadline values.
- Reset keeps Admin/MO/TA accounts, MO-course ownership, course names, and previous recruitment text.
- Every course returns to Draft, so MOs can reuse and edit the previous text before publishing again.

Course publishing state:

- Draft courses show Save Draft and Publish Recruitment.
- MOs fill in TA Positions manually in the project edit page; AI draft generation does not set this field.
- TA Positions is shown to TAs on the public job list, course detail page, and application pages.
- Published courses show Save Changes only.
- A Published course is not saved back to Draft through the MO project page.
- After the MO course modification deadline, editing and publishing are locked.

Review selection state:

- Review Save Changes stores the MO's current picked candidates as a shortlist.
- Saved picks are displayed first when the MO opens the review page again, so the MO can refine candidates from a priority group.
- Save Changes does not enforce the TA Positions limit.
- Publish enforces the TA Positions limit, so the final number of approved applicants cannot exceed the configured quota.


6. Runtime Data and Reset Instructions

Source template data files are located at:

webapp/WEB-INF/file/

After deployment, Tomcat uses:

TOMCAT_HOME/webapps/SE/WEB-INF/file/

Important runtime files:

- users.txt: TA, MO, and Admin accounts.
- courses.txt: course information, recruitment text, TA positions, draft/published status, picked applicants, and review status.
- deadline.txt: TA application deadline.
- mo-deadline.txt: MO course modification deadline.
- application-forms.txt: generated, saved, and submitted TA application forms.
- resume/: uploaded PDF resumes.

Manual reset for testing:

Usually, clear the deployed Tomcat runtime folder, not the source template folder.

Clear or empty:

- TOMCAT_HOME/webapps/SE/WEB-INF/file/users.txt
- TOMCAT_HOME/webapps/SE/WEB-INF/file/courses.txt
- TOMCAT_HOME/webapps/SE/WEB-INF/file/application-forms.txt
- TOMCAT_HOME/webapps/SE/WEB-INF/file/deadline.txt
- TOMCAT_HOME/webapps/SE/WEB-INF/file/mo-deadline.txt
- TOMCAT_HOME/webapps/SE/WEB-INF/file/resume/

Keep the file names and folder names. Empty file contents instead of deleting the files. Empty the contents of resume/ but keep the folder. Restart Tomcat or run start_SE.bat again after clearing deadline files, because deadline status is loaded into ServletContext when the web application starts.

For normal yearly reuse, prefer Admin Dashboard -> Reset Recruitment Cycle. It clears application-cycle data while preserving accounts, MO-course ownership, course names, and previous recruitment text.

CSV persistence note:

The project previously split users.txt and courses.txt rows directly by commas, which could corrupt values such as Java, Python, SQL. The Store layer now uses standard CSV escaping:

- A value containing a comma is written inside quotes.
- A value containing double quotes escapes the inner quotes by doubling them.

Example:

Java, Python, SQL

is saved as:

"Java, Python, SQL"

TA "Lead"

is saved as:

"TA ""Lead"""

Files involved:

- webapp/WEB-INF/src/store/CsvRecord.java
- webapp/WEB-INF/src/store/CourseStore.java
- webapp/WEB-INF/src/store/UserStore.java
- src/test/java/store/CourseStoreTest.java
- src/test/java/store/UserStoreTest.java


7. Accounts

Admin accounts:

- admin1@bupt.edu.cn / admin123456
- admin2@bupt.edu.cn / admin223456
- admin3@bupt.edu.cn / admin323456
- admin4@bupt.edu.cn / admin423456
- admin5@bupt.edu.cn / admin523456

MO accounts:

MO accounts are created by Admin from the MO Accounts page. When Admin creates an MO, Admin assigns one or more courses. Existing course names are reused case-insensitively. New course names are created as Draft courses and can receive an AI-generated or fallback recruitment draft.

TA accounts:

TA accounts are registered from the TA login/register page with an email ending in @bupt.edu.cn. After registration, the system generates a TA access key. The TA uses that access key to log in later.


8. Build and Test

Test program locations:

- Primary Maven test source directory: src/test/java/
- Submission-friendly copy: test_programs/java/
- Test program instructions: test_programs/README.txt

The copy in test_programs/java/ is provided for the coursework requirement.
Run the tests from the project root rather than from inside test_programs/,
because Maven needs pom.xml and the project dependencies.

Run all Maven tests from the project root:

mvn test

Recommended clean verification:

mvn clean test

Current verified result:

- Tests run: 138
- Failures: 0
- Errors: 0

Manual compile/deployment compilation check:

Windows:

cmd /c webapp\command2.bat

Expected result:

Compilation completed.

macOS/Linux:

cd webapp
./command2.sh


9. JavaDocs

JavaDocs are included in the Java source code and package-level documentation.

Generate JavaDocs:

mvn javadoc:javadoc

Open:

target/reports/apidocs/index.html

Important documented packages:

- controller
- listener
- model
- repository
- repository.impl
- service
- service.ai
- service.ai.impl
- service.impl
- store


10. Optional AI Configuration

The system can use Groq API for two optional AI-assisted features:

- TA application form generation from a PDF resume and course requirements.
- Initial MO course recruitment draft generation when Admin creates a new course for an MO.

If no API key is configured, the system still works. It uses local fallback generation, so TA application and MO course creation workflows are not blocked.

Platform note:

- The current Groq transport uses PowerShell. It works best on Windows.
- On macOS/Linux, real Groq generation requires PowerShell Core (`pwsh`) or a future Java HttpClient implementation.
- If the transport or API key is unavailable, the system automatically uses local fallback generation, so the application remains usable.

Environment variables:

GROQ_API_KEY=your_groq_api_key_here
GROQ_MODEL=llama-3.3-70b-versatile

GROQ_MODEL is optional. If it is not set, the default model is llama-3.3-70b-versatile.

Windows user-level setup:

[Environment]::SetEnvironmentVariable("GROQ_API_KEY", "your_groq_api_key_here", "User")
[Environment]::SetEnvironmentVariable("GROQ_MODEL", "llama-3.3-70b-versatile", "User")

Then close and reopen the terminal or IDE that starts Tomcat, and restart Tomcat or rerun start_SE.bat.

Temporary PowerShell setup:

$env:GROQ_API_KEY="your_groq_api_key_here"
$env:GROQ_MODEL="llama-3.3-70b-versatile"
.\start_SE.bat

Security notes:

- Do not commit a real API key to Git.
- Do not paste the API key into users.txt, courses.txt, JSP pages, or Java source files.
- Use environment variables instead.
- If a key is exposed, revoke it from the Groq dashboard and create a new one.


11. Local Deployment

Windows:

1. Install Tomcat 11.
2. Set TOMCAT_HOME or CATALINA_HOME, or place Tomcat at G:\Tomcat.
3. Run from the project root:

start_SE.bat

The script will:

1. Find Tomcat.
2. Back up existing runtime data under webapps/SE/WEB-INF/file.
3. Copy webapp to TOMCAT_HOME/webapps/SE.
4. Restore runtime data.
5. Compile Java classes.
6. Start Tomcat.
7. Open http://localhost:8081/SE/ta.

macOS/Linux:

1. Install Tomcat 11.
2. Set TOMCAT_HOME or CATALINA_HOME.
3. Run:

./start_se.sh


12. Application URLs

Default local URLs:

- TA entry: http://localhost:8081/SE/ta
- MO entry: http://localhost:8081/SE/mo
- Admin entry: http://localhost:8081/SE/admin

Main servlet mappings:

- /ta, /mo, /admin: entry pages
- /account: login and registration
- /TAclasscontroller: TA actions
- /MOclasscontroller: MO actions
- /AdminController: Admin actions


13. User Manual and Screenshots

The user manual is:

user manual.docx

It includes key screenshots for the main application frames, including:

- TA home/job list
- TA login and registration
- TA course detail
- TA generated application form
- TA personal centre and review result status
- MO login
- MO dashboard
- MO personal centre
- MO my project
- MO project detail in Draft and Published states
- MO review candidates
- MO published review result
- Admin login
- Admin dashboard
- Admin MO management
- Admin candidate management
- Admin deadline pages
- Admin reset recruitment cycle


14. User Workflows

TA workflow:

1. Open /SE/ta.
2. Check the public recruitment timeline, including TA application deadline, MO course information deadline, and current open/locked status.
3. Choose TA Login/Register.
4. Register with a @bupt.edu.cn email.
5. Save the generated TA access key.
6. Browse published jobs.
7. Open a course detail page.
8. Upload a PDF resume if needed.
9. Generate a standard application form.
10. Review, edit, save, or submit the application form.
11. Use Personal Centre to view submitted courses, modify applications before the deadline, withdraw applications before the deadline, and check review results after publication.
12. If review results have just been published, check the red notification dot on Personal Center and on the updated course in Personal Centre.

MO workflow:

1. Open /SE/mo.
2. Log in with an MO account created by Admin.
3. Check the dashboard recruitment timeline, including TA application deadline, MO course information deadline, edit status, and review status.
4. Open Publish Recruitment or My Project.
5. Use My Project search if needed.
6. Use Back to Personal Centre or Back to Dashboard depending on where you want to return.
7. Select an assigned course.
8. Edit job title, TA positions, description, and requirement.
9. For a Draft course, use Save Draft or Publish Recruitment before the MO deadline. A positive TA Positions value is required before publishing.
10. For a Published course, use Save Changes to update the published recruitment text before the MO deadline. If TA Positions is left unchanged, the existing value is preserved.
11. After the TA application deadline passes, open Review Applications.
12. Select candidates.
13. Use Save Changes to save a candidate shortlist; saved candidates appear first the next time the review page is opened.
14. Publish final review results. The final approved count cannot exceed TA Positions.
15. Publishing final review results triggers unread result notifications for affected TAs.

Admin workflow:

1. Open /SE/admin.
2. Log in with a built-in Admin account.
3. Open MO Accounts to create MO accounts and assign courses.
4. Open Candidates to inspect registered TA users and applications.
5. Open TA Application Deadline to set when TA applications close.
6. Open MO Course Modification Deadline to set when MO editing and publishing close.
7. Open Reset Recruitment Cycle to start a new yearly recruitment cycle while preserving accounts and course draft text.


15. Demonstration and Validation Checklist

Before a demo:

1. Run mvn test.
2. Run cmd /c webapp\command2.bat.
3. Run start_SE.bat.
4. Open http://localhost:8081/SE/ta.
5. Confirm /SE/mo and /SE/admin open.
6. Log in as admin1@bupt.edu.cn / admin123456.
7. Set and check both deadlines.
8. Create or use an Admin-created MO account.
9. Assign at least one course to the MO.
10. Log in as the MO and confirm the dashboard timeline and assigned projects appear.
11. Publish a Draft course and confirm it becomes Published.
12. Confirm Published courses show Save Changes instead of Save Draft.
13. Register or use a TA with a @bupt.edu.cn email.
14. Confirm the TA public page timeline appears.
15. Submit or inspect a test application.
16. Move the TA application deadline to the past for review-stage demonstration.
17. Publish a review result as MO and confirm the logged-in TA sees a red notification dot before opening Personal Centre.
18. Optionally run Admin Reset Recruitment Cycle and confirm courses return to Draft while course text remains.


16. Recent Validation Workflow

The recent validation workflow included:

1. Inspect project structure with rg and Maven configuration.
2. Run mvn test.
3. Run mvn clean test when a clean compile was needed.
4. Run webapp\command2.bat to verify the non-Maven deployment compile path.
5. Start Tomcat with start_SE.bat.
6. Check that Tomcat listens on port 8081.
7. Request /SE/ta, /SE/mo, and /SE/admin.
8. Log in as Admin and request dashboard, MO management, candidate management, deadline pages, and reset page.
9. Log in as MO and request dashboard, my project, personal centre, project detail, and review pages.
10. Register a temporary TA, request TA job pages and personal centre, submit/inspect an application, and clean up temporary test data.
11. Inspect Tomcat logs for deployment or JSP errors.
12. Verify CSV escaping for quoted values in CourseStore and UserStore.
13. Verify deadline state is sourced from ServletContext after startup.
14. Verify TA notification dots for published review results.
15. Verify Admin Reset Recruitment Cycle.
16. Verify Published course update flow and Draft course publish flow.


17. Known Limitations and Suggested Future Work

- JSP pages still output some values directly. For a production-grade system, use HTML escaping consistently.
- Passwords are stored in text files. For a real deployment, use password hashing and remove password display from Admin pages.
- Runtime file paths are configured from ServletContext real paths at application startup. The deployment scripts still use the SE context path for the coursework demo.
- Groq API transport currently uses PowerShell on Windows. A Java HttpClient implementation would be more portable.
- This is a prototype without a database and without full browser automation tests. The current JUnit suite is good for service, store, controller, and regression coverage, but manual UI smoke testing is still recommended before demonstrations.
- Notification dots depend on the TA session being refreshed from file storage. The current TA entry and TA controller paths refresh the logged-in TA object before rendering TA pages.

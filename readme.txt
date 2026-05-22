TA Recruitment System
BUPT Teaching Assistant Recruitment Platform

1. Project Summary

This project is a role-based teaching assistant recruitment system for BUPT International School.
It is built with Java Servlet, JSP, Maven, JUnit 5, Mockito, and file-based persistence.

The system supports three roles:

- TA: view jobs, register with a BUPT email, upload a profile resume, generate and submit an application form, manage applications, withdraw applications, and view review results.
- MO: manage assigned course projects, edit and publish recruitment information before the MO deadline, review applications after the TA application deadline, save picked applicants, and publish final results.
- Admin: manage MO accounts, assign courses, view candidates, set TA application deadlines, and set MO course modification deadlines.
- Admin can also reset the yearly recruitment cycle while keeping accounts, MO-course ownership, and course draft text.

The project intentionally uses text files instead of a database so that it is easy to deploy and demonstrate for coursework.


2. Technology Stack

- Java 17 source target, tested locally with JDK 21
- Jakarta Servlet API 6.0
- JSP pages under webapp/WEB-INF/views
- Apache Tomcat 11 for local deployment
- Maven for compilation and unit tests
- JUnit 5 and Mockito for tests
- Apache PDFBox for resume text extraction
- Optional Groq API integration for AI-generated application forms and course drafts


3. Main Project Structure

.
|-- pom.xml
|-- README.md
|-- readme.txt
|-- 用户使用手册.md
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
        |   `-- store/
        |-- views/
        |   |-- admin/
        |   |-- entry/
        |   |-- mo/
        |   `-- ta/
        `-- web.xml


4. Architecture

The current architecture is:

JSP page -> Servlet Controller -> Service -> Repository -> Store -> text file

Important rule:
Repository interfaces and repository-to-store method calls are preserved. The persistence improvement for CSV data is implemented inside the Store layer only.

Examples:

- TxtUserRepositoryImpl still calls UserStore.getTAList(), UserStore.saveUser(), UserStore.validateUser(), and related methods.
- TxtCourseRepositoryImpl still calls CourseStore.getCourseList(), CourseStore.saveCourse(), and CourseStore.updateCourse().
- The CSV parsing and escaping fix is internal to UserStore, CourseStore, and CsvRecord.

Deadline state:

- Admin saves the TA application deadline into applicationDeadline in ServletContext.
- Admin saves the MO course modification deadline into moCourseModifyDeadline in ServletContext.
- TA and MO pages read deadline status from ServletContext instead of directly reading Store files.
- AppContextListener initializes ServletContext on application startup by loading persisted deadline files from the deployed WEB-INF/file directory.
- Store remains responsible for persistence only; ServletContext is the runtime source for page status.

Review notification state:

- When an MO publishes review results, each affected TA application is marked as unread.
- A logged-in TA sees a red notification dot on the Personal Center link on the TA public page.
- In Personal Centre, courses with newly published review results also show a course-level red dot.
- Opening Personal Centre clears all unread review update flags for that TA.
- Guests and logged-out users do not see notification dots.

Annual reset state:

- Admin can run Reset Recruitment Cycle from the Admin Dashboard.
- The reset clears TA applications, generated application forms, review picks, review results, unread review dots, and both deadline files.
- The reset keeps Admin/MO/TA accounts, MO-owned courses, course names, and the previous recruitment text.
- All courses become Draft, so MOs can edit the preserved text and publish again for the new year.


5. Data Storage

Runtime data is stored under:

webapp/WEB-INF/file/

When deployed through start_SE.bat or start_se.sh, runtime data is copied to:

TOMCAT_HOME/webapps/SE/WEB-INF/file/

Important files:

- users.txt: TA, MO, and Admin accounts.
- courses.txt: course and recruitment information.
- deadline.txt: TA application deadline.
- mo-deadline.txt: MO course modification deadline.
- application-forms.txt: TA generated and submitted application forms.
- resume/: uploaded PDF resumes.

Runtime data note:

The currently running Tomcat application uses TOMCAT_HOME/webapps/SE/WEB-INF/file/, not the source webapp/WEB-INF/file/ directory. The deployment scripts preserve Tomcat runtime data by backing up and restoring this folder.

To reset the demo manually, clear these runtime files:

- users.txt
- courses.txt
- application-forms.txt
- deadline.txt
- mo-deadline.txt
- resume/

Optional files that can also be cleared:

- candidates.txt
- groq-transport.log

After clearing deadline.txt or mo-deadline.txt, restart Tomcat or redeploy so ServletContext is rebuilt from the cleared files.

For normal yearly reuse, prefer the Admin Dashboard's Reset Recruitment Cycle action instead of manually editing files. It clears application-cycle data while preserving accounts, course ownership, and MO draft text.

Recent persistence fix:

The project previously used simple comma splitting for users.txt and courses.txt. That could damage data when names, schools, skills, course names, or job descriptions contained commas.

Now the Store layer uses standard CSV escaping:

- A normal value is written directly.
- A value containing a comma is written inside quotes.
- A value containing quotes doubles the inner quotes.

Example:

Java, Python, SQL

is saved as:

"Java, Python, SQL"

and is loaded back as:

Java, Python, SQL

This keeps the text-file architecture but prevents comma-based field corruption.


6. Built-in Accounts

MO accounts:

- mo1@bupt.edu.cn / mo123456
- mo2@bupt.edu.cn / mo223456
- mo3@bupt.edu.cn / mo323456
- mo4@bupt.edu.cn / mo423456
- mo5@bupt.edu.cn / mo523456

Admin accounts:

- admin1@bupt.edu.cn / admin123456
- admin2@bupt.edu.cn / admin223456
- admin3@bupt.edu.cn / admin323456
- admin4@bupt.edu.cn / admin423456
- admin5@bupt.edu.cn / admin523456

TA accounts are registered from the TA login/register page with an email ending in @bupt.edu.cn.
After registration, the system generates an access key. The TA uses that access key to log in later.


7. Local Build and Test

Run all Maven tests from the project root:

mvn test

Recommended clean verification:

mvn clean test

Current verified result after the latest persistence fix:

- Tests run: 134
- Failures: 0
- Errors: 0

The local manual compile script can also be checked:

Windows:
cmd /c webapp\command2.bat

macOS/Linux:
cd webapp
./command2.sh


8. Local Deployment

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


9. Application URLs

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


10. User Workflows

TA workflow:

1. Open /SE/ta.
2. Check the public recruitment timeline, including TA application deadline, MO course information deadline, and current open/locked status.
3. Choose TA Login/Register.
4. Register with a @bupt.edu.cn email.
5. Save the generated TA access key.
6. Browse jobs.
7. Open a course detail page.
8. Upload a PDF resume if needed.
9. Generate a standard application form.
10. Review and submit the application form.
11. Use Personal Centre to view submitted courses, modify applications before the deadline, withdraw applications before the deadline, and check review results after publication.
12. If review results have just been published, check the red notification dot on Personal Center and on the updated course in Personal Centre.

MO workflow:

1. Open /SE/mo.
2. Log in with a built-in or Admin-created MO account.
3. Check the dashboard recruitment timeline, including TA application deadline, MO course information deadline, edit status, and review status.
4. Open Publish Recruitment or My Project.
5. Use My Project search if needed.
6. Use Back to Personal Centre or Back to Dashboard depending on where you want to return.
7. Select an assigned course.
8. Edit job title, description, and requirement.
9. Save draft or publish recruitment before the MO deadline.
10. After the TA application deadline passes, open Review Applications.
11. Select candidates.
12. Save picks or publish final review results.
13. Publishing final review results triggers unread result notifications for affected TAs.

Admin workflow:

1. Open /SE/admin.
2. Log in with a built-in Admin account.
3. Open MO Accounts to create MO accounts and assign courses.
4. Open Candidates to inspect registered TA users and applications.
5. Open TA Application Deadline to set when TA applications close.
6. Open MO Course Modification Deadline to set when MO editing and publishing close.
7. Open Reset Recruitment Cycle to start a new yearly recruitment cycle while preserving accounts and course draft text.


11. Testing Workflow Used During Recent Project Work

The recent validation workflow was:

1. Inspect project structure with rg and Maven configuration.
2. Run mvn test.
3. Run mvn clean test to force a clean compile and full unit test pass.
4. Run webapp\command2.bat to verify the non-Maven deployment compile path.
5. Start Tomcat with start_SE.bat.
6. Check that Tomcat listens on port 8081.
7. Request /SE/ta, /SE/mo, and /SE/admin.
8. Log in as Admin and request dashboard, MO management, candidate management, and deadline pages.
9. Log in as MO and request dashboard, my project, personal centre, and project detail pages.
10. Register a temporary TA, request TA job pages and personal centre, then clean up temporary test data.
11. Inspect Tomcat logs for deployment or JSP errors.
12. Review risks not covered by unit tests, especially JSP output escaping, file persistence, hard-coded deployment path assumptions, and platform-specific Groq transport.
13. Fix CSV persistence corruption while preserving the repository-store architecture.
14. Add regression tests for quoted CSV values in CourseStore and UserStore.
15. Add deadline timeline display to the TA public page and MO dashboard.
16. Keep deadline page state sourced from ServletContext and initialize it from AppContextListener on startup.
17. Add two MO My Project navigation buttons: Back to Personal Centre and Back to Dashboard.
18. Simplify Review Applications navigation to keep only Back to Personal Centre.
19. Remove the Course Fit field from generated TA application forms and AI prompts.
20. Restore TA review-result notification dots on the logged-in TA public page and Personal Centre.
21. Add Admin Reset Recruitment Cycle for yearly reuse.
22. Run mvn test again and confirm 134 tests pass.
23. Run webapp\command2.bat again and confirm deployment compilation still passes.


12. Known Limitations and Suggested Future Work

- JSP pages still output some values directly. For a production-grade system, use HTML escaping consistently.
- Passwords are stored in text files. For a real deployment, use password hashing and remove password display from Admin pages.
- Runtime file paths are configured from ServletContext real paths at application startup. The deployment scripts still use the SE context path for the coursework demo.
- Groq API transport currently uses PowerShell on Windows. A Java HttpClient implementation would be more portable.
- This is a prototype without a database and without full browser automation tests. The current JUnit suite is good for service, store, controller, and regression coverage, but manual UI smoke testing is still recommended before demonstrations.
- Notification dots depend on the TA session being refreshed from file storage. The current TA entry and TA controller paths refresh the logged-in TA object before rendering TA pages.


13. Quick Demo Checklist

Before a demo:

1. Run mvn test.
2. Run start_SE.bat.
3. Open http://localhost:8081/SE/ta.
4. Confirm /SE/mo and /SE/admin open.
5. Log in as admin1@bupt.edu.cn / admin123456.
6. Set and check both deadlines.
7. Log in as mo1@bupt.edu.cn / mo123456.
8. Confirm the dashboard timeline and assigned projects appear.
9. Register a TA with a @bupt.edu.cn email.
10. Confirm the TA public page timeline appears.
11. Submit or inspect a test application.
12. Publish a review result as MO and confirm the logged-in TA sees a red notification dot before opening Personal Centre.
13. Optionally run Admin Reset Recruitment Cycle and confirm courses return to Draft while course text remains.

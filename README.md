# BUPT-TA-Recruitment-Group33

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Servlet](https://img.shields.io/badge/Jakarta%20Servlet-6.0-blue)
![JSP](https://img.shields.io/badge/JSP-Views-green)
![Tomcat](https://img.shields.io/badge/Tomcat-11-yellow)
![Maven](https://img.shields.io/badge/Maven-Build%20%26%20Test-red)
![JUnit](https://img.shields.io/badge/JUnit-138%20Tests-brightgreen)
![Storage](https://img.shields.io/badge/Storage-Text%20Files-lightgrey)
![AI](https://img.shields.io/badge/AI-Groq%20Optional-purple)

BUPT Teaching Assistant Recruitment Platform

## 1. Project Summary

This project is a role-based teaching assistant recruitment system for BUPT International School.
It is built with Java Servlet, JSP, Maven, JUnit 5, Mockito, Apache PDFBox, and file-based persistence.

The system supports three independent entry pages and three roles:

- TA: view published jobs, register with a BUPT email, upload a profile resume, generate and submit application forms, manage submitted applications, withdraw applications, and view review results.
- MO: manage assigned course projects, edit and publish recruitment information before the MO deadline, review applications after the TA deadline, save picked applicants, and publish final results.
- Admin: manage MO accounts, assign courses, view candidates, set TA application deadlines, set MO course modification deadlines, and reset the yearly recruitment cycle.

The project intentionally uses text files instead of a database so that it is easy to deploy, inspect, reset, and demonstrate for coursework.


## 2. Software ZIP Contents

According to the coursework requirement, the software ZIP should include the following items:

- Source code
  - webapp/WEB-INF/src/ contains Java source code.
  - webapp/WEB-INF/views/ contains JSP pages.
  - webapp/css/ contains shared styling.
  - pom.xml defines Maven dependencies and test configuration.
- Test programs
  - src/test/java/ contains JUnit 5 and Mockito tests for controllers, services, repositories, stores, models, AI fallback behavior, and listener initialization.
  - test_programs/java/ is a submission copy of the same test source files, provided so the marker can find the test programs immediately after unzipping.
  - test_programs/pom.xml lets the marker run the tests directly from the test_programs folder.
  - test_programs/README.md explains how to inspect and run the tests.
- Code documentation
  - JavaDoc comments are included in the Java source files.
  - Generate JavaDocs with: mvn javadoc:javadoc
  - Generated entry page: target/reports/apidocs/index.html
- Readme
  - README.md explains setup, configuration, testing, JavaDocs, running, data reset, and demo workflows.


## 3. Technology Stack

- Java 17 source target, tested locally with JDK 21
- Jakarta Servlet API 6.0
- JSP pages under webapp/WEB-INF/views
- Apache Tomcat 11 for local deployment
- Maven for compilation, tests, and JavaDocs
- JUnit 5 and Mockito for tests
- Apache PDFBox for resume text extraction
- Optional Groq API integration for AI-generated TA application forms and MO course drafts


## 4. Main Project Structure

```text
.
|-- pom.xml
|-- README.md
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
```


## 5. Architecture

The current architecture is:

```text
JSP page -> Servlet Controller -> Service -> Repository -> Store -> text file
```

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


## 6. Runtime Data and Reset Instructions

Source template data files are located at:

```text
webapp/WEB-INF/file/
```

After deployment, Tomcat uses:

```text
TOMCAT_HOME/webapps/SE/WEB-INF/file/
```

### Important Runtime Files

- users.txt: TA, MO, and Admin accounts.
- courses.txt: course information, recruitment text, TA positions, draft/published status, picked applicants, and review status.
- deadline.txt: TA application deadline.
- mo-deadline.txt: MO course modification deadline.
- application-forms.txt: generated, saved, and submitted TA application forms.
- resume/: uploaded PDF resumes.

### Manual Reset for Testing

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

### CSV Persistence Note

The project previously split users.txt and courses.txt rows directly by commas, which could corrupt values such as Java, Python, SQL. The Store layer now uses standard CSV escaping:

- A value containing a comma is written inside quotes.
- A value containing double quotes escapes the inner quotes by doubling them.

Example:

```text
Java, Python, SQL
```

is saved as:

```text
"Java, Python, SQL"
```

```text
TA "Lead"
```

is saved as:

```text
"TA ""Lead"""
```

Files involved:

- webapp/WEB-INF/src/store/CsvRecord.java
- webapp/WEB-INF/src/store/CourseStore.java
- webapp/WEB-INF/src/store/UserStore.java
- src/test/java/store/CourseStoreTest.java
- src/test/java/store/UserStoreTest.java


## 7. Accounts

### Admin Accounts

- admin1@bupt.edu.cn / admin123456
- admin2@bupt.edu.cn / admin223456
- admin3@bupt.edu.cn / admin323456
- admin4@bupt.edu.cn / admin423456
- admin5@bupt.edu.cn / admin523456

### MO Accounts

MO accounts are created by Admin from the MO Accounts page. When Admin creates an MO, Admin assigns one or more courses. Existing course names are reused case-insensitively. New course names are created as Draft courses and can receive an AI-generated or fallback recruitment draft.

### TA Accounts

TA accounts are registered from the TA login/register page with an email ending in @bupt.edu.cn. After registration, the system generates a TA access key. The TA uses that access key to log in later.


## 8. Build and Test

Test program locations:

- Primary Maven test source directory: src/test/java/
- Submission-friendly copy: test_programs/java/
- Standalone test-program Maven file: test_programs/pom.xml
- Test program instructions: test_programs/README.md

The copy in test_programs/java/ is provided for the coursework requirement.
The tests can be run from the project root or directly from test_programs/.

### 8.1 Run Tests from the Project Root

How to run all tests from the project root:

1. Open a terminal in the project root.
2. Run:

```cmd
mvn test
```

Expected result:

- Tests run: 138
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

### 8.2 Run Tests from test_programs

How to run the submitted test programs from test_programs:

1. Open a terminal in the test_programs folder.
2. Run:

```cmd
mvn test
```

Alternative from the project root:

```cmd
cd test_programs
mvn test
```

Expected result:

- Tests run: 138
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

### 8.3 Clean Verification

Recommended clean verification:

1. Open a terminal in the project root.
2. Run:

```cmd
mvn clean test
```

Expected result:

- BUILD SUCCESS

### 8.4 Manual Compile Check

Manual compile/deployment compilation check:

#### Windows

1. Open a terminal in the project root.
2. Run:

```cmd
cmd /c webapp\command2.bat
```

Expected result:

- Compilation completed.

#### macOS/Linux

1. Open a terminal in the project root.
2. Run:

```bash
cd webapp
./command2.sh
```

Expected result:

- Compilation completed.


## 9. JavaDocs

JavaDocs are included in the Java source code and package-level documentation.

### 9.1 Generate JavaDocs

Generate JavaDocs:

1. Open a terminal in the project root.
2. Run:

```cmd
mvn javadoc:javadoc
```

Expected result:

- BUILD SUCCESS

Open the generated documentation:

```text
target/reports/apidocs/index.html
```

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


## 10. Optional AI Configuration

The system can use Groq API for two optional AI-assisted features:

- TA application form generation from a PDF resume and course requirements.
- Initial MO course recruitment draft generation when Admin creates a new course for an MO.

If no API key is configured, the system still works. It uses local fallback generation, so TA application and MO course creation workflows are not blocked.

### Platform Note

- The current Groq transport uses PowerShell. It works best on Windows.
- On macOS/Linux, real Groq generation requires PowerShell Core (`pwsh`).
- If the transport or API key is unavailable, the system automatically uses local fallback generation, so the application remains usable.

### 10.1 Environment Variables

Environment variables:

```text
GROQ_API_KEY=your_groq_api_key_here
GROQ_MODEL=llama-3.3-70b-versatile
```

GROQ_MODEL is optional. If it is not set, the default model is llama-3.3-70b-versatile.

### 10.2 Windows User-Level Setup

Windows user-level setup:

1. Open PowerShell.
2. Run:

```powershell
[Environment]::SetEnvironmentVariable("GROQ_API_KEY", "your_groq_api_key_here", "User")
[Environment]::SetEnvironmentVariable("GROQ_MODEL", "llama-3.3-70b-versatile", "User")
```

Then close and reopen the terminal or IDE that starts Tomcat, and restart Tomcat or rerun start_SE.bat.

### 10.3 Temporary PowerShell Setup

Temporary PowerShell setup:

1. Open PowerShell in the project root.
2. Run:

```powershell
$env:GROQ_API_KEY="your_groq_api_key_here"
$env:GROQ_MODEL="llama-3.3-70b-versatile"
.\start_SE.bat
```

### 10.4 macOS/Linux Temporary Setup

macOS/Linux temporary setup:

1. Open a terminal in the project root.
2. Run:

```bash
export GROQ_API_KEY="your_groq_api_key_here"
export GROQ_MODEL="llama-3.3-70b-versatile"
./start_se.sh
```

### 10.5 Persistent zsh Setup

Persistent zsh setup:

1. Open a terminal.
2. Run:

```bash
echo 'export GROQ_API_KEY="your_groq_api_key_here"' >> ~/.zshrc
echo 'export GROQ_MODEL="llama-3.3-70b-versatile"' >> ~/.zshrc
source ~/.zshrc
```

### 10.6 Persistent bash Setup

Persistent bash setup:

1. Open a terminal.
2. Run:

```bash
echo 'export GROQ_API_KEY="your_groq_api_key_here"' >> ~/.bashrc
echo 'export GROQ_MODEL="llama-3.3-70b-versatile"' >> ~/.bashrc
source ~/.bashrc
```

### 10.7 AI Setup Check

AI setup check:

1. Start or restart Tomcat after setting the environment variables.
2. Log in as a TA.
3. Upload a PDF resume.
4. Open a published course.
5. Click Generate Standard Form.

Expected result:

- With a valid API key, the generated form uses Groq-assisted content.
- Without a valid API key or transport support, the system uses fallback generation and the workflow still completes.

### Security Notes

- Do not commit a real API key to Git.
- Do not paste the API key into users.txt, courses.txt, JSP pages, or Java source files.
- Use environment variables instead.
- If a key is exposed, revoke it from the Groq dashboard and create a new one.


## 11. Local Deployment

### 11.1 Windows Startup

1. Install Tomcat 11.
2. Set TOMCAT_HOME or CATALINA_HOME to your own Tomcat folder. G:\Tomcat is only an example path; the program can run from C:, D:, or any other drive.
3. Open a terminal in the project root.
4. Run:

```cmd
start_SE.bat
```

The script will:

1. Find Tomcat.
2. Back up existing runtime data under webapps/SE/WEB-INF/file.
3. Copy webapp to TOMCAT_HOME/webapps/SE.
4. Restore runtime data.
5. Compile Java classes.
6. Start Tomcat.
7. Open http://localhost:8081/SE/ta.

### 11.2 macOS/Linux Startup

1. Install Tomcat 11.
2. Set TOMCAT_HOME or CATALINA_HOME to your own Tomcat folder. The project does not require a G drive.
3. Open a terminal in the project root.
4. If needed, make the script executable:

```bash
chmod +x start_se.sh
```

5. Run:

```bash
./start_se.sh
```

Expected result:

- Tomcat starts.
- The application is deployed under the SE context path.
- The TA entry page is available at http://localhost:8081/SE/ta.


## 12. Application URLs

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



## 13. User Workflows

### TA Workflow

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

### MO Workflow

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

### Admin Workflow

1. Open /SE/admin.
2. Log in with a built-in Admin account.
3. Open MO Accounts to create MO accounts and assign courses.
4. Open Candidates to inspect registered TA users and applications.
5. Open TA Application Deadline to set when TA applications close.
6. Open MO Course Modification Deadline to set when MO editing and publishing close.
7. Open Reset Recruitment Cycle to start a new yearly recruitment cycle while preserving accounts and course draft text.


## 14. Demonstration and Validation Checklist

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


## 15. Recent Validation Workflow

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


## 16. Known Limitations

- Passwords are stored in text files because this coursework prototype uses file-based persistence instead of a database.
- Runtime file paths are configured from ServletContext real paths at application startup. The deployment scripts deploy the application under the SE context path for the coursework demo.
- Groq API transport currently works best on Windows PowerShell. If the API key or transport is unavailable, fallback generation is used automatically.
- The current JUnit suite covers controller, service, repository, store, model, listener, and AI fallback logic. Manual UI smoke testing is still recommended before demonstrations.
- Notification dots depend on the TA session being refreshed from file storage. The current TA entry and TA controller paths refresh the logged-in TA object before rendering TA pages.


## 17. Group Name-List

- Github ID: qrsikno2         QMID: 190898878 (Support TA)
- Github ID: 21ZQH            QMID: 231222442 (Lead)
- Github ID: Aether0623       QMID: 231220839 (Member)
- Github ID: Anoyo36          QMID: 231220596 (Member)
- Github ID: Junwei-Lee-0713  QMID: 231221858 (Member)
- Github ID: sha7dow18        QMID: 231221294 (Member)
- Github ID: DLZDC            QMID: 231220046 (Member)
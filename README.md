# TA Recruitment System

![Coursework](https://img.shields.io/badge/Coursework-EBU6304-1f3b77)
![Stack](https://img.shields.io/badge/Stack-JSP%20%7C%20Servlet%20%7C%20JUnit-2f6bff)
![Architecture](https://img.shields.io/badge/Architecture-Role--Based-0f766e)
![Status](https://img.shields.io/badge/Status-Active%20Prototype-f59e0b)

A role-based recruitment platform for teaching assistant applications at **BUPT International School**.  
The system models the full workflow from course publishing and resume submission to deadline control, applicant review, and final result publishing.

![System Hero](./docs/images/ta-home.png)

## Overview

This project was developed for the **EBU6304 Software Engineering Group Project**.  
It is built with a classic **JSP/Servlet** architecture and designed around three clearly separated roles:

- **TA**: browse jobs, upload resumes, manage profile data, and track review results
- **MO**: create course projects, review applicants, and publish final decisions
- **Admin**: manage deadlines, candidate visibility, and system-level control

The project emphasizes:

- role-based access control
- deadline-aware interaction
- ID-based course persistence
- file-based storage without a database
- testable controller/store/model workflows

## Why This Project

Teaching assistant recruitment is a small system on the surface, but a complete one needs more than simple forms.
It needs:

- different permissions for different users
- course-linked applications instead of one global submission
- a staged review workflow
- persistent result tracking
- visible status feedback after publication

This project aims to turn that real process into a clear, demo-friendly web platform.

## Feature Breakdown

### TA Module

#### Job Discovery

- Browse available TA positions
- View detailed course and workload information
- Enter the application flow through dedicated job pages

#### Resume Submission

- Upload a resume for a specific course
- Re-upload the resume before the application deadline
- Keep application records linked to immutable course IDs

#### Personal Centre

- View all applied courses in one place
- Switch between applications through a structured course selector
- Track review progress after the application stage closes
- See final review results after publication
- Receive a notification dot when a review result is updated

#### Profile Management

- Update name and educational background
- Edit skills through a dedicated skill page
- Preserve profile data and skill selections across updates

#### TA Screens

<table>
  <tr>
    <td align="center" width="50%"><strong>Find Jobs</strong><br><img src="./docs/images/ta-findjob.png" alt="TA Find Job" height="300"></td>
    <td align="center" width="50%"><strong>Application</strong><br><img src="./docs/images/ta-application.png" alt="TA Application" height="300"></td>
  </tr>
  <tr>
    <td align="center" width="50%"><strong>Personal Centre</strong><br><img src="./docs/images/ta-personal-centre.png" alt="TA Personal Centre" height="300"></td>
    <td align="center" width="50%"><strong>Review Result</strong><br><img src="./docs/images/ta-review-result.png" alt="TA Review Result" height="300"></td>
  </tr>
  <tr>
    <td align="center" width="50%"><strong>Profile Centre</strong><br><img src="./docs/images/ta-profile-centre.png" alt="TA Profile Centre" height="300"></td>
    <td align="center" width="50%"><strong>Skill Editing</strong><br><img src="./docs/images/ta-profile-skill.png" alt="TA Skill Editing" height="300"></td>
  </tr>
</table>

### MO Module

#### Project Creation

- Create new recruitment posts for courses
- Assign immutable course IDs
- Persist course information to local storage

#### Project Management

- View owned projects only
- Edit course information before the MO modification deadline
- Lock editing after the deadline with disabled controls and modal feedback

#### Review Workflow

- Review applicants course by course
- Open uploaded resumes directly from the review page
- Save picked applicants before publishing
- Publish final results to update TA status

#### MO Profile and Control

- Edit MO personal information through a profile page
- Restrict creation and editing when profile data is incomplete
- Restrict protected routes through servlet-level session checks

#### MO Screens

<table>
  <tr>
    <td align="center" width="50%"><strong>Dashboard</strong><br><img src="./docs/images/mo-dashboard.png" alt="MO Dashboard" height="300"></td>
    <td align="center" width="50%"><strong>Profile</strong><br><img src="./docs/images/mo-profie.png" alt="MO Profile" height="300"></td>
  </tr>
  <tr>
    <td align="center" width="50%"><strong>Create Project</strong><br><img src="./docs/images/mo-create-project.png" alt="MO Create Project" height="300"></td>
    <td align="center" width="50%"><strong>Project Detail</strong><br><img src="./docs/images/mo-project-detail.png" alt="MO Project Detail" height="300"></td>
  </tr>
</table>

### Admin Module

#### Deadline Management

- Set the TA application deadline
- Set the MO course modification deadline
- Persist deadlines and reload them when the server starts

#### Candidate and Workload Visibility

- View candidate information from the admin side
- Open uploaded resumes directly from the admin workflow
- Monitor recruitment-related information through a centralized interface

#### Admin Screens

<table>
  <tr>
    <td align="center" width="50%"><strong>Dashboard</strong><br><img src="./docs/images/admin-dashboard.png" alt="Admin Dashboard" height="300"></td>
    <td align="center" width="50%"><strong>Candidate / Workload</strong><br><img src="./docs/images/admin-workload.png" alt="Admin Workload" height="300"></td>
  </tr>
  <tr>
    <td align="center" width="50%"><strong>TA Deadline</strong><br><img src="./docs/images/admin-application-deadline.png" alt="Admin Application Deadline" height="300"></td>
    <td align="center" width="50%"><strong>MO Deadline</strong><br><img src="./docs/images/admin-mo-deadline.png" alt="Admin MO Deadline" height="300"></td>
  </tr>
</table>

## Project Structure

```text
.
|-- docs/                         Documentation and project assets
|   `-- images/                   README screenshots
|-- src/
|   `-- test/java/                JUnit test sources
|-- webapp/
|   |-- images/                   Static assets
|   |-- WEB-INF/
|   |   |-- file/                 Local data files
|   |   |-- src/
|   |   |   |-- controller/       Servlet controllers
|   |   |   |-- listener/         Application startup logic
|   |   |   |-- model/            Domain models
|   |   |   `-- store/            File-based persistence
|   |   |-- views/
|   |   |   |-- admin/            Admin JSP pages
|   |   |   |-- mo/               MO JSP pages
|   |   |   `-- ta/               TA JSP pages
|   |   `-- web.xml               Servlet mappings
|   |-- command2.bat              Local compile helper
|   `-- start.html                Login and registration entry
|-- pom.xml                       Maven test configuration
|-- start_SE.bat                  Deployment helper
`-- README.md
```

## Highlights

- **Role-based workflow**
  TA, MO, and Admin each have dedicated routes, UI flows, and responsibilities.

- **Deadline-aware behavior**
  Application, review, and modification actions react to configured deadlines both in the UI and in controller-level checks.

- **ID-based persistence**
  Courses use immutable IDs, making resume mappings and review publishing more reliable.

- **Publish-first review model**
  MO can save picks first and publish final decisions later.

- **Notification logic for applicants**
  TA users receive visible unread result updates after MO publishes review outcomes.

- **Profile completeness gate**
  MO users must complete required personal information before creating or modifying projects.

- **File-based storage**
  Users, deadlines, courses, and resume mappings are stored locally without requiring a database.

- **JUnit-backed validation**
  Controllers, stores, and models are covered with tests for major user journeys and regression-prone logic.

## Advantages

- Clear separation of responsibilities across three user roles
- Easy to demonstrate in a coursework setting
- Strong linkage between UI behavior and backend rules
- Stable persistence model for course-based applications
- Good foundation for explaining sessions, deadlines, testing, and state transitions

## Quick Start

### Requirements

- Java
- Tomcat
- Maven

### Run Tests

```bash
mvn test
```

### Start the System

Use your Tomcat deployment flow and open:

```text
http://localhost:8081/SE/start.html
```

### Local Notes

- The project uses file-based runtime data under `webapp/WEB-INF/file`
- Tomcat deployment is driven by the repository's existing batch workflow
- README screenshots are stored under `docs/images`

## Testing

The project includes JUnit-based testing for controller, model, and store layers.
Covered areas include:

- account registration and login
- TA application and resume upload
- MO review and publish flow
- deadline restrictions
- admin resume access and deadline setting
- persistence format compatibility for TA and MO records

## Group Name List

- Github ID: qrsikno2       QMID: 190898878 (Support TA)
- Github ID: ZQH-21         QMID: 231222442 (Lead)(now currently be suspended, creating 21ZQH)
- Github ID: 21ZQH          QMID: 231222442(Lead)
- Github ID: Aether0623     QMID: 231220839 (Member)
- Github ID: DLZDC          QMID: 231220046(Member)
- Github ID: Anoyo36        QMID: 231220596(Member)
- Github ID: sha7dow18      QMID: 231221294(Member)
- Github ID: Junwei-Lee-0713  QMID: 231221858(Member)
- Info of others

# TA Recruitment System

![Coursework](https://img.shields.io/badge/Coursework-EBU6304-1f3b77)
![Stack](https://img.shields.io/badge/Stack-JSP%20%7C%20Servlet%20%7C%20JUnit-2f6bff)
![Architecture](https://img.shields.io/badge/Architecture-Role--Based-0f766e)
![Status](https://img.shields.io/badge/Status-Active%20Prototype-f59e0b)

A role-based web platform for teaching assistant recruitment at BUPT International School.
The system covers the full workflow from TA application and resume upload to MO review, deadline control, and final result publishing.

> Recommended hero image:
> `docs/images/hero-overview.png`
>
> Best choice:
> a clean full-page screenshot or a 3-panel collage showing `TA / MO / Admin`.

## Overview

This project was developed for the **EBU6304 Software Engineering Group Project**.
It is built with a classic **JSP/Servlet** architecture and focuses on a realistic recruitment workflow involving three different roles:

- **TA** for job browsing, resume submission, and result tracking
- **MO** for course publishing, applicant review, and result publishing
- **Admin** for deadline management and system-level control

The project is designed around **role separation**, **deadline-aware interaction**, and **file-based persistence** without relying on a database.

## Why This Project

Teaching assistant recruitment is not just a simple form submission process.
It requires:

- clear role-based permissions
- deadline-aware behavior
- application tracking per course
- a review and publishing workflow
- stable persistence between users, courses, and uploaded resumes

This system aims to model that complete lifecycle in a way that is practical, testable, and easy to demonstrate.

## Feature Breakdown

### TA Module

#### Job Discovery

- Browse available TA openings
- View course details and application information
- Enter the application flow from a guided interface

#### Resume Submission

- Upload a resume for a specific course
- Re-upload the resume before the application deadline
- Keep course-linked application records with stable course IDs

#### Personal Centre

- View all applied courses in one place
- Switch between applications using a structured course selector
- Track progress after the application stage closes
- See final review results after MO publishes them
- Receive a notification dot when a review result has been updated

#### Profile Management

- Update personal information
- Edit educational background
- Maintain skill information through a dedicated skill editing flow

> Recommended screenshots for this section:
> `docs/images/ta-home.png`
> `docs/images/ta-personal-centre.png`
> `docs/images/ta-profile-centre.png`
> `docs/images/ta-profile-skill.png`
> `docs/images/ta-application.png`

### MO Module

#### Project Creation

- Create new course recruitment posts
- Assign immutable course IDs
- Save course information to local storage

#### Project Management

- View owned projects only
- Edit course information before the MO modification deadline
- Lock editing after the deadline with disabled controls and modal feedback

#### Review Workflow

- Review applicants course by course
- Open uploaded resumes directly from the review page
- Save picked applicants before publishing
- Publish final review results to update TA application status

#### Access Control

- Prevent review before the TA application stage ends
- Prevent course modification after the MO deadline
- Restrict MO routes through servlet-level session checks

> Recommended screenshots for this section:
> `docs/images/mo-dashboard.png`
> `docs/images/mo-create-project.png`
> `docs/images/mo-project-detail.png`
> `docs/images/mo-review.png`

### Admin Module

#### Deadline Management

- Set the TA application deadline
- Set the MO course modification deadline
- Persist deadlines to disk and restore them when the server starts

#### Administrative Control

- Access the admin dashboard through a dedicated role-based entry
- Manage recruitment settings through a centralized interface
- Maintain system timing rules that drive TA and MO behavior

> Recommended screenshots for this section:
> `docs/images/admin-dashboard.png`
> `docs/images/admin-application-deadline.png`
> `docs/images/admin-mo-deadline.png`

## Project Structure

```text
.
|-- docs/                         Documentation and project assets
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
  Separate TA, MO, and Admin experiences with dedicated routes and responsibilities.

- **Deadline-aware system behavior**
  Application, review, and modification flows change automatically based on configured deadlines.

- **ID-based course persistence**
  Course records use immutable IDs, making review, application, and resume mapping more stable.

- **Publish-first review design**
  MO can save a review selection first and publish final results later.

- **Notification logic for TA users**
  Review result updates surface through unread notifications in the TA interface.

- **File-based persistence**
  User data, course data, deadlines, and resume mappings are stored locally without a database dependency.

- **Test coverage for core flows**
  Controllers, stores, and models are covered with JUnit tests for major user actions and edge cases.

## Screenshots

Use this section as the final polished gallery after you collect screenshots.

### Entry Flow

> Place screenshot here:
> `docs/images/start-page.png`

### TA Experience

> Place screenshots here:
> `docs/images/ta-home.png`
> `docs/images/ta-personal-centre.png`

### MO Experience

> Place screenshots here:
> `docs/images/mo-dashboard.png`
> `docs/images/mo-review.png`

### Admin Experience

> Place screenshots here:
> `docs/images/admin-dashboard.png`
> `docs/images/admin-application-deadline.png`

## Advantages

- Clean separation of responsibilities across three user roles
- Simple deployment model for demonstration and coursework delivery
- Strong workflow visibility for both applicants and reviewers
- Easy-to-demo UI states driven by deadlines and review results
- Practical architecture for explaining session handling, persistence, and testing in a course setting

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

## Testing

The project includes JUnit-based testing for controller, model, and store layers.
Covered areas include:

- account registration and login
- TA resume upload and personal centre behavior
- MO review and publish flow
- deadline restrictions
- persistence format handling

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

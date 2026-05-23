# Controller and Service Outline

This document describes the current responsibility split in the TA Recruitment
system. It focuses on what each controller action does and which services or
repositories it calls.

## Current Layering

The intended layering after the recent refactor is:

```text
Controller -> Service -> Repository -> Store -> txt files
```

`UserStore`, `CourseStore`, `DeadlineStore`, and `ApplicationFormStore` still
exist. They are now mostly hidden behind repository interfaces. Service classes
should depend on repositories instead of calling stores directly.

Current repository wrappers:

- `UserRepository` -> `TxtUserRepositoryImpl` -> `UserStore`
- `CourseRepository` -> `TxtCourseRepositoryImpl` -> `CourseStore`
- `DeadlineRepository` -> `TxtDeadlineRepositoryImpl` -> `DeadlineStore`
- `ApplicationFormRepository` -> `TxtApplicationFormRepositoryImpl` -> `ApplicationFormStore`

At the time of writing, the main workflows go through controller -> service ->
repository -> store. Some legacy compatibility paths and startup/reset helpers
still touch store classes directly; those are good candidates for a later
cleanup pass.

## Controllers

### `AccountController`

Role: handles login and registration form submissions for TA, MO, Admin, and
legacy generic login/register actions.

Dependencies:

- `AccountService`

Actions:

- `RegisterTA`
  - Reads TA email.
  - Calls `AccountService.registerTaWithBuptEmail(null, email)`.
  - Rejects non-`@bupt.edu.cn` email addresses.
  - Rejects duplicate email addresses.
  - Stores authenticated TA in session.
  - Shows `/WEB-INF/views/entry/ta-key.jsp` with the generated access key.

- `LoginTA`
  - Reads TA access key from the password field.
  - Calls `AccountService.loginTaByAccessKey(accessKey)`.
  - On success, stores user in session and redirects to `/ta`.
  - On failure, redirects to `/ta?action=auth` with an error.

- `LoginMo`
  - Reads MO account and password.
  - Calls `AccountService.loginBuiltInMo(email, password)`.
  - The method name is retained for compatibility, but MO accounts are created
    by Admin rather than seeded automatically.
  - On success, stores user in session and redirects to MO dashboard.
  - On failure, redirects to `/mo` with an error.

- `LoginAdmin`
  - Reads Admin account and password.
  - Calls `AccountService.loginBuiltInAdmin(email, password)`.
  - On success, stores user in session and redirects to Admin dashboard.
  - On failure, redirects to `/admin` with an error.

- `Register`
  - Legacy generic registration.
  - Only allows role `TA`.
  - Calls `AccountService.register(name, password, role, email)`.
  - Stores the user in session and redirects by role.

- `Login`
  - Legacy generic login.
  - Calls `AccountService.login(password, role, email)`.
  - Stores the user in session and redirects by role.

### `EntryController`

Role: serves the three public entry pages and the TA public job list.

Dependencies:

- `AccountService`
- `CourseService`

Actions / routes:

- `/ta`
  - Calls `AccountService.ensureBuiltInAccounts()`.
  - Loads courses through `CourseService.getCourseList()`.
  - Filters to courses where recruitment has been published.
  - Sets deadline-related display attributes.
  - If `action=auth`, forwards to the TA login/register page.
  - Otherwise forwards to the TA job/home page.

- `/mo`
  - Calls `AccountService.ensureBuiltInAccounts()`.
  - Shows MO login entry.

- `/admin`
  - Calls `AccountService.ensureBuiltInAccounts()`.
  - Shows Admin login entry.

Current direct store use:

- Reads application deadline through `DeadlineStore.getDeadline()`.
  - This could be moved to `DeadlineService`.

### `TAClassController`

Role: handles TA-only authenticated actions after login, including job detail,
resume upload, AI form generation, form submission, and personal centre.

Dependencies:

- `ApplicationFormService`
- `TAApplicationService`
- `CourseService`
- `DeadlineService`

GET actions:

- `logout`
  - Invalidates session.
  - Redirects to `/ta`.

- `home`
  - Redirects to `/ta`.

- `view_information`
  - Legacy compatibility action.
  - Currently redirects to `/ta`.

- `show_all_information`
  - Resolves selected course through `TAApplicationService.getCourseByIndex(...)`.
  - Sets `selectedCourse`, `courseIndex`, and `applicationOpen`.
  - If the TA has already submitted for this course, the page shows Modify and Withdraw controls.
  - Forwards to `/WEB-INF/views/ta/specific-class.jsp`.

- `go_apply`
  - Resolves selected course by course index.
  - Checks application deadline through `DeadlineService`.
  - If closed, forwards to personal centre with an error.
  - Calls `TAApplicationService.prepareCurrentApplicationData(...)`.
  - Forwards to `/WEB-INF/views/ta/application.jsp`.

- `go_apply_by_id`
  - Resolves course using `TAApplicationService.getCourseById(...)`.
  - Resolves index using `TAApplicationService.findCourseIndexById(...)`.
  - Checks deadline.
  - Forwards to application page or personal centre depending on state.

- `generate_application_form`
  - Resolves selected course.
  - Checks deadline.
  - Calls `TAApplicationService.validateApplicationLimit(ta, course)`.
  - Blocks starting a fourth different course application.
  - Checks master resume through `TAApplicationService.hasMasterResume(...)`.
  - Calls `ApplicationFormService.generateInitialForm(ta, course)`.
  - Forwards to `/WEB-INF/views/ta/application-form.jsp`.

- `edit_application_form`
  - Resolves course by course id.
  - Checks deadline.
  - Calls `ApplicationFormService.getForm(taEmail, courseId)`.
  - If no form exists, calls `ApplicationFormService.generateInitialForm(...)`.
  - Forwards to application form page.

- `view_master_resume`
  - Gets master resume file through `TAApplicationService.getMasterResumeFile(...)`.
  - Streams the PDF inline or as attachment.

- `personal_centre`
  - Calls `TAApplicationService.preparePersonalCentreData(...)`.
  - Sets applied courses, selected course, selected status, deadline state, and resume state.
  - Sets `applicationCount` and `applicationLimit` for the applications badge.
  - Forwards to `/WEB-INF/views/ta/personalCentre.jsp`.

POST actions:

- `logout`
  - Invalidates session.
  - Redirects to `/ta`.

- `upload_resume`
  - Used when a TA starts applying and does not yet have a master resume.
  - Checks deadline.
  - Calls `TAApplicationService.validateApplicationLimit(ta, course)`.
  - Blocks first-time generation for a fourth different course application.
  - Rejects replacing an existing master resume from this page.
  - Calls `TAApplicationService.uploadMasterResume(...)` when a PDF is submitted.
  - Calls `ApplicationFormService.generateInitialForm(...)` after resume upload.
  - Forwards to application form page.

- `upload_master_resume`
  - Used from personal centre to upload or replace the global master resume.
  - Calls `TAApplicationService.uploadMasterResume(...)`.
  - Forwards back to personal centre.

- `save_application_form`
  - Builds form through `ApplicationFormService.buildFormFromRequest(..., submitted=false)`.
  - Saves through `ApplicationFormService.saveForm(form)`.
  - Returns to form page with a saved message.
  - This backend action is retained for compatibility, but the visible Save Draft button has been removed.

- `submit_application_form`
  - Builds form through `ApplicationFormService.buildFormFromRequest(..., submitted=true)`.
  - Saves through `ApplicationFormService.saveForm(form)`.
  - Calls `TAApplicationService.submitApplicationForm(ta, course, form)`.
  - The service rejects a fourth different course application as a backend safety check.
  - Updates session user.
  - Forwards to course detail page with a success message.

- `withdraw_application`
  - Checks course id and deadline.
  - Resolves course by id.
  - Calls `TAApplicationService.withdrawApplication(ta, course)`.
  - If submitted from job detail with `returnTo=course_detail`, forwards back to the course detail page.
  - Otherwise forwards to personal centre.

### `MOClassController`

Role: handles MO authenticated actions, including publishing course recruitment,
editing owned course information, profile editing, and reviewing TA submitted
forms.

Dependencies:

- `ApplicationReviewService`
- `UserRepository`

GET actions:

- `logout`
  - Invalidates session.
  - Redirects to `/mo`.

- `dashboard`
  - Sets whether MO profile is complete.
  - Sets whether MO course modification is open.
  - Forwards to `/WEB-INF/views/mo/dashboard.jsp`.

- `create_class`
  - Checks MO modification deadline.
  - Checks MO profile completeness.
  - Loads current MO owned course list.
  - Forwards to `/WEB-INF/views/mo/create-project.jsp`.

- `personal_center`
  - Sets whether review stage is open.
  - Forwards to `/WEB-INF/views/mo/personal-center.jsp`.

- `profile_center`
  - Forwards to `/WEB-INF/views/mo/profile-center.jsp`.

- `review_candidates`
  - Checks review stage deadline.
  - Resolves selected owned course.
  - Calls `ApplicationReviewService.getSubmittedFormsByApplicantEmail(course)`.
  - Forwards to `/WEB-INF/views/mo/review.jsp`.

- `my_project`
  - Loads current MO owned course list.
  - Forwards to `/WEB-INF/views/mo/my-project.jsp`.

- `project_detail`
  - Resolves owned course by course index.
  - Sets course detail attributes.
  - Forwards to `/WEB-INF/views/mo/project-detail.jsp`.

POST actions:

- `logout`
  - Invalidates session.
  - Redirects to `/mo`.

- `publish_course`
  - Checks MO modification deadline.
  - Checks profile completeness.
  - Resolves assigned course from MO owned courses.
  - Requires a positive `TA Positions` value.
  - Builds an updated `Course`, stores TA Positions, and marks recruitment as published.
  - Updates current MO session course copy.
  - Redirects to project detail.

- `update_published`
  - Checks MO modification deadline.
  - Checks profile completeness.
  - Resolves selected owned course.
  - Builds updated `Course` with new job title, TA Positions, hours,
    description, and requirement.
  - If TA Positions is left blank, the existing value is preserved.
  - Keeps recruitment marked as published.
  - Updates current MO session course copy.
  - Redirects to project detail.

- `save_course_changes`
  - Kept as a compatibility alias for `update_published`.

- `save_personal_information`
  - Updates MO name, degree, and college.
  - Calls `UserRepository.updateMoProfile(mo)`.
  - Stores updated MO in session.
  - Forwards to profile centre.

- `save_review_picks`
  - Checks review stage deadline.
  - Resolves course for review.
  - Calls `ApplicationReviewService.saveReviewPicks(course, pickedEmails)`.
  - Saves a shortlist only; this action can exceed TA Positions.
  - Saved picks are rendered first on the review page for follow-up review.
  - Syncs current MO course copy.
  - Redirects back to review page.

- `publish_review`
  - Checks review stage deadline.
  - Resolves course for review.
  - Calls `ApplicationReviewService.publishReview(course, pickedEmails)`.
  - Enforces TA Positions so final approved candidates cannot exceed the limit.
  - Syncs current MO course copy.
  - Redirects back to review page.

Current direct store use:

- Course updates flow through `MOProjectService`, `CourseService`, `CourseRepository`, then `CourseStore`.
- Uses `CourseStore.getCourseList()` when refreshing MO owned courses.
- Uses `DeadlineStore.getMoModifyDeadline()` and `DeadlineStore.getDeadline()`.

These should eventually move behind `CourseService`, `DeadlineService`, or
`MOProjectService`.

### `AdminController`

Role: handles Admin authenticated actions, including MO management, candidate
management, and deadline settings.

Dependencies:

- `UserRepository`

GET actions:

- `logout`
  - Invalidates session.
  - Redirects to `/admin`.

- `dashboard`
  - Forwards to `/WEB-INF/views/admin/dashboard.jsp`.

- `manage_mo`
  - Calls `UserRepository.getMOList()`.
  - Loads course list for display.
  - Forwards to `/WEB-INF/views/admin/mo-management.jsp`.

- `candidate_management`
  - Calls `UserRepository.getTAList()`.
  - Forwards to `/WEB-INF/views/admin/candidate-management.jsp`.

- `view_resume`
  - Legacy PDF resume streaming action.
  - Resolves resume file by applicant email and course id.
  - Streams PDF inline.

- `set_deadline`
  - Shows TA application deadline page.

- `set_mo_deadline`
  - Shows MO course modification deadline page.

POST actions:

- `logout`
  - Invalidates session.
  - Redirects to `/admin`.

- `save_deadline`
  - Parses deadline date and time.
  - Saves TA application deadline.
  - Stores deadline in servlet context.
  - Forwards back to deadline page.

- `save_mo_deadline`
  - Parses deadline date and time.
  - Saves MO modification deadline.
  - Stores deadline in servlet context.
  - Forwards back to deadline page.

- `create_mo`
  - Reads MO name, account, password, degree, college, and assigned courses.
  - Checks duplicate account through `UserRepository.isEmailRegistered(account)`.
  - Finds or creates assigned courses.
  - Creates `Mo`, assigns owned courses, and calls `UserRepository.saveUser(mo)`.
  - Forwards back to MO management page.

Current direct store use:

- Uses `CourseStore.getCourseList()` and `CourseStore.saveCourse(...)`.
- Uses `DeadlineStore.saveDeadline(...)` and `DeadlineStore.saveMoModifyDeadline(...)`.

These should eventually move behind `CourseService` and `DeadlineService`.

## Services

### `AccountService` / `AccountServiceImpl`

Role: account registration, login, and built-in account initialization.

Dependencies:

- `UserRepository`
- `CourseRepository`

Responsibilities:

- Check whether an email/account is registered.
- Save users.
- Validate users by password, role, and email/account.
- Register TA users.
- Enforce `@bupt.edu.cn` domain for TA registration.
- Generate TA access keys.
- Login TA by access key.
- Login MO/Admin accounts.
- Ensure built-in Admin accounts exist.
- MO accounts and assigned courses are created by Admin.

### `CourseService` / `CourseServiceImpl`

Role: thin service for course persistence.

Dependencies:

- `CourseRepository`

Responsibilities:

- Get the full course list.
- Save a new course.
- Update an existing course.

### `DeadlineService` / `DeadlineServiceImpl`

Role: deadline parsing and deadline state rules.

Dependencies:

- `DeadlineRepository`

Responsibilities:

- Get and save TA application deadline.
- Get and save MO course modification deadline.
- Parse deadline date and time from form fields.
- Decide whether TA application is still open.
- Decide whether MO review stage is open.
- Decide whether MO course modification is still open.

### `ApplicationFormService` / `ApplicationFormServiceImpl`

Role: generate, load, build, and save TA standard application forms.

Dependencies:

- `ApplicationFormRepository`
- `ApplicationFormAiClient`
- `ResumeTextExtractor`
- fallback `MockApplicationFormAiClient`

Responsibilities:

- Extract text from TA master resume.
- Generate an initial application form from resume + course.
- Call Groq AI through `ApplicationFormAiClient`.
- Fall back to local mock generation if Groq fails.
- Load an existing form by TA email and course id.
- Build a form from submitted request fields.
- Preserve `courseFit` from the generated form.
- Save draft or submitted forms.

### `ApplicationReviewService` / `ApplicationReviewServiceImpl`

Role: MO review workflow for submitted application forms.

Dependencies:

- `ApplicationFormRepository`
- `CourseRepository`
- `UserRepository`

Responsibilities:

- Load submitted forms for a course.
- Exclude draft forms from MO review.
- Save MO picked applicant emails.
- Allow saved review picks to act as a shortlist, even above the final approval quota.
- Publish review results.
- Reject review publication when the number of picked applicants exceeds TA Positions.
- Mark picked applicants as approved.
- Mark unpicked submitted applicants as rejected.
- Mark review result as unread for TA.
- Persist course review state.
- Persist updated TA application status.

### `TAApplicationService` / `TAApplicationServiceImpl`

Role: TA application workflow helpers and state changes.

Dependencies:

- `UserProfileService`
- `ResumeStorageService`

Responsibilities:

- Resolve course by index.
- Resolve course by id.
- Resolve course index by id.
- Enforce the maximum of 3 different course applications per TA.
- Allow resubmitting or modifying an already-applied course even when the TA is at the limit.
- Expose current application count and application limit for the personal centre UI.
- Check whether TA has a current submitted application.
- Check whether TA has a master resume.
- Get master resume file and stable file name.
- Prepare application page resume/form status attributes.
- Upload and persist master resume.
- Submit application form by linking TA, course, and application form id.
- Validate the application limit again during submit so direct POST requests cannot bypass the rule.
- Withdraw an application.
- Prepare personal centre data.
- Mark unread review updates as read.
- Update TA profile data for legacy flows.

### `ResumeStorageService` / `ResumeStorageServiceImpl`

Role: PDF resume file storage.

Dependencies:

- File system only.

Responsibilities:

- Build stable resume file names from TA email.
- Resolve upload directory.
- Build resume file paths.
- Get TA course resume file.
- Get TA master resume file.
- Get applicant resume file.
- Store course-specific resume.
- Store global master resume.
- Delete stored resume files.

### `UserProfileService` / `UserProfileServiceImpl`

Role: user profile persistence facade.

Dependencies:

- `UserRepository`

Responsibilities:

- Get all TA users.
- Persist TA applied course ids.
- Persist TA profile.
- Persist MO profile.
- Persist MO owned course ids.

### `MOProjectService` / `MOProjectServiceImpl`

Role: MO project/course workflow service. This service exists, but the current
`MOClassController` has not fully migrated to it yet.

Dependencies:

- `CourseService`
- `UserProfileService`

Responsibilities:

- Publish a new course recruitment with a manually configured TA Positions value.
- Update course recruitment information.
- Update MO profile.
- Refresh MO owned courses from current course list.
- Sync an updated course into MO owned courses.
- Resolve course by index.
- Resolve review course index.
- Resolve valid picked applicant emails.
- Save review picks.
- Publish review result while enforcing the TA Positions approval limit.
- Update applicant statuses during review publication.

## AI Services

### `ApplicationFormAiClient`

Interface for AI-based application form generation.

### `GroqApplicationFormAiClient`

Real Groq implementation.

Responsibilities:

- Build prompt from TA, course, and resume text.
- Call Groq chat completions API.
- Parse the model output into `ApplicationForm`.
- Uses the current PowerShell transport strategy.
- On macOS/Linux, real Groq generation depends on PowerShell Core or a future
  Java HTTP client implementation. If the transport is unavailable, the system
  falls back locally.

### `MockApplicationFormAiClient`

Local fallback implementation.

Responsibilities:

- Generate a form without network access.
- Infer education, skills, experience, and project content from resume text.
- Provide local feedback when Groq generation is unavailable.

### `ResumeTextExtractor`

Interface for extracting text from resume files.

### `PdfBoxResumeTextExtractor`

Primary mature PDF text extraction implementation using PDFBox.

### `SimplePdfResumeTextExtractor`

Older/simple PDF text extraction implementation retained as an alternative.

## Main Remaining Coupling

The service layer is mostly clean from direct store calls. Remaining coupling is
mainly in legacy compatibility paths and application startup/reset helpers.

Recommended next refactor:

1. Replace the PowerShell Groq transport with Java `HttpClient` for full cross-platform AI support.
2. Add consistent HTML escaping to JSP output.
3. Replace text-file persistence with a database if the system is expanded beyond coursework/demo use.

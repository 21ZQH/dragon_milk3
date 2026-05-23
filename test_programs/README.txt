Test Programs
=============

This folder is provided as a clear submission entry point for the coursework
requirement "Test programs".

The test source files are copied from the Maven standard test directory:

Original location:
  src/test/java/

Submission copy:
  test_programs/java/

Important:
  This folder includes its own pom.xml, so the tests can be run either from
  this test_programs folder or from the project root. The test_programs pom.xml
  points back to ../webapp/WEB-INF/src for the application source code.

How to run all tests from this folder:

  1. Open a terminal in the test_programs folder.
  2. Run:

       mvn test

Alternative from the project root:

       mvn test

Expected result:

  Tests run: 138, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS

Test coverage overview:

  controller/   Servlet controller workflow tests
  listener/     ServletContext initialization tests
  model/        Model behavior tests
  repository/   Text repository tests
  service/      Business service tests
  service/ai/   AI fallback and resume extraction tests
  store/        File-store and CSV persistence tests
  testsupport/  Shared utilities for isolated store tests

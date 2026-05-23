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
  The files in this folder are a copy for easy inspection. The tests should be
  executed from the project root, because Maven needs pom.xml and the project
  dependencies.

How to run all tests:

  1. Open a terminal in the project root folder.
  2. Run:

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

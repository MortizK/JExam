# Contributor Workflow

This page describes a practical development loop for contributors.

## Fast Loop

1. Make a small source change.
2. Run tests.
3. Check coverage and report impact.
4. Update wiki notes if behavior or architecture changed.

## Commands

    mvn test
    mvn package
    mvn verify

## Reports To Check

- `target/reports/surefire.html`
- `target/site/jacoco/index.html`
- `target/surefire-reports/*.xml`

## Change Strategy

- Keep orchestration in `ExamApplicationService`.
- Keep UI components focused on rendering and interaction.
- Keep persistence logic in `com.jexam.io`.
- Keep generation logic in `com.jexam.generation`.

## Suggested Test Targets By Change Type

- Validation rule change: `ExamValidatorTest`
- XML structure change: `ExamPersistenceServiceTest`, `ExamXmlRoundTripTest`
- Generation logic change: `PdfBoxGenerationServiceTest`, relevant `ExamApplicationServiceTest`
- End-to-end behavior: `EndToEndWorkflowTest`

## Review Checklist

- Do tests still pass locally?
- Did coverage change in expected packages?
- Did any exception messages or user-facing dialogs change?
- Does the wiki need an update for new workflows or responsibilities?

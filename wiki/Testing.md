# Testing

The current test suite is small but organized along the same boundaries as the main code.

## Test Layout

- `com.jexam.validation` tests validation rules.
- `com.jexam.app` tests application service behavior.
- `com.jexam.io` tests XML persistence and XML round-tripping.
- `com.jexam.generation` tests PDF generation.
- `com.jexam.workflow` contains end-to-end workflow coverage.
- `TestFixtures` holds shared test data and helpers.

## Current Surefire Snapshot

- Tests: 65
- Failures: 0
- Errors: 0
- Skipped: 0
- Success rate: 100%

## What The Tests Cover Well

- Validation of exam, chapter, task, and variant structure
- XML load and save behavior
- PDF generation paths
- Application service orchestration
- Selection model boundary behavior (`JExamSelectionModel`)
- Validation result/error aggregation and immutability behavior
- UI language enum labeling and lookup behavior
- Stub PDF generation failure contract (`UnsupportedOperationException`)
- Model aggregate behavior (`Exam`, `Chapter`, `Task`, `Variant`)
- Enum XML parsing behavior (`Difficulty`, `Scope`)
- XML loader/writer edge cases and nested directory handling
- Application service generation selection and preview fallback branches
- Validator null/blank field aggregation
- Persistence load-validation failure path
- PDF null-exam, empty-exam, and long-word rendering branches
- Dataset-basierte Workflow-Integration (XML Ressourcen -> Validate -> Save/Load -> PDF)
- App-Service Fehlerpfade fuer exam-only Scope, fehlende Varianten und extensionless Pair-Output
- One end-to-end workflow across XML and PDF generation

## Where The Coverage Is Thin

The JaCoCo report shows that most of the UI layer is currently uncovered.

- `com.jexam.app.ui.components.pdf` is at 0%
- `com.jexam.app.ui.components.xml` is at 0%
- `com.jexam.app.ui.components` is at 0%
- `com.jexam.app.ui.base` is at 0%
- `com.jexam.app.ui` is at 0%
- `com.jexam.app.ui.styling` is at 0%

## Run Locally

    mvn test
    mvn package

`mvn package` also produces the Surefire report in `target/reports/surefire.html`.

## Current Verification Signal

The build is currently green at the test level, so the main risk is coverage depth rather than failing tests.

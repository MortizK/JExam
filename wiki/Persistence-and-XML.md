# Persistence And XML

This page documents the XML persistence path in `com.jexam.io`.

## Components

- `ExamXmlLoader`: parses XML into domain model objects.
- `ExamXmlWriter`: serializes domain model objects to XML.
- `ExamPersistenceService`: wraps load/save with validation policy.
- `ExamXmlException`: checked error type for persistence operations.

## Load Path

1. `ExamPersistenceService.loadValidated(path)`
2. `ExamXmlLoader.load(path)` parses XML into `Exam` graph
3. `ExamValidator.validate(exam)` runs immediately
4. Invalid loaded data throws `ExamXmlException`

## Save Path

1. `ExamPersistenceService.saveValidated(exam, path)`
2. `ExamValidator.validate(exam)` runs before write
3. Invalid data aborts write via `ExamXmlException`
4. `ExamXmlWriter.write(exam, path)` writes formatted XML

## XML Shape

Top-level structure:

- `<exam name="...">`
- nested `<chapter name="...">`
- nested `<task name="..." points="..." difficulty="..." scope="...">`
- nested `<variant>` with `<question>` and `<answer>` children

## Error Contract

- Parse or I/O failures become `ExamXmlException`.
- Enum parse failures are mapped to `ExamXmlException` with invalid enum semantics.
- Validation failures are aggregated and included in exception messages.

## Refactoring Checklist

When changing model fields or XML attributes:

1. Update `ExamXmlLoader` parse logic.
2. Update `ExamXmlWriter` serialization logic.
3. Keep validation and persistence service behavior aligned.
4. Extend persistence and round-trip tests.

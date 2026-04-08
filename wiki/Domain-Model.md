# Domain Model

This page describes `com.jexam.model` and the model contracts expected by validation and generation.

## Object Graph

- `Exam`
  - has a name
  - contains ordered `Chapter` entries
- `Chapter`
  - has a name
  - contains ordered `Task` entries
- `Task`
  - has a name
  - has points (double)
  - has `Difficulty` and `Scope`
  - contains one or more `Variant` entries
- `Variant`
  - has question text
  - has answer text

## Enums

- `Difficulty`: expected values map to XML values such as `easy`, `medium`, and `hard`.
- `Scope`: controls generation filtering, primarily `exam` and `mock-exam` semantics.

## Behavioral Assumptions

The current service and validator rely on these assumptions:

- Exam/chapter/task names must be non-blank.
- Task points must be positive and in 0.5 increments.
- Task difficulty and scope must be set.
- Every task must contain at least one variant.
- Variant question must be non-blank.

## Model Semantics Used By Generation

- `GenerationMode.EXAM` filters tasks by `Scope.EXAM`.
- `GenerationMode.MOCK_EXAM` includes both exam and mock-exam scoped tasks.
- Randomized generation clones tasks with one selected variant per task.
- Chapter goal points are normalized to 0.5 increments and resolved via fallback preference when infeasible.

## Equality And Round-Trip Expectations

`ExamXmlRoundTripTest` verifies model round-trip integrity through XML serialization and parsing. If model fields are added, update both loader/writer and equality-sensitive tests.

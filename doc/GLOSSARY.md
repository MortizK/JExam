# Glossary

This glossary standardizes project terminology for implementation and documentation.

## Exam

Top-level container for all exam content.

German aliases: Klausur.

Default name in prototype docs: `New Exam`.

## Chapter

Thematic grouping of tasks.

German aliases: Kapitel.

Default name in prototype docs: `New Chapter`.

## Task

A scored unit within a chapter with metadata and one or more variants.

German aliases: Aufgabe, Teilaufgabe (in older material).

Typical attributes:

- points
- difficulty
- scope

Default prototype values include name `New Subtask`, points `1`, difficulty `easy`, scope `exam`.

## Variant

Alternative statement/answer pair for a task.

German aliases: Variante.

Typical attributes:

- question text
- answer/solution text

Default prototype values: question `New Question`, answer `New Answer`.

## Difficulty

Task classification used during selection and generation.

Allowed values:

- `easy`
- `medium`
- `hard`

## Scope

Marks where a task is eligible.

Common values:

- `exam`
- `mock-exam`

## Validation

Consistency checks applied before generation.

Examples:

- each task has at least one variant
- valid points, difficulty, and scope
- generation blocked when data is invalid

## Outputs

Expected PDF output types:

- exam (without solutions)
- exam solution
- mock exam (without solutions)
- mock exam with solutions

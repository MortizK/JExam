# User Guide

This guide describes the expected user workflow for creating and exporting exams in JExam.

## 1. Start or Load an Exam

- Start with a new exam structure, or
- Open an existing XML file.

If XML is invalid, loading should fail with an error message.

## 2. Build the Exam Structure

Create and manage hierarchy:

- Add chapters
- Add tasks in chapters
- Add variants in tasks

Edit task metadata:

- Points
- Difficulty (`easy`, `medium`, `hard`)
- Scope (`exam`, `mock-exam`)

## 3. Maintain Content Quality

Before generation, ensure:

- Every task has at least one variant.
- Question and solution content is complete where required.
- Points and metadata are valid.

## 4. Configure PDF Generation

In the PDF area:

- Reorder chapters
- Exclude chapters from generation if needed
- Select generation type:
  - Exam PDF
  - Solution PDF
  - Mock exam variants

## 5. Generate and Review

- Trigger generation action.
- Use preview if available.
- Save resulting PDF files.

## Common Issues

- Generation blocked: exam structure is invalid.
- Missing mock exam output: no tasks marked as `mock-exam`.
- Incomplete result: chapter selection or ordering excludes required content.

## Related Documentation

- [SETUP.md](SETUP.md)
- [ARCHITECTURE.md](ARCHITECTURE.md)
- [GLOSSARY.md](GLOSSARY.md)

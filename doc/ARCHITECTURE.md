# Architecture

## Purpose

JExam manages structured exam content and generates PDFs from validated XML data.

High-level flow:

1. Load or create exam structure
2. Edit chapters, tasks, and variants
3. Validate structural and content constraints
4. Generate PDF outputs (exam and optional solutions)

## Domain Model

Hierarchy:

- Exam
- Chapter
- Task
- Variant

Relationship sketch:

- One exam contains many chapters.
- One chapter contains many tasks.
- One task contains one or more variants.

Key task attributes:

- Points
- Difficulty (`easy`, `medium`, `hard`)
- Scope (`exam`, `mock-exam`)

Key variant attributes:

- Question text
- Solution text

## Validation Rules (Specification-Derived)

- A task must contain at least one variant.
- Difficulty and scope values must be valid.
- Point values must be valid according to model constraints.
- Invalid data blocks PDF generation.

## UI Structure (JavaFX)

Main navigation:

- XML tab: structure editing
- PDF tab: generation configuration and preview

XML tab concepts:

- Tree-based navigation of exam hierarchy
- Detail panel for selected node
- Child tables/lists and create/edit/delete interactions

PDF tab concepts:

- Configuration controls
- Chapter ordering and exclusion controls
- Generation actions (exam, mock exam, solution)
- Preview area

## Architectural Direction for Refactor

Target engineering principles:

- Clear separation between model, UI, and generation logic
- Testable validation layer independent from UI
- Explicit mapping between domain model and XML representation
- Consistent naming and JavaDoc on public APIs

## Source References

- [LaTeX/chapters/intro.tex](LaTeX/chapters/intro.tex)
- [LaTeX/chapters/usecases.tex](LaTeX/chapters/usecases.tex)
- [LaTeX/chapters/ui.tex](LaTeX/chapters/ui.tex)
- [LaTeX/chapters/ui.md](LaTeX/chapters/ui.md)
- [LaTeX/chapters/components.md](LaTeX/chapters/components.md)

# Documentation Index

This folder is the primary documentation source for JExam.

Policy:

- Markdown files in [doc/](doc/) are the canonical source-of-truth.
- LaTeX files in [doc/LaTeX/](doc/LaTeX/) are retained for archival/formal submission output.

## Reading Order

1. [SETUP.md](SETUP.md)
2. [ARCHITECTURE.md](ARCHITECTURE.md)
3. [USER_GUIDE.md](USER_GUIDE.md)
4. [GLOSSARY.md](GLOSSARY.md)
5. [METRICS_BASELINE.md](METRICS_BASELINE.md)
6. [REFACTORING_EVALUATION.md](REFACTORING_EVALUATION.md)

## Source Mapping

Mapped from existing specification material:

- System overview and requirements: [LaTeX/chapters/intro.tex](LaTeX/chapters/intro.tex)
- Domain terms: [LaTeX/chapters/lexi.tex](LaTeX/chapters/lexi.tex)
- Use cases: [LaTeX/chapters/usecases.tex](LaTeX/chapters/usecases.tex)
- UI and components: [LaTeX/chapters/ui.tex](LaTeX/chapters/ui.tex), [LaTeX/chapters/ui.md](LaTeX/chapters/ui.md), [LaTeX/chapters/components.md](LaTeX/chapters/components.md)

## Notes for Contributors

- Keep terminology aligned with [GLOSSARY.md](GLOSSARY.md).
- Update markdown docs first when requirements change.
- If LaTeX exports are needed, sync from markdown into LaTeX as a separate step.

## Implementation Status

| Phase | Scope | Status |
| --- | --- | --- |
| 0 | Maven bootstrap, Java 17 setup, package skeleton | Done |
| 1 | Domain model + validation with tests | Done |
| 2 | XML load/save + round-trip tests + validation wiring | Done |
| 3 | PDF generation services | Done (PDFBox implementation + tests) |
| 4 | Bare minimum UI | Done (chapter/task/variant CRUD + detail editing + PDF controls) |
| 5 | End-to-end hardening | Done (workflow integration tests and regression loop active) |
| 6 | MVC + metrics refactoring | Done with documented residual hotspots (see metrics/evaluation docs) |
| 7 | Refactoring evaluation + handoff | Done (before/after metrics summary and next-step backlog) |
| 8 | Documentation + styling implementation | In progress (Phase 1: method-level JavaDoc expanded in UI tab containers and shared UI components) |

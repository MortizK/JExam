# Refactoring Hotspots

This page captures high-value refactoring targets based on current structure and metrics.

## High-Risk Areas

- UI-heavy packages in `com.jexam.app.ui.*` have 0% coverage in the latest JaCoCo report.
- `JExamApp` and tab containers coordinate many responsibilities and are natural complexity magnets.
- `ExamApplicationService` is central and broad; regression risk is high when altering generation logic.

## Safe Refactoring Sequence

1. Add characterization tests around current behavior.
2. Extract pure helper logic from UI classes into testable collaborators.
3. Keep public service API stable during internal cleanup.
4. Move duplicate selection/refresh flows behind single-purpose methods.

## Immediate Candidate Extractions

- XML tab selection and visibility logic from `XmlTabContainer` into navigation/state helpers.
- Preview/export decision logic from `PdfTabContainer` into a dedicated policy helper.
- Chapter goal resolution and candidate selection internals in `ExamApplicationService` into explicit generation strategy classes.

## Guardrails

- Preserve dirty-state and preview-stale behavior.
- Preserve generation warning behavior (warning vs hard error).
- Keep persistence validation policy unchanged unless explicitly redesigned.

## Success Signal

- Increased coverage in `com.jexam.app.ui.*` without reduced pass rate.
- Smaller class size and lower method complexity in UI shell classes.
- No behavioral regression in workflow and generation tests.

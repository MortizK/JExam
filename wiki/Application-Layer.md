# Application Layer

This page describes the orchestration layer in `com.jexam.app`.

## Core Classes

- `Main`: JVM entry point that delegates to JavaFX app startup.
- `JExamApp`: JavaFX shell that wires tabs, header actions, keyboard shortcuts, language, and theme.
- `ExamApplicationService`: use-case orchestration boundary for open/save/validate/generate operations.
- `XmlTabContainer`: XML editing tab shell with hierarchical navigation and editor coordination.
- `PdfTabContainer`: PDF tab shell with chapter selection, preview lifecycle, and export actions.

## Request Flow

1. User action starts in JavaFX controls.
2. Tab container delegates to `ExamApplicationService`.
3. Service validates and coordinates model, persistence, and generation.
4. UI updates local state (`UiStateManager`, selection model, dirty/stale flags).

## Key Behaviors In `JExamApp`

- Loads user preferences at startup (language, theme, last XML/PDF paths).
- Builds two-tab UI (`XML` and `PDF`) and synchronizes focus on tab switches.
- Registers shortcuts:
  - Ctrl/Cmd+N: new exam
  - Ctrl/Cmd+O: open XML
  - Ctrl/Cmd+S: save XML
  - Ctrl/Cmd+P: open preview flow
  - Ctrl/Cmd+Shift+T: theme toggle
- Handles dirty-state close confirmation.

## Why `ExamApplicationService` Matters

`ExamApplicationService` is the most important boundary for non-UI code changes.

It owns:

- Current exam lifecycle (`newExam`, `openExam`, `saveExam`)
- Validation (`validateCurrentExam`)
- Generation orchestration (`generatePdf`, `generatePdfPair`, preview generation)
- Chapter inclusion/order/goal selection for generation
- Deterministic random-seed handling for repeatable variant selection

## XML Tab Responsibilities

`XmlTabContainer` keeps view-level concerns in one place:

- Hierarchy navigation over chapter/task/variant nodes
- Editor visibility based on current selection depth
- Dirty-state notifications to the shell
- Localization refresh wiring for tab texts and controls

Refactoring guidance:

- Keep mutations in `ExamApplicationService`; keep tab container focused on presentation and routing.
- Avoid introducing persistence or generation details in editor components.

## PDF Tab Responsibilities

`PdfTabContainer` coordinates preview/export interactions:

- Generation mode and fallback selection controls
- Chapter include/exclude/reorder and goal-points configuration
- Preview stale-state logic and preview reuse for export
- Validation issue selection callback back to XML navigation

Refactoring guidance:

- Preserve `previewStale` semantics so users do not export stale previews unintentionally.
- Keep warning display non-blocking; generation warnings are informational, not hard failures.

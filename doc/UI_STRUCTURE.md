# UI Structure (Checkpoint 1)

Status: Draft for review
Scope: Structure only (XML tab + PDF tab), based on tex sources and current app constraints.

## 1. Sources and Traceability

Primary specification sources:
- [doc/LaTeX/02_Spezifikation.tex](doc/LaTeX/02_Spezifikation.tex)
- [doc/LaTeX/chapters/ui.tex](doc/LaTeX/chapters/ui.tex)
- [doc/LaTeX/01_Analysefragen.tex](doc/LaTeX/01_Analysefragen.tex)
- [doc/LaTeX/chapters/intro.tex](doc/LaTeX/chapters/intro.tex)

Current implementation references:
- [src/main/java/com/jexam/app/JExamApp.java](src/main/java/com/jexam/app/JExamApp.java)
- [src/main/java/com/jexam/app/JExamSelectionModel.java](src/main/java/com/jexam/app/JExamSelectionModel.java)
- [src/main/java/com/jexam/app/JExamUiSupport.java](src/main/java/com/jexam/app/JExamUiSupport.java)

Figure references used for structure alignment:
- [doc/LaTeX/fig/header.pdf](doc/LaTeX/fig/header.pdf)
- [doc/LaTeX/fig/breadcrumbs.pdf](doc/LaTeX/fig/breadcrumbs.pdf)
- [doc/LaTeX/fig/treeview.pdf](doc/LaTeX/fig/treeview.pdf)
- [doc/LaTeX/fig/pdf_sidebar.pdf](doc/LaTeX/fig/pdf_sidebar.pdf)

## 2. Top-Level Information Architecture

The UI is split into two primary tabs with shared top-level controls:

1. XML Tab
- Purpose: Manage the exam hierarchy and edit domain data.
- Primary outcome: Valid, complete exam data in memory and XML persistence.

2. PDF Tab
- Purpose: Configure generation context (chapter ordering/inclusion, generation mode) and trigger preview/export.
- Primary outcome: Correct exam/mock/solution PDF outputs.

Shared top area (visible regardless of selected tab):
- App title
- Language selector
- Core file actions: New, Open, Save
- Validation action
- Primary navigation between XML and PDF context

## 3. XML Tab Structure

Layout regions:

1. Left Navigation Rail
- Tree view of Exam -> Chapter -> Task (expand/collapse)
- Search/filter input for hierarchy filtering
- Selection drives center content context

2. Center Content Region (context-sensitive)
- Exam context: Exam header + chapter table + inline exam editor
- Chapter context: Chapter header + task table + inline chapter editor
- Task context: Task header + variant list + inline task/variant editor

3. Inline Edit Region (non-modal)
- No creation modal is used in this stage.
- Editing/creation fields for the currently selected object are embedded in center content.
- Delete confirmation dialog remains allowed for destructive actions.

Navigation model:
- Header navigation switches top-level tab
- Breadcrumbs provide quick parent/ancestor navigation
- Tree selection and center-list selection keep each other synchronized

## 4. PDF Tab Structure

Layout regions:

1. Left/Main Context (top-left)
- Generation mode controls (Exam, Solution, Mock)
- Preview action
- Export action
- Validation/error summary area for generation blockers

2. Left Configuration Region (below main context)
- Ordered list of included chapters
- Excluded-chapters area
- Up/Down controls for reordering
- Include/Exclude actions

3. Right Preview Region
- Preview is displayed inside the app, not opened as an external file/browser action.
- Region shows a pre-generated preview based on current XML data and current PDF settings.
- Region includes loading/error state handling for preview refresh.

Behavioral boundary:
- PDF Tab does not edit content text directly.
- PDF Tab consumes XML data and generation configuration only.

## 5. Cross-Tab State Boundaries

Global shared state:
- Current exam model
- Current language
- Last validation result
- Persistence path context

XML Tab-owned interaction state:
- Current hierarchy selection
- Active editor input state
- Inline create/edit form state
- Delete confirmation state

PDF Tab-owned interaction state:
- Generation chapter order and inclusion set
- Selected generation mode
- Last preview render state and status

Synchronization rules:
- Any XML model change invalidates prior generation status.
- Validation status is recomputed before generation actions.
- Language changes update labels/text in both tabs immediately.

## 6. Navigation Flow (Structure-Level)

1. App opens on XML tab with default exam loaded.
2. User edits structure/content in XML tab.
3. User validates (optional explicit step, mandatory before generation actions).
4. User switches to PDF tab.
5. User configures chapter order/inclusion and mode.
6. User previews PDF in the right in-app preview region.
7. User exports PDF.

## 7. Mapping to Current JavaFX Composition

Planned structural decomposition from current single-screen layout:

1. Tab container
- Introduce TabPane with XML Tab and PDF Tab as primary containers.

2. XML tab content module
- Reuse and modularize existing hierarchy list + editor sections currently in [src/main/java/com/jexam/app/JExamApp.java](src/main/java/com/jexam/app/JExamApp.java).

3. PDF tab content module
- Bind to generation APIs already exposed by [src/main/java/com/jexam/app/ExamApplicationService.java](src/main/java/com/jexam/app/ExamApplicationService.java).

4. Shared support
- Continue using [src/main/java/com/jexam/app/JExamUiSupport.java](src/main/java/com/jexam/app/JExamUiSupport.java) for dialogs/file handling/language text.

## 8. Spec-First Improvements Included

These are included as structured improvements while preserving core spec behavior:

1. Explicit generation-blocker area in PDF tab.
- Makes validation errors visible without forcing modal-only flow.

2. Clear state ownership between tabs.
- Reduces accidental coupling and supports later extraction into dedicated tab controllers.

3. In-app preview panel in PDF tab.
- Aligns preview use-case with explicit user feedback directly inside the application window.

## 9. Assumptions for Review

1. Two-tab design is the only top-level navigation model for this stage.
2. Preview is rendered and displayed in-app in the right preview region.
3. Tree filter is required in XML tab navigation rail.
4. Shared top area retains core actions; tab-specific actions stay inside each tab.

## 10. Review Questions (Checkpoint 1)

Please validate or change:

1. Is this two-tab boundary correct, especially what belongs in XML vs PDF tab?
2. Do you want breadcrumbs visible only in XML tab or globally in shared top area?
3. Should validation action stay global, or move into both tab contexts separately?
4. Is the PDF preview/result region split acceptable (left controls/config stacked, right preview panel)?
5. Do you want additional top-level regions before we lock structure and move to component specification?

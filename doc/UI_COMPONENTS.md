# UI Components (Checkpoint 2)

Status: Draft for review
Scope: Detailed component specification for XML tab, PDF tab, and shared components.
Depends on approved structure in [doc/UI_STRUCTURE.md](doc/UI_STRUCTURE.md).

## 1. Component Traceability

Source references:
- [doc/LaTeX/chapters/ui.tex](doc/LaTeX/chapters/ui.tex)
- [doc/LaTeX/01_Analysefragen.tex](doc/LaTeX/01_Analysefragen.tex)
- [doc/LaTeX/chapters/intro.tex](doc/LaTeX/chapters/intro.tex)

Implementation context references:
- [src/main/java/com/jexam/app/JExamApp.java](src/main/java/com/jexam/app/JExamApp.java)
- [src/main/java/com/jexam/app/JExamSelectionModel.java](src/main/java/com/jexam/app/JExamSelectionModel.java)
- [src/main/java/com/jexam/app/JExamUiSupport.java](src/main/java/com/jexam/app/JExamUiSupport.java)
- [src/main/java/com/jexam/app/ExamApplicationService.java](src/main/java/com/jexam/app/ExamApplicationService.java)

## 2. Shared Components

### 2.1 App Header Navigation

Purpose:
- Provide top-level app controls and switch between XML and PDF tabs.

Inputs:
- Current language.
- Current tab.
- Global app state availability (exam loaded, validation status).

State:
- Active tab id: XML or PDF.
- Selected UI language.

Outputs/events:
- OnTabChange(tabId).
- OnLanguageChange(language).
- OnNewExam().
- OnOpenExam().
- OnSaveExam().
- OnValidate().
- OnCloseRequest() with unsaved confirmation.

Validation rules:
- Save/Open actions require valid path choices.
- Validate action available when exam exists in memory.

Dependencies:
- Exam service for New/Open/Save/Validate.
- Language dictionary/fallback provider.

Empty/error states:
- Missing file path: show non-blocking warning.
- Validation failed: show summarized validation errors.
- Unsaved XML on close: show confirmation dialog warning progress may be lost (XML auto-saved to temp folder).

### 2.2 Breadcrumb Navigation

Purpose:
- Show and navigate current hierarchy path: Exam -> Chapter -> Task.

Inputs:
- Current hierarchy selection.

State:
- Current path segments.

Outputs/events:
- OnNavigateToExam().
- OnNavigateToChapter(chapterId).
- OnNavigateToTask(taskId).

Validation rules:
- Segment click enabled only for existing ancestors.

Dependencies:
- Selection model.

Empty/error states:
- No selection: show Exam root only.

### 2.3 TreeView + Filter

Purpose:
- Hierarchical navigation and filtering of exam content.

Inputs:
- Exam model.
- Filter text.

State:
- Expanded nodes.
- Selected node.
- Filter text.

Outputs/events:
- OnNodeSelected(nodeType, nodeIndexPath).
- OnFilterChanged(text).

Validation rules:
- Filter must not break parent visibility rule.
- If leaf matches filter, all ancestors must remain visible.

Dependencies:
- Selection model.
- Exam model read access.

Empty/error states:
- No chapters/tasks: show empty helper text.
- No filter match: show no-results state with clear-filter action.

### 2.4 Delete Confirmation Dialog

Purpose:
- Confirm destructive delete actions.

Inputs:
- Target type: chapter, task, variant.
- Target label.

State:
- Open/closed.

Outputs/events:
- OnConfirmDelete().
- OnCancelDelete().

Validation rules:
- Must show cascade impact message when relevant.

Dependencies:
- UI support dialog layer.

Empty/error states:
- Not applicable.

### 2.5 Localization Text Provider

Purpose:
- Resolve UI text by key with fallback to English.

Inputs:
- Language selection.
- Text key.

State:
- Active language.

Outputs/events:
- OnTextResolved(key, text).

Validation rules:
- If key missing in selected language, use English.
- If key missing in English, return key as diagnostic fallback.

Dependencies:
- Language dictionary maps.

Empty/error states:
- Unknown language selection: fallback to English.

## 3. XML Tab Components

### 3.1 XML Tab Container

Purpose:
- Orchestrate XML tab layout and synchronize tree, breadcrumbs, and center editor region.
- Manage empty state when no XML is loaded.

Inputs:
- Current exam model (or null/empty when starting fresh).
- Current hierarchy selection.

State:
- Local editor dirty flags.
- Active context: exam, chapter, task.
- Is XML loaded: boolean.

Outputs/events:
- OnSelectionChange(...).
- OnEditorSave(...) - triggers save on change.
- OnCreateNewExam() - from empty state builder.
- OnDeleteRequest(...).
- OnNavigateToChild(...) - when selecting table row.

Validation rules:
- Prevent invalid save payloads from inline editors.
- Save-on-change applies after each valid edit.

Dependencies:
- Selection model.
- Exam service CRUD methods.

Empty/error states:
- No XML loaded: show full-screen empty state with "Create New Exam" action and "Load XML" action that allows user to build up exam structure from scratch or load an existing one.
- Empty exam: show starter hint and chapter creation action.

### 3.2 Exam Header + Inline Exam Editor

Purpose:
- Display and edit exam-level metadata inline.

Inputs:
- Exam name.

State:
- Draft exam name.

Outputs/events:
- OnExamNameSave(name).

Validation rules:
- Name must not be blank.

Dependencies:
- Exam service update function.

Empty/error states:
- Invalid name: field error, save disabled.

### 3.3 Chapter Table

Purpose:
- Show all chapters from current exam context with select and delete actions.
- Row selection navigates to chapter for editing in the center region.

Inputs:
- Chapter list.

State:
- Selected chapter row.

Outputs/events:
- OnSelectChapter(index) - navigates breadcrumb and center editor to chapter for inline editing.
- OnCreateChapterInline() - from add button.
- OnDeleteChapter(index) - delete button stays in table row.

Validation rules:
- Deletion disabled when minimum-remaining rule would break.

Dependencies:
- Exam service add/remove chapter.

Empty/error states:
- No chapters: show inline create chapter call-to-action.

### 3.4 Chapter Header + Inline Chapter Editor

Purpose:
- Edit chapter metadata in-place when chapter is selected from table.

Inputs:
- Chapter name (from navigation/selection).

State:
- Draft chapter name.
- Is dirty (tracking unsaved edits).

Outputs/events:
- OnChapterNameChange(name) - saves on change.

Validation rules:
- Name must not be blank.

Dependencies:
- Exam service chapter update path.

Empty/error states:
- Invalid name: field-level error message.
- No chapter selected: region hidden.

### 3.5 Task Table

Purpose:
- Show chapter tasks and provide create and delete actions.
- Row selection navigates to task for editing in the center region.

Inputs:
- Task list for selected chapter.

State:
- Selected task row.

Outputs/events:
- OnSelectTask(index) - navigates breadcrumb and center editor to task for inline editing.
- OnCreateTaskInline() - from add button.
- OnDeleteTask(index) - delete button stays in table row.

Validation rules:
- Delete disabled if it would violate minimum task constraints.

Dependencies:
- Exam service add/remove task.

Empty/error states:
- No tasks: show inline create task action.

### 3.6 Task Header + Inline Task Editor

Purpose:
- Edit task properties inline when task is selected from table: name, points, difficulty, scope.

Inputs:
- Task properties (from navigation/selection).

State:
- Draft property values.
- Is dirty (tracking unsaved edits).

Outputs/events:
- OnTaskChange(...) - saves on change.

Validation rules:
- Points must be > 0 and in 0.5 increments.
- Difficulty and scope required.
- Name must not be blank.

Dependencies:
- Exam service updateTaskDetails.
- Validation rules from service/validator.

Empty/error states:
- Invalid points format: field error, invalid fields not saved.
- No task selected: region hidden.

### 3.7 Variant List

Purpose:
- Show variants for selected task with create and delete actions.
- Row selection navigates to variant for editing in the center region.

Inputs:
- Variant list.

State:
- Selected variant index.

Outputs/events:
- OnSelectVariant(index) - navigates to variant for inline editing below the list.
- OnCreateVariantInline() - from add button (creates default variant).
- OnDeleteVariant(index) - delete button stays in list row.

Validation rules:
- Deletion blocked when removing last remaining variant.

Dependencies:
- Exam service addVariant/removeVariant.

Empty/error states:
- Should not be empty in valid state; if empty due to inconsistency, show recovery action to add variant.

### 3.8 Inline Variant Editor

Purpose:
- Edit question and answer text of selected variant inline, positioned below variant list.

Inputs:
- Variant question/answer (from selection).

State:
- Draft question/answer.
- Is dirty (tracking unsaved edits).

Outputs/events:
- OnVariantChange(...) - saves on change.

Validation rules:
- Question must not be blank.
- Answer may be empty.

Dependencies:
- Exam service updateVariantDetails.

Empty/error states:
- No variant selected: region hidden with select-variant hint.
- Invalid question: field error, unsaved changes not persisted.

## 4. PDF Tab Components

### 4.1 PDF Tab Container

Purpose:
- Host left stacked control/config regions and right in-app preview region.

Inputs:
- Current exam model.
- Generation configuration state.

State:
- Active generation mode.
- Preview status.

Outputs/events:
- OnGeneratePreview(mode, config).
- OnExportPdf(mode, config).

Validation rules:
- Generation actions blocked by validation errors.

Dependencies:
- Exam service generation and preview APIs.

Empty/error states:
- No generation-eligible chapters: show blocking state with explanation.

### 4.2 Generation Controls (Top-Left)

Purpose:
- Select generation mode and trigger manual preview/export.

Inputs:
- Available modes and current mode.

State:
- Selected mode.

Outputs/events:
- OnModeChanged(mode).
- OnPreviewRequested() - manual refresh action.
- OnExportRequested().

Validation rules:
- Disable actions if no chapter selected for generation.

Dependencies:
- Exam service generatePdf and preview function.

Empty/error states:
- Invalid generation state: show inline error summary.
- Stale preview indicator: show manual refresh required message.

### 4.3 Generation Validation Summary

Purpose:
- Display generation blockers clearly in PDF tab, grouped by chapter and task for easier navigation.

Inputs:
- Validation result list.

State:
- Expanded/collapsed per-issue details.

Outputs/events:
- OnIssueSelected(path) - navigate to affected element in XML tab.

Validation rules:
- Display latest result for active mode and config.
- Group issues by chapter/task hierarchy for readability.

Dependencies:
- Validator/service results.

Empty/error states:
- No errors: show success/ready state.

### 4.4 Chapter Configuration Region (Below Left Main)

Purpose:
- Manage included/excluded chapter sets and order for generation via drag-and-drop and button controls.

Inputs:
- Included chapter order.
- Excluded chapter list.

State:
- Selected included item.
- Selected excluded item.
- Drag source/target for reordering.

Outputs/events:
- OnMoveUp(index).
- OnMoveDown(index).
- OnExclude(index).
- OnInclude(chapterId).
- OnReorderByDrag(fromIndex, toIndex).
- OnResetSelection().

Validation rules:
- At least one chapter must remain selected.
- Move actions enabled only with valid neighboring index.
- Drag-and-drop reordering follows same constraints as button moves.

Dependencies:
- Exam service generationChapterOrder and include/exclude/reorder methods.

Empty/error states:
- Included list empty: show blocking message and include action.

### 4.5 In-App Preview Region (Right)

Purpose:
- Render pre-generated preview inside the app window (in-app only, not external).

Inputs:
- Current XML data snapshot.
- Current generation mode and chapter configuration.

State:
- Preview lifecycle: idle, loading, ready, stale, error.
- Last rendered timestamp/config fingerprint.

Outputs/events:
- OnRefreshPreview() - triggered only by manual refresh button.
- OnOpenExternalFallback() if embedded rendering fails (fallback only).

Validation rules:
- Preview marked stale when XML or PDF config changes.
- Manual refresh required when stale.
- Stale indicator shown when data changed since last render.

Dependencies:
- Preview generator API in exam service.
- Embedded preview renderer layer (implementation choice in next phase).

Empty/error states:
- Idle: show no-preview-yet helper.
- Loading: show spinner/progress.
- Stale: show stale indicator with manual refresh button.
- Error: show recoverable error with retry.

## 5. Interaction Contracts Between XML and PDF Tabs

1. XML data mutation contract
- Any XML tab save/create/delete action marks preview as stale in PDF tab.

2. Validation contract
- PDF generation and preview use current validated snapshot.

3. Language contract
- Component labels, helper text, and tooltip text update immediately when language changes.
- Missing translation key falls back to English.

## 6. Decisions - Final (Approved by User)

1. **Inline create forms**: Removed. Add buttons create default components directly.
2. **Preview refresh**: Manual via refresh action button (not automatic).
3. **Validation display**: Per-issue visibility grouped by chapter/task hierarchy (not flat).
4. **Chapter ordering**: Drag-and-drop + up/down buttons both available.
5. **Save model**: Save-on-change for all inline editors (no explicit save button per component).

## 7. Implementation Update (2026-04-02)

This section documents concrete component behavior implemented after the initial component draft.

### 7.1 XML Child List Components

Affected components:
- [src/main/java/com/jexam/app/ui/components/xml/ChapterTableComponent.java](src/main/java/com/jexam/app/ui/components/xml/ChapterTableComponent.java)
- [src/main/java/com/jexam/app/ui/components/xml/TaskTableComponent.java](src/main/java/com/jexam/app/ui/components/xml/TaskTableComponent.java)
- [src/main/java/com/jexam/app/ui/components/xml/VariantListComponent.java](src/main/java/com/jexam/app/ui/components/xml/VariantListComponent.java)

Implemented interaction changes:
- Per-row Delete button on each list row
- Footer Delete button removed
- Footer Add button retained

### 7.2 Inline Row Statistics

Composition handled in:
- [src/main/java/com/jexam/app/XmlTabContainer.java](src/main/java/com/jexam/app/XmlTabContainer.java)

Implemented row content:
- Chapter rows: name + stats line (children, points, difficulty distribution, scope distribution)
- Task rows: name + stats line (children, points, difficulty, scope)
- Variant rows: compact label row with row-local delete action

### 7.3 Keyboard Contracts in Child Lists

Implemented list-level key behavior:
- Up/Down: change selected row
- Enter: activate selected child context
- Delete: trigger selected child delete flow
- Tab/Shift+Tab: move focus between tree, list, and editor

### 7.4 Delete Flow Contract

Delete orchestration remains centralized in:
- [src/main/java/com/jexam/app/XmlTabContainer.java](src/main/java/com/jexam/app/XmlTabContainer.java)

Runtime safeguards remain unchanged:
- confirmation dialog before delete
- minimum-child protection per hierarchy level

### 7.5 PDF Focus Contract Update

Relevant components:
- [src/main/java/com/jexam/app/PdfTabContainer.java](src/main/java/com/jexam/app/PdfTabContainer.java)
- [src/main/java/com/jexam/app/ui/components/pdf/ValidationSummaryComponent.java](src/main/java/com/jexam/app/ui/components/pdf/ValidationSummaryComponent.java)
- [src/main/java/com/jexam/app/ui/components/pdf/GenerationControlsComponent.java](src/main/java/com/jexam/app/ui/components/pdf/GenerationControlsComponent.java)

Implemented behavior:
- PDF tab focuses validation tree when issues are present
- otherwise focuses generation controls

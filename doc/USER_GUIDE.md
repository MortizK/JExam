# User Guide

This guide explains how to use JExam to create, validate, preview, and export exams.

## 1. Quick Start

1. Start the application.
2. Choose one of these actions:
   - Create a new exam
   - Open an existing XML file
3. Work in the XML tab to edit exam content.
4. Switch to PDF tab to validate, preview, and export.

## 2. XML Tab Workflow

### 2.1 Navigate the Exam

Use the left tree to select:
- Exam
- Chapter
- Task

Use breadcrumbs in the right area to move back up the hierarchy.

### 2.2 Edit Exam, Chapter, Task, and Variant Data

The right side shows context-sensitive editors:
- Exam selection: exam editor and chapter children list
- Chapter selection: chapter editor and task children list
- Task selection: task editor and variant children list
- Variant selection: variant editor

Edits are applied as you type valid values.

### 2.3 Use Children Lists

Children rows support:
- Add button in footer
- Delete button on each row
- Row selection for navigation

Row statistics:
- Chapter rows show aggregated child statistics:
  - tasks and variants count
  - total points
  - difficulty distribution
  - scope distribution
- Task rows show:
  - variants count
  - total points
  - difficulty
  - scope

### 2.4 Delete Safety Rules

Delete operations are protected:
- A confirmation dialog appears before deletion
- Minimum child constraints are enforced

Examples:
- You cannot delete the last chapter
- You cannot delete the last task in a chapter
- You cannot delete the last variant in a task

## 3. Keyboard Shortcuts and Navigation

### 3.1 Global Shortcuts

- Ctrl/Cmd+N: New exam
- Ctrl/Cmd+O: Open XML
- Ctrl/Cmd+S: Save XML
- Ctrl/Cmd+P: Open PDF tab and generate preview

### 3.2 Children List Keyboard Controls

- Up/Down: move selection between child rows
- Enter: open selected child context
- Delete: delete selected child (with safeguards)
- Tab/Shift+Tab: move between tree, children list, and editor

## 4. PDF Tab Workflow

### 4.1 Validate and Review Issues

Validation summary appears in the left column.
- Click an issue to navigate to the corresponding XML location.

### 4.2 Configure Generation

Set generation mode and chapter inclusion/order in the left column:
- include or exclude chapters
- reorder included chapters

### 4.3 Preview and Export

- Generate preview to refresh in-app PDF preview
- Use Export PDF to save the selected generation mode output
- Use Open External to open preview file with your system PDF viewer

## 5. Saving and Closing

- Use Save to write current exam XML to disk.
- If unsaved changes exist when closing, a confirmation prompt appears.

## 6. Troubleshooting

### XML open fails
Possible causes:
- invalid XML structure
- unsupported or malformed values

Action:
- verify XML validity and required fields

### PDF preview is stale
Cause:
- XML data or generation configuration changed after last preview

Action:
- click Refresh Preview or generate preview again

### Cannot delete an item
Cause:
- minimum-child safety rule is active

Action:
- create another child item first, then retry deletion

## 7. Related Docs

- [doc/UI_STRUCTURE.md](doc/UI_STRUCTURE.md)
- [doc/UI_COMPONENTS.md](doc/UI_COMPONENTS.md)
- [doc/UI_STYLING.md](doc/UI_STYLING.md)
- [doc/SETUP.md](doc/SETUP.md)

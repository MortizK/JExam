# Architecture

JExam is organized as a layered desktop application.

## Top Level Flow

1. `Main` launches the JavaFX application.
2. `JExamApp` builds the window, tabs, actions, and navigation.
3. `ExamApplicationService` coordinates the current exam state and use cases.
4. `ExamValidator` checks domain rules before save or generation.
5. `ExamXmlLoader` and `ExamXmlWriter` handle XML persistence.
6. `PdfBoxGenerationService` renders the exam to PDF.

## Package Map

### `com.jexam.app`

This package is the orchestration layer around the UI.

- `Main` is the JVM entry point.
- `JExamApp` is the JavaFX application.
- `ExamApplicationService` owns the current exam and the main use cases.
- `JExamUiSupport`, `JExamSelectionModel`, `UiStateManager`, `ThemeManager`, and `UserPreferencesStore` support UI state and preferences.
- `XmlTabContainer` and `PdfTabContainer` host the two main tabs.

### `com.jexam.model`

This package holds the domain tree.

- `Exam` contains chapters.
- `Chapter` contains tasks.
- `Task` contains variants and task metadata.
- `Variant` stores the question and answer text.
- `com.jexam.model.enums` defines values such as difficulty and scope.

### `com.jexam.validation`

This package contains the validation boundary.

- `ExamValidator` traverses the domain tree and collects all violations.
- `ValidationResult` stores the result set.
- `ValidationError` describes an individual problem.

### `com.jexam.io`

This package handles XML persistence.

- `ExamXmlLoader` parses XML into the model.
- `ExamXmlWriter` serializes the model back to XML.
- `ExamPersistenceService` combines load, save, and validation.
- `ExamXmlException` is the persistence error type.

### `com.jexam.generation`

This package handles PDF export.

- `PdfGenerationService` is the abstraction.
- `PdfBoxGenerationService` is the current implementation.
- `StubPdfGenerationService` is a lightweight alternative used for testing or replacement scenarios.
- `GenerationMode` selects exam, mock exam, or solution output.

## UI Shape

The JavaFX app currently uses two tabs:

- XML tab for editing and navigation around exam content.
- PDF tab for previewing and generating exports.

The application header exposes the primary actions: new, open, save, validate, and language selection.

## Design Boundaries

- The UI should not know XML parsing details.
- Persistence should not depend on JavaFX UI classes.
- Validation should remain model-driven and reusable from save and generation paths.
- PDF generation should consume a validated exam model rather than UI state.

## Refactoring Hotspots

Based on the current coverage snapshot, the largest blind spots are the UI packages, especially the component-heavy XML and PDF tabs. The strongest coverage is in `com.jexam.io`, `com.jexam.validation`, `com.jexam.model`, `com.jexam.generation`, and `com.jexam.model.enums`.

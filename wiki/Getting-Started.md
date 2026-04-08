# Getting Started

This page is for contributors who need a quick, accurate entry into the codebase.

## Build And Run

The current Maven build is configured for Java 17.

    mvn clean test
    mvn package
    mvn verify

The package build produces the runnable jar and the current report artifacts under `target/`.

## Main Entry Points

- `com.jexam.app.Main` starts the application.
- `com.jexam.app.JExamApp` creates the JavaFX UI and wires the tabs, actions, and keyboard shortcuts.
- `com.jexam.app.ExamApplicationService` centralizes the application use cases.

## Recommended First Reads In Source

1. `src/main/java/com/jexam/app/Main.java`
2. `src/main/java/com/jexam/app/JExamApp.java`
3. `src/main/java/com/jexam/app/ExamApplicationService.java`
4. `src/main/java/com/jexam/model/Exam.java`
5. `src/main/java/com/jexam/validation/ExamValidator.java`
6. `src/main/java/com/jexam/io/ExamXmlLoader.java`
7. `src/main/java/com/jexam/io/ExamXmlWriter.java`
8. `src/main/java/com/jexam/generation/PdfBoxGenerationService.java`

## Package Responsibilities

- `com.jexam.model` defines the exam tree: exam, chapter, task, and variant.
- `com.jexam.validation` enforces business rules on the model.
- `com.jexam.io` moves the model to and from XML.
- `com.jexam.generation` renders the model to PDF.
- `com.jexam.app` coordinates the UI, user preferences, and application flow.

## Practical Developer Notes

- The application opens with an in-memory default exam if no file is loaded.
- XML and PDF actions are coordinated through the application service rather than directly from the UI classes.
- The current build includes Surefire and JaCoCo reports in `target/` after `mvn package`.

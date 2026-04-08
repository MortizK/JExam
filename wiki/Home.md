# JExam Developer Wiki

This wiki is a developer-oriented map of the current JExam codebase. It is based on the source tree and the current build outputs, not on the documentation under `doc/`.

JExam is a Java 17 JavaFX desktop application for editing XML-based exam content and generating PDFs.

## Start Here

1. [Getting Started](Getting-Started.md)
2. [Architecture](Architecture.md)
3. [Testing](Testing.md)
4. [Metrics Snapshot](Metrics.md)

## What Lives Where

- `com.jexam.app` contains the JavaFX entry point, the application service, UI state, theme handling, and user preferences.
- `com.jexam.io` contains XML loading, XML writing, and persistence orchestration.
- `com.jexam.model` contains the domain objects for exams, chapters, tasks, and variants.
- `com.jexam.validation` contains the validation rules and result model.
- `com.jexam.generation` contains PDF generation, including the PDFBox-backed implementation.

## Current Build Snapshot

- Maven release level: Java 17
- JavaFX version: 21.0.5
- JUnit version: 5.11.3
- JaCoCo report: 22% instruction coverage, 21% branch coverage, 22% line coverage, 23% method coverage, 30% class coverage
- Surefire report: 28 tests, 0 failures, 0 errors, 0 skipped, 100% success

## Use This Wiki For

- Finding the right entry point before editing code
- Understanding responsibility boundaries between packages
- Checking which areas are well covered by tests and which are not
- Locating the current build metrics without searching the target directory manually

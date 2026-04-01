# Setup and Local Workflow

This document describes the expected local setup for JExam contributors.

## Prerequisites

- Java 17 or newer
- JavaFX runtime (depending on build/runtime packaging approach)
- A Java IDE (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

## Repository Reality Check

Current repository status:

- Maven project is initialized in the repository root.
- Primary implementation now lives in [../src/](../src/).
- Legacy folder [../scr/](../scr/) is currently non-canonical and can be ignored for new code.

Because of this, the setup flow is now backend-first with incremental UI integration.

## Suggested Developer Workflow

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) and [GLOSSARY.md](GLOSSARY.md).
2. Implement incrementally in [../src/](../src/) according to the documented model.
3. Keep naming and comments in English.
4. Add tests for each rule introduced from specification docs.
5. Update markdown docs in [doc/](./) when behavior changes.

## Build and Run

From repository root:

- Build: `mvn clean package`
- Test: `mvn test`

Run packaged application from console:

- `java -jar target/jexam-0.1.0-SNAPSHOT-all.jar`

Notes:

- `target/jexam-0.1.0-SNAPSHOT.jar` is the thin project jar.
- `target/jexam-0.1.0-SNAPSHOT-all.jar` is the executable jar with dependencies.

Run minimal JavaFX shell:

- `mvn -q exec:java -Dexec.mainClass=com.jexam.app.JExamApp`

If `exec:java` is unavailable in your local setup, use your IDE run configuration with main class `com.jexam.app.JExamApp`.

## Platform Notes

Target platforms (from specification):

- Windows 11+
- macOS Monterey+
- Linux with OpenJDK 17+

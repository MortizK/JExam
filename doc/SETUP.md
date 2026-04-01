# Setup and Local Workflow

This document describes the expected local setup for JExam contributors.

## Prerequisites

- Java 17 or newer
- JavaFX runtime (depending on build/runtime packaging approach)
- A Java IDE (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

## Repository Reality Check

Current repository status:

- No finalized build file is committed yet (for example `pom.xml` is not present at this stage).
- Source directory [scr/](../scr/) is currently minimal.
- Existing docs define behavior and architecture targets.

Because of this, the setup flow is documentation-first right now.

## Suggested Developer Workflow

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) and [GLOSSARY.md](GLOSSARY.md).
2. Implement incrementally in [scr/](../scr/) according to the documented model.
3. Keep naming and comments in English.
4. Add tests for each rule introduced from specification docs.
5. Update markdown docs in [doc/](./) when behavior changes.

## Build and Run (When Build Config Is Added)

Once a build file exists, this section should be updated with exact commands for:

- Build
- Test
- Run desktop application

Do not add guessed commands until the build system is committed.

## Platform Notes

Target platforms (from specification):

- Windows 11+
- macOS Monterey+
- Linux with OpenJDK 17+

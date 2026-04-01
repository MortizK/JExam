# JExam

JExam is a JavaFX desktop application for managing XML-based question pools and generating exam PDFs.

## Project Status

This repository currently contains specification and design documentation. The production codebase is being refactored.

Current state:

- Prototype exists on `main` with technical debt.
- Documentation is being migrated to markdown as the primary source-of-truth.
- LaTeX documents are kept for archival and formal exports.

## Who This Is For

Primary audience:

- Developers
- Course contributors

Secondary audience:

- Educators and testers can start with [doc/USER_GUIDE.md](doc/USER_GUIDE.md).

## Repository Structure

- [doc/](doc/) documentation and specifications
- [doc/LaTeX/](doc/LaTeX/) archival/formal specification sources
- [scr/](scr/) implementation workspace (currently minimal)

## Documentation Map

Start here:

- [doc/README.md](doc/README.md)

Core docs:

- [doc/SETUP.md](doc/SETUP.md) local setup and workflow
- [doc/ARCHITECTURE.md](doc/ARCHITECTURE.md) domain model and system structure
- [doc/USER_GUIDE.md](doc/USER_GUIDE.md) user workflow for building exams
- [doc/GLOSSARY.md](doc/GLOSSARY.md) shared terms and defaults

Legacy/archival sources:

- [doc/02_Spezifikation.pdf](doc/02_Spezifikation.pdf)
- [doc/01_Analysefragen.pdf](doc/01_Analysefragen.pdf)

## Core Capabilities (Planned and Prototype)

- Create and edit exam structures in XML
- Manage chapters, tasks, and task variants
- Mark tasks by difficulty and scope (`exam` or `mock-exam`)
- Validate input before generation
- Generate PDFs for exam and solution variants

## Next Refactor Milestones

- Define and stabilize project layout under [scr/](scr/)
- Introduce reproducible build configuration
- Add tests for XML validation and generation rules
- Align implementation with documentation in [doc/](doc/)

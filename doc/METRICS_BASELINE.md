# Metrics Baseline

This document records the first measurable quality baseline for the current implementation.

Date: 2026-04-01

## Tooling

Primary complexity tool:
- Maven PMD plugin (`mvn pmd:pmd`)

Notes:
- JavaNCSS was tested but is not compatible with several Java language features used in this codebase (for example stream/lambda parsing), so PMD is the baseline source for McCabe at the moment.
- PMD plugin version is pinned in [../pom.xml](../pom.xml).

## Commands

From repository root:

- `mvn test`
- `mvn pmd:pmd`

Reports:

- `target/pmd.xml`
- `target/site/pmd.html`

## McCabe (Cyclomatic Complexity) Baseline

High-complexity methods currently reported by PMD:

1. `com.jexam.generation.PdfBoxGenerationService.generate(Exam, GenerationMode, Path)`
- Cyclomatic complexity: `11`
- Source: [../src/main/java/com/jexam/generation/PdfBoxGenerationService.java](../src/main/java/com/jexam/generation/PdfBoxGenerationService.java)

2. `com.jexam.io.ExamXmlLoader.load(Path)`
- Cyclomatic complexity: `10`
- Source: [../src/main/java/com/jexam/io/ExamXmlLoader.java](../src/main/java/com/jexam/io/ExamXmlLoader.java)

## Current Refactoring Targets

1. Reduce complexity in PDF generation flow by extracting traversal/writing steps.
2. Reduce complexity in XML loading by extracting node parsing and traversal guards.
3. Continue moving orchestration logic out of [../src/main/java/com/jexam/app/JExamApp.java](../src/main/java/com/jexam/app/JExamApp.java) into application services.

## Next Measurement Gate

After each refactoring batch:

1. Run `mvn test`
2. Run `mvn pmd:pmd`
3. Compare violations in `target/pmd.xml` against this baseline.

## Post-Refactoring Checkpoint (Phase Continuation)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn pmd:pmd` (build success)

Delta vs baseline:

1. `PdfBoxGenerationService.generate(Exam, GenerationMode, Path)`
- Previous: cyclomatic complexity `11`
- Current: no `CyclomaticComplexity` violation in PMD report

2. `ExamXmlLoader.load(Path)`
- Previous: cyclomatic complexity `10`
- Current: no `CyclomaticComplexity` violation in PMD report

Interpretation:

- Targeted extract-method refactoring reduced control-flow complexity at the previous hotspots enough to clear the PMD threshold.
- Remaining PMD findings are mostly design-style findings (for example Law of Demeter), not McCabe threshold breaches.

## Phase Continuation Checkpoint (UI Responsibility Extraction)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn pmd:pmd` (build success)

Observed delta:

1. `JExamApp` Law of Demeter findings reduced from `71` to `65` by extracting UI dialog/file-chooser responsibilities into `JExamUiSupport` and centralizing selection/count access.
2. Total PMD violations remained stable at `159` because some LoD findings moved into the new helper class and broader MVC decomposition is still pending.

Interpretation:

- This phase reduced concentration in the main UI class and prepared the code for larger decomposition.
- A larger drop now requires moving business/navigation operations out of `JExamApp` and reducing chained access in `ExamApplicationService` and XML writer classes.

## Phase Continuation Checkpoint (Service + XML Writer Refactoring)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn pmd:pmd` (build success)

Observed delta:

1. Total PMD violations reduced from `159` to `145`.
2. Law of Demeter reduced from `148` to `134`.
3. `ExamApplicationService` Law of Demeter reduced from `24` to `16` by centralizing chapter/task/variant navigation.
4. `ExamXmlWriter` Law of Demeter reduced from `18` to `12` by extracting chapter/task/variant write helpers.

Interpretation:

- This phase produced the first larger global PMD drop while keeping all tests green.
- The main remaining concentration is still `JExamApp`, so the next phase should continue MVC decomposition of that class.

## Phase Continuation Checkpoint (JExamApp Navigation Extraction)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn pmd:pmd` (build success)

Observed delta:

1. Total PMD violations reduced from `145` to `140`.
2. Law of Demeter reduced from `134` to `129`.
3. `JExamApp` Law of Demeter reduced from `65` to `44` by extracting hierarchy navigation into `JExamSelectionModel`.

Trade-off:

- Some LoD findings moved into `JExamSelectionModel` (new helper class), which is expected during decomposition and can be reduced in follow-up by introducing a richer domain/service API.

Interpretation:

- This phase achieved a strong reduction in the main UI hotspot and continued the MVC refactoring direction.
- Next phase should reduce LoD at source by adding intention-revealing domain/service methods and shrinking direct collection traversal in helper classes.

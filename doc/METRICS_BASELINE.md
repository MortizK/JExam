# Metrics Baseline

This document records the first measurable quality baseline for the current implementation.

Date: 2026-04-01

## Tooling

Primary complexity tool:
- Maven PMD plugin (`mvn pmd:pmd`)

Extended metrics toolchain:
- Checkstyle (`mvn checkstyle:checkstyle`)
- SpotBugs (`mvn spotbugs:spotbugs`)
- JaCoCo (`mvn verify`)

Notes:
- JavaNCSS was tested but is not compatible with several Java language features used in this codebase (for example stream/lambda parsing), so PMD is the baseline source for McCabe at the moment.
- PMD plugin version is pinned in [../pom.xml](../pom.xml).
- Checkstyle and SpotBugs are configured in non-blocking mode for baseline gathering; they can be tightened into quality gates later.
- SpotBugs is profile-gated to supported JDKs (`[17,23)`) due class-file compatibility issues on newer runtimes in this environment.

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

## Coverage Targets (Line Count Priority)

Date: 2026-04-22

Primary objective for this phase:

- Raise overall JaCoCo line coverage to `50%`.
- Prioritize high-yield line gains in application/service paths before deep edge-case expansion.

Per-package target policy:

- `com.jexam.model`, `com.jexam.model.enums`, `com.jexam.validation`, `com.jexam.io`: maintain `>=95%`.
- `com.jexam.generation`: raise and maintain `>=93%`.
- `com.jexam.app`: primary growth target, move toward `>=50%` by expanding service-layer tests and extracting testable logic from UI-coupled flow where required.
- `com.jexam.app.ui.*`: currently manual-test-oriented; do not block the 50% target on direct UI automation in this phase.

Ratcheting policy:

1. Immediate gate: no coverage regression from the latest accepted baseline.
2. Milestone ratchet: increase required overall line coverage by `+2` to `+3` points per checkpoint until `50%` is reached.
3. Keep PMD/Checkstyle/SpotBugs reports in each checkpoint summary so line gains do not hide maintainability regressions.

Verification commands for each checkpoint:

- `mvn clean verify`
- `mvn -DskipTests javadoc:javadoc`

Primary reports:

- `target/site/jacoco/index.html`
- `target/site/jacoco/jacoco.xml`
- `target/site/pmd.html`
- `target/checkstyle-result.xml`
- `target/spotbugsXml.xml`
- `target/reports/apidocs/index.html`

## Phase Kickoff Checkpoint (Coverage + Javadocs)

Date: 2026-04-22

Executed:

- `mvn -Dtest=ExamApplicationServiceTest,ExamApplicationServiceBranchesTest,ExamValidatorTest,ExamXmlLoaderWriterTest,PdfBoxGenerationServiceTest test` (53 tests passed)
- `mvn clean verify` (build success)
- `mvn -DskipTests javadoc:javadoc` (build success, warnings only)

Observed delta:

1. `ExamValidator` line coverage reached `100%` (56/56 lines).
2. `ExamApplicationService` line coverage improved from `316` covered lines to `320` covered lines.
3. `com.jexam.generation` package line coverage increased to `92.28%`.
4. `com.jexam.io` package line coverage remains high at `98.57%`.
5. Javadoc generation is now integrated via Maven plugin and produces `-javadoc.jar` during package.

Current package line coverage snapshot:

- `com.jexam.app`: `28.11%`
- `com.jexam.validation`: `100.00%`
- `com.jexam.io`: `98.57%`
- `com.jexam.generation`: `92.28%`
- Overall line coverage: `28.73%`

Interpretation:

- The line-count-first strategy is yielding measurable gains in covered service and validator paths while preserving full build stability.
- The remaining path to `50%` depends primarily on increasing coverage in `com.jexam.app` and selectively extracting UI-coupled logic into testable service methods.

## Phase Continuation Checkpoint (App Coverage + Javadoc Reduction)

Date: 2026-04-22

Executed:

- `mvn -Dtest=UserPreferencesStoreTest,ExamApplicationServiceBranchesTest,ExamApplicationServiceTest test` (33 tests passed)
- `mvn clean verify` (110 tests passed)
- `mvn -DskipTests javadoc:javadoc` (build success)

Observed delta:

1. Overall line coverage increased from `28.73%` to `30.24%`.
2. `com.jexam.app` package line coverage increased from `28.11%` to `31.12%`.
3. `UserPreferencesStore` improved to `48` covered lines (`6` missed).
4. `ExamApplicationService` remains at `320` covered lines with improved branch-path test resilience.
5. Javadoc warnings reduced to `11` after enum and constructor documentation updates.

Current package line coverage snapshot:

- `com.jexam.app`: `31.12%`
- `com.jexam.validation`: `100.00%`
- `com.jexam.io`: `98.59%`
- `com.jexam.generation`: `92.28%`
- Overall line coverage: `30.24%`

Interpretation:

- Adding deterministic tests for low-coverage app infrastructure (`UserPreferencesStore`) gives better line-yield than incremental micro-branches alone.
- The next high-impact path remains the untested UI-facing app classes (`XmlTabContainer`, `PdfTabContainer`, `JExamApp`) via further extraction and service-level tests.

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

## Phase Continuation Checkpoint (Domain Navigation API)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn pmd:pmd` (build success)

Observed delta:

1. Total PMD violations reduced from `140` to `129`.
2. Law of Demeter reduced from `129` to `119`.
3. `ExamApplicationService` Law of Demeter reduced from `16` to `11`.
4. `JExamSelectionModel` Law of Demeter reduced from `16` to `8`.

Key implementation change:

- Added richer domain traversal methods in `Exam`, `Chapter`, and `Task` (for example `chapterAt`, `taskAt`, `variantAt`, and count helpers) and updated app/service consumers to use them.

Interpretation:

- This phase reduced LoD at the source rather than only redistributing findings across helper classes.
- The largest remaining hotspot is still `JExamApp`, so the next phase should keep extracting UI/controller responsibilities from that class.

## Phase Continuation Checkpoint (Validation JavaDoc + Checkstyle Pass)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn checkstyle:checkstyle` (report generated)
- `mvn pmd:pmd` (build success)

Observed delta:

1. Checkstyle findings reduced from `542` to `498`.
2. PMD trajectory preserved (`mvn pmd:pmd` still succeeds).

Key implementation change:

- Refactored validation package classes (`ExamValidator`, `ValidationError`, `ValidationResult`) with JavaDoc coverage, final-parameter cleanup, and safer encapsulation defaults (`final` classes where applicable).

Environment note:

- JaCoCo agent still logs class-file compatibility warnings on this runtime but `mvn verify` completes successfully.

Interpretation:

- This phase made measurable progress on the JavaDoc/checkstyle part of the plan while keeping tests and PMD green.

## Phase Continuation Checkpoint (Model JavaDoc + Style Pass)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn checkstyle:checkstyle` (report generated)
- `mvn pmd:pmd` (build success)

Observed delta:

1. Checkstyle findings reduced from `498` to `370`.
2. PMD run remains successful.

Key implementation change:

- Applied JavaDoc and style cleanup to model classes (`Exam`, `Chapter`,
	`Task`, `Variant`) including final class markers, final parameters,
	and improved formatting of long lines.

Interpretation:

- This is the largest single Checkstyle reduction so far and keeps the
	implementation aligned with the plan's JavaDoc and clean-code phases.

## Phase Continuation Checkpoint (Enums + Package Docs Pass)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn checkstyle:checkstyle` (report generated)
- `mvn pmd:pmd` (build success)

Observed delta:

1. Checkstyle findings reduced from `370` to `347`.
2. PMD run remains successful.

Key implementation change:

- Added package-level JavaDocs for `model`, `model.enums`, and `validation`.
- Added enum API JavaDocs and formatting fixes in `Difficulty` and `Scope`.
- Applied additional line-length cleanup in `ExamValidator`, `Chapter`, and
	`Exam`.

Interpretation:

- This phase continued steady reduction while preserving test and PMD
	stability.

## Phase Continuation Checkpoint (Model Field + Shadowing Cleanup)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn checkstyle:checkstyle` (report generated)
- `mvn pmd:pmd` (build success)

Observed delta:

1. Checkstyle findings reduced from `347` to `293`.
2. PMD run remains successful.

Key implementation change:

- Added field-level JavaDocs and non-shadowing parameter names in `Exam`,
	`Chapter`, and `Task`.
- Added enum constant JavaDocs in `Difficulty` and `Scope`.
- Applied additional formatting and signature cleanups in `ExamXmlWriter` and
	`ExamValidator`.

Interpretation:

- This is another strong reduction pass; the remaining highest concentration is
	now in the `io` package (`ExamXmlLoader`, `ExamXmlWriter`,
	`ExamPersistenceService`, `ExamXmlException`).

## Phase Continuation Checkpoint (IO Package Cleanup)

Date: 2026-04-01

Executed:

- `mvn test` (10/10 tests passed)
- `mvn checkstyle:checkstyle` (report generated)
- `mvn pmd:pmd` (build success)

Observed delta:

1. Checkstyle findings reduced from `293` to `254`.
2. PMD run remains successful.

Key implementation change:

- Added JavaDocs and final parameters in `ExamXmlException` and
	`ExamPersistenceService`.
- Applied signature/final-parameter and line-length cleanup in
	`ExamXmlLoader` and `ExamXmlWriter`.

Interpretation:

- The remaining larger share of Checkstyle findings is now concentrated in the
	`generation` package and residual line-length rules.

## Final Re-Measurement and Plan Closure

Date: 2026-04-01

Executed final loop:

- `mvn test`
- `mvn pmd:pmd`
- `mvn checkstyle:checkstyle`
- `mvn verify`

Final measured values:

1. PMD total violations: `129` (from initial `166`)
2. PMD Law of Demeter: `119` (from initial `156`)
3. Checkstyle findings: `205` (from first cycle baseline `542`)
4. Test status: `10/10` passing

Quality-gate mode decision:

- Keep PMD/Checkstyle/SpotBugs in warning-mode (`failOnViolation=false`) for
	now, because residual findings are still concentrated in high-churn UI
	classes.
- This aligns with the plan's "threshold or justified exceptions" criterion;
	the exception is documented and measurable.

Residual technical debt (prioritized):

1. `src/main/java/com/jexam/app/JExamApp.java`
2. `src/main/java/com/jexam/app/ExamApplicationService.java`
3. `src/main/java/com/jexam/app/JExamSelectionModel.java`
4. `src/main/java/com/jexam/generation/PdfBoxGenerationService.java`

Plan closure status:

- Refactoring/metrics hardening plan completed for this cycle with documented
	residual backlog and repeatable measurement commands.

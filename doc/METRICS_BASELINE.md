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

# Metrics Snapshot

This page records the current build metrics from the latest generated reports in `target/`.

## JaCoCo Summary

| Metric | Value |
| --- | --- |
| Instructions covered | 3,777 of 14,755 |
| Instruction coverage | 26% |
| Branches covered | 303 of 1,156 |
| Branch coverage | 26% |
| Lines covered | 837 of 3,314 |
| Line coverage | 25% |
| Methods covered | 196 of 712 |
| Method coverage | 28% |
| Classes covered | 21 of 61 |
| Class coverage | 34% |

## Package Coverage Highlights

### Stronger Areas

| Package | Instruction coverage | Branch coverage |
| --- | --- | --- |
| `com.jexam.model.enums` | 100% | n/a |
| `com.jexam.model` | 97% | 63% |
| `com.jexam.io` | 96% | 90% |
| `com.jexam.validation` | 86% | 76% |
| `com.jexam.generation` | 84% | 58% |
| `com.jexam.app` | 23% | 32% |

### Weakest Areas

| Package | Instruction coverage | Branch coverage |
| --- | --- | --- |
| `com.jexam.app.ui.components.pdf` | 0% | 0% |
| `com.jexam.app.ui.components.xml` | 0% | 0% |
| `com.jexam.app.ui.components` | 0% | 0% |
| `com.jexam.app.ui.base` | 0% | 0% |
| `com.jexam.app.ui` | 0% | n/a |
| `com.jexam.app.ui.styling` | 0% | 0% |

## Surefire Summary

| Metric | Value |
| --- | --- |
| Tests | 52 |
| Errors | 0 |
| Failures | 0 |
| Skipped | 0 |
| Success rate | 100% |

## Iteration Delta (2026-04-08)

- Line coverage increased from 22% to 25% (+3 percentage points).
- Branch coverage increased from 21% to 26% (+5 percentage points).
- Method coverage increased from 23% to 28% (+5 percentage points).
- Class coverage increased from 30% to 34% (+4 percentage points).

## Notes On Other Quality Tools

The current build configuration includes PMD, Checkstyle, and SpotBugs, but their current report artifacts were not present in the generated `target/` tree when this wiki was drafted. To avoid inventing numbers, this page only quotes the metrics that are present in the current build outputs.

## Regeneration Commands

    mvn package
    mvn verify

`mvn package` refreshes the current JaCoCo and Surefire outputs.

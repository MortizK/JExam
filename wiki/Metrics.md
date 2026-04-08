# Metrics Snapshot

This page records the current build metrics from the latest generated reports in `target/`.

## JaCoCo Summary

| Metric | Value |
| --- | --- |
| Instructions covered | 3,314 of 14,755 |
| Instruction coverage | 22% |
| Branches covered | 245 of 1,156 |
| Branch coverage | 21% |
| Lines covered | 735 of 3,314 |
| Line coverage | 22% |
| Methods covered | 156 of 712 |
| Method coverage | 23% |
| Classes covered | 18 of 61 |
| Class coverage | 30% |

## Package Coverage Highlights

### Stronger Areas

| Package | Instruction coverage | Branch coverage |
| --- | --- | --- |
| `com.jexam.io` | 92% | 88% |
| `com.jexam.model.enums` | 88% | n/a |
| `com.jexam.generation` | 83% | 58% |
| `com.jexam.model` | 73% | 50% |
| `com.jexam.validation` | 68% | 60% |

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
| Tests | 28 |
| Errors | 0 |
| Failures | 0 |
| Skipped | 0 |
| Success rate | 100% |

## Notes On Other Quality Tools

The current build configuration includes PMD, Checkstyle, and SpotBugs, but their current report artifacts were not present in the generated `target/` tree when this wiki was drafted. To avoid inventing numbers, this page only quotes the metrics that are present in the current build outputs.

## Regeneration Commands

    mvn package
    mvn verify

`mvn package` refreshes the current JaCoCo and Surefire outputs.

# PDF Generation

This page documents the generation subsystem in `com.jexam.generation` and its orchestration through `ExamApplicationService`.

## Components

- `PdfGenerationService`: generation interface contract.
- `PdfBoxGenerationService`: PDFBox-backed implementation.
- `GenerationMode`: output mode selection (`EXAM`, `MOCK_EXAM`, `SOLUTION`).

## Orchestration

`ExamApplicationService` drives generation workflows:

- `generatePdf(mode, outputPath)` for single export
- `generatePdfPair(mode, outputPath)` for primary plus solutions file
- `generatePreviewPdf(mode)` for temporary preview rendering
- `generatePdfPairFromLastPreview(mode, outputPath)` to reuse already-rendered preview content when valid

## Chapter And Task Selection Rules

- Generation uses selected chapter order from `generationChapterIndices`.
- Chapters can be included/excluded and reordered in the UI.
- For `EXAM`, tasks are filtered to `Scope.EXAM`.
- For `MOCK_EXAM`, all scoped tasks are eligible.
- Goal points are chapter-specific and normalized to half-points.

## Determinism

Generation can be deterministic via optional random seed (`setGenerationRandomSeed`). Matching seed and source content produce stable variant selection.

## Warnings Versus Errors

- Validation failures are hard failures.
- Difficulty-balance checks can produce warnings while still generating output.
- Preview/export warnings are surfaced to users as informational messages.

## Rendering Guarantees Verified In Tests

Current tests verify:

- PDFs are generated for exam/mock/solution modes.
- Scope filtering behavior is correct.
- Cover page and chapter page breaks are present.
- Answer-box height scales with expected answer length.
- Deterministic seed behavior is reproducible.

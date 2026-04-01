# Backend Requirement Traceability and Gap Matrix

Source scope for this phase: .tex files only under `doc/LaTeX`.

## Requirement Inventory

- REQ-01 (UC-01): App starts with a new exam structure in memory.
- REQ-02 (UC-02): Open XML must validate structure and fail on corrupt/invalid files.
- REQ-03 (UC-03): Save XML persists current exam and reports write failures.
- REQ-04 (UC-04): Create chapter with defaults.
- REQ-05 (UC-05): Edit chapter metadata.
- REQ-06 (UC-06): Delete chapter with user confirmation (UI concern) and cascading model removal.
- REQ-07 (UC-07): Create task under chapter with default variant.
- REQ-08 (UC-08): Edit task points, difficulty, and scope; validate allowed values.
- REQ-09 (UC-09): Delete task.
- REQ-10 (UC-10): Create variant with defaults.
- REQ-11 (UC-11): Variant question must not be empty.
- REQ-12 (UC-12): Variant deletion allowed only if task still has at least one variant.
- REQ-13 (UC-13): Exam configuration access requires at least one generatable chapter/task.
- REQ-14 (UC-14): Chapter order is configurable; excluded chapters are not generated.
- REQ-15 (UC-15): Generate exam PDF; precondition includes documented difficulty 33% rule.
- REQ-16 (UC-16): Generate solution PDF including answers.
- REQ-17 (UC-17): Generate mock exam PDF from mock scope only.
- REQ-18 (UC-18): Preview generated PDF before export.
- REQ-19 (UC-19): Language switch with fallback behavior.
- REQ-20 (Lexicon): Default values: New Exam, New Chapter, New Subtask, points=1, difficulty=easy, scope=exam, question/answer defaults.
- REQ-21 (Lexicon/Intro): Validation blocks PDF generation for invalid exam data.

## Gap Matrix

| Requirement | Backend Status | Evidence | Tests | Gap Action |
|---|---|---|---|---|
| REQ-01 | Implemented | `ExamApplicationService.createDefaultExam()` | Covered indirectly | Keep |
| REQ-02 | Implemented | `ExamXmlLoader`, `ExamPersistenceService.loadValidated` | `ExamPersistenceServiceTest`, `ExamXmlRoundTripTest` | Keep |
| REQ-03 | Implemented | `ExamPersistenceService.saveValidated` | `ExamPersistenceServiceTest` | Keep |
| REQ-04 | Implemented | `ExamApplicationService.addChapter` and chapter remove flow | `ExamApplicationServiceTest.addAndRemoveChapterShouldUpdateExamStructure` | Keep |
| REQ-07 | Implemented | `ExamApplicationService.addTask` and task remove flow | `ExamApplicationServiceTest.addTaskAndUpdateTaskDetailsShouldPersistChanges`, `ExamApplicationServiceTest.removeTaskShouldDeleteRequestedTask` | Keep |
| REQ-08 | Implemented | `updateTaskDetails`, `ExamValidator` task checks including 0.5-point increments | `ExamValidatorTest` includes invalid points increment case | Keep |
| REQ-11 | Implemented | `ExamValidator.validateVariant` | `ExamValidatorTest.blankVariantQuestionShouldFail` | Keep |
| REQ-12 | Implemented | `ExamApplicationService.removeVariant` rejects removing the last variant | `ExamApplicationServiceTest.removeVariantShouldRejectDeletingLastVariant` | Keep |
| REQ-15 | Implemented | `ExamApplicationService.generatePdf` enforces difficulty thirds for exam/solution generation | `ExamApplicationServiceTest.generatePdfShouldRejectWhenDifficultyIsNotInThirds`, `ExamApplicationServiceTest.generatePdfShouldAllowBalancedDifficultyThirds` | Keep |
| REQ-16 | Implemented | `GenerationMode.SOLUTION` writes answers | `PdfBoxGenerationServiceTest.shouldIncludeAnswersInSolutionPdf` | Keep |
| REQ-17 | Implemented | `shouldIncludeTask` filters by scope | `PdfBoxGenerationServiceTest.shouldGenerateMockExamPdf` | Keep |
| REQ-14 | Implemented | `ExamApplicationService` provides chapter generation selection/order/exclusion API and generation uses selected subset | `ExamApplicationServiceTest.generatePdfShouldRespectConfiguredChapterOrderAndExclusion`, `ExamApplicationServiceTest.generatePdfShouldRejectWhenNoGenerationChapterIsSelected` | Keep |
| REQ-18 | Implemented | `ExamApplicationService.generatePreviewPdf` integrated in `JExamApp` preview action with system file open fallback | `ExamApplicationServiceTest.generatePreviewPdfShouldCreateReadableTemporaryPdf` | Keep |
| REQ-19 | Implemented | `UiLanguage` selector integrated into `JExamApp` and `JExamUiSupport` uses key-based dictionaries with English fallback | Manual UI verification; fallback logic covered by `JExamUiSupport.text` behavior | Keep |
| REQ-20 | Implemented | `ExamApplicationService.defaultTask` aligns with lexicon defaults | `ExamApplicationServiceTest.newExamShouldUseDocumentedDefaultTaskValues` | Keep |

## Prioritized Backlog (Backend)

1. Completed: REQ-12 enforced in application service.
2. Completed: REQ-20 default task values aligned to specification.
3. Completed: Preview API integrated into JavaFX flow with user-facing preview action.
4. Completed: REQ-19 language switch with English fallback.

## Milestone Criteria for This Implementation Start

- First TDD slice passes with green tests for REQ-12 and REQ-20.
- Existing tests continue to pass.
- Traceability document exists and links concrete requirement IDs to code/test evidence.

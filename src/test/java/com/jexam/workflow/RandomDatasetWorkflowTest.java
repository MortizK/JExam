package com.jexam.workflow;

import com.jexam.io.ExamPersistenceService;
import com.jexam.io.ExamXmlLoader;
import com.jexam.io.ExamXmlWriter;
import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import com.jexam.validation.ExamValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomDatasetWorkflowTest {
    private static final int ITERATIONS = 1000;

    @TempDir
    Path tempDir;

    @Test
    void randomizedDatasetRoundTripShouldRemainValidAcrossManySizes() throws Exception {
        Random random = new Random(20260505L);
        ExamValidator validator = new ExamValidator();
        ExamPersistenceService persistenceService = new ExamPersistenceService(
            new ExamXmlLoader(),
            new ExamXmlWriter(),
            validator
        );

        for (int iteration = 0; iteration < ITERATIONS; iteration++) {
            Exam exam = randomExam(random, iteration);
            assertTrue(validator.validate(exam).isValid(), "Random exam should be valid at iteration " + iteration);

            Path xmlPath = tempDir.resolve(String.format("random-%04d.xml", iteration));
            persistenceService.saveValidated(exam, xmlPath);
            assertTrue(Files.exists(xmlPath));

            Exam loaded = persistenceService.loadValidated(xmlPath);
            assertEquals(exam, loaded, "Roundtrip should preserve the exam at iteration " + iteration);
        }
    }

    private Exam randomExam(final Random random, final int iteration) {
        List<Chapter> chapters = new ArrayList<>();
        int chapterCount = 1 + random.nextInt(3);

        for (int chapterIndex = 0; chapterIndex < chapterCount; chapterIndex++) {
            List<Task> tasks = new ArrayList<>();
            int taskCount = 1 + random.nextInt(4);

            for (int taskIndex = 0; taskIndex < taskCount; taskIndex++) {
                List<Variant> variants = new ArrayList<>();
                int variantCount = 1 + random.nextInt(3);

                for (int variantIndex = 0; variantIndex < variantCount; variantIndex++) {
                    variants.add(new Variant(
                        "Question " + iteration + "-" + chapterIndex + "-" + taskIndex + "-" + variantIndex,
                        "Answer " + iteration + "-" + chapterIndex + "-" + taskIndex + "-" + variantIndex
                    ));
                }

                tasks.add(new Task(
                    "Task " + iteration + "-" + chapterIndex + "-" + taskIndex,
                    0.5 * (1 + random.nextInt(10)),
                    Difficulty.values()[random.nextInt(Difficulty.values().length)],
                    Scope.values()[random.nextInt(Scope.values().length)],
                    variants
                ));
            }

            chapters.add(new Chapter("Chapter " + iteration + "-" + chapterIndex, tasks));
        }

        return new Exam("Random Exam " + iteration, chapters);
    }
}
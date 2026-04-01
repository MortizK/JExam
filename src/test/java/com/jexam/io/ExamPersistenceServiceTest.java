package com.jexam.io;

import com.jexam.TestFixtures;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import com.jexam.validation.ExamValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamPersistenceServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void saveValidatedShouldRejectInvalidExam() {
        Exam exam = TestFixtures.validExam();
        exam.getChapters().get(0).addTask(new Task("Bad", 0.0, Difficulty.EASY, Scope.EXAM, List.of()));

        ExamPersistenceService service = new ExamPersistenceService(
            new ExamXmlLoader(),
            new ExamXmlWriter(),
            new ExamValidator()
        );

        assertThrows(ExamXmlException.class, () -> service.saveValidated(exam, tempDir.resolve("invalid.xml")));
    }

    @Test
    void loadValidatedShouldLoadValidXml() throws Exception {
        Exam exam = TestFixtures.validExam();
        ExamPersistenceService service = new ExamPersistenceService(
            new ExamXmlLoader(),
            new ExamXmlWriter(),
            new ExamValidator()
        );

        Path path = tempDir.resolve("valid.xml");
        service.saveValidated(exam, path);
        Exam loaded = service.loadValidated(path);

        assertTrue(loaded.getChapters().size() == 1);
    }
}

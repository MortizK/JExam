package com.jexam.io;

import com.jexam.TestFixtures;
import com.jexam.model.Exam;
import com.jexam.validation.ExamValidator;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExamPersistenceServiceDelegationTest {
    @Test
    void loadValidatedShouldDelegateToLoaderAndValidator() throws Exception {
        TrackingLoader loader = new TrackingLoader(TestFixtures.validExam());
        ExamPersistenceService service = new ExamPersistenceService(loader, new TrackingWriter(), new ExamValidator());

        Exam loaded = service.loadValidated(Path.of("ignored.xml"));

        assertSame(loader.exam, loaded);
        assertEquals(1, loader.loadCalls);
    }

    @Test
    void saveValidatedShouldDelegateToValidatorAndWriterForValidExam() throws Exception {
        TrackingWriter writer = new TrackingWriter();
        ExamPersistenceService service = new ExamPersistenceService(
            new TrackingLoader(TestFixtures.validExam()),
            writer,
            new ExamValidator()
        );

        service.saveValidated(TestFixtures.validExam(), Path.of("output.xml"));

        assertEquals(1, writer.writeCalls);
        assertEquals(Path.of("output.xml"), writer.lastPath);
    }

    @Test
    void saveValidatedShouldStopBeforeWriterWhenValidationFails() {
        TrackingWriter writer = new TrackingWriter();
        Exam invalidExam = new Exam(" ", TestFixtures.validExam().getChapters());
        ExamPersistenceService service = new ExamPersistenceService(
            new TrackingLoader(TestFixtures.validExam()),
            writer,
            new ExamValidator()
        );

        ExamXmlException exception = assertThrows(
            ExamXmlException.class,
            () -> service.saveValidated(invalidExam, Path.of("invalid.xml"))
        );

        assertEquals("Refusing to save invalid exam: [exam.name: Exam name must not be blank.]", exception.getMessage());
        assertEquals(0, writer.writeCalls);
    }

    private static final class TrackingLoader extends ExamXmlLoader {
        private final Exam exam;
        private int loadCalls;

        private TrackingLoader(final Exam exam) {
            this.exam = exam;
        }

        @Override
        public Exam load(final Path path) {
            loadCalls++;
            return exam;
        }
    }

    private static final class TrackingWriter extends ExamXmlWriter {
        private int writeCalls;
        private Path lastPath;

        @Override
        public void write(final Exam exam, final Path path) {
            writeCalls++;
            lastPath = path;
        }
    }
}
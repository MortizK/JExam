package com.jexam.generation;

import com.jexam.model.Exam;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StubPdfGenerationServiceTest {
    @Test
    void generateShouldAlwaysThrowUnsupportedOperationException() {
        StubPdfGenerationService service = new StubPdfGenerationService();
        Exam exam = new Exam("Demo", List.of());

        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> service.generate(exam, GenerationMode.EXAM, Path.of("ignored.pdf"))
        );

        assertEquals("PDF generation is planned for phase 3.", exception.getMessage());
    }
}

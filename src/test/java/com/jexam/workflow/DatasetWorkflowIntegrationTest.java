package com.jexam.workflow;

import com.jexam.generation.GenerationMode;
import com.jexam.generation.PdfBoxGenerationService;
import com.jexam.generation.PdfGenerationService;
import com.jexam.io.ExamPersistenceService;
import com.jexam.io.ExamXmlLoader;
import com.jexam.io.ExamXmlWriter;
import com.jexam.model.Exam;
import com.jexam.validation.ExamValidator;
import com.jexam.validation.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetWorkflowIntegrationTest {
    @TempDir
    Path tempDir;

    @Test
    void validDatasetShouldRoundTripAndGeneratePdfArtifacts() throws Exception {
        Path source = copyResourceToTemp("/dataSetValid.xml", "valid-source.xml");

        ExamPersistenceService persistenceService = new ExamPersistenceService(
            new ExamXmlLoader(),
            new ExamXmlWriter(),
            new ExamValidator()
        );
        Exam loaded = persistenceService.loadValidated(source);

        assertEquals("New Exam", loaded.getName());
        assertEquals(3, loaded.chapterCount());

        Path saved = tempDir.resolve("valid-roundtrip.xml");
        persistenceService.saveValidated(loaded, saved);
        assertTrue(Files.exists(saved));

        PdfGenerationService pdfService = new PdfBoxGenerationService();
        Path examPdf = tempDir.resolve("valid-exam.pdf");
        Path mockPdf = tempDir.resolve("valid-mock.pdf");
        pdfService.generate(loaded, GenerationMode.EXAM, examPdf);
        pdfService.generate(loaded, GenerationMode.MOCK_EXAM, mockPdf);

        assertTrue(Files.size(examPdf) > 0);
        assertTrue(Files.size(mockPdf) > 0);
    }

    @Test
    void largeDatasetShouldBeParseableAndValid() throws Exception {
        Path source = copyResourceToTemp("/dataSet.xml", "large-source.xml");

        Exam loaded = new ExamXmlLoader().load(source);
        ValidationResult result = new ExamValidator().validate(loaded);

        assertTrue(result.isValid());
        assertTrue(loaded.chapterCount() >= 2);
        assertTrue(loaded.chapterAt(0).taskCount() >= 10);

        Path output = tempDir.resolve("large-mock.pdf");
        new PdfBoxGenerationService().generate(loaded, GenerationMode.MOCK_EXAM, output);
        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    private Path copyResourceToTemp(String resourceName, String targetFileName) throws Exception {
        InputStream stream = getClass().getResourceAsStream(resourceName);
        assertNotNull(stream, "Missing test resource: " + resourceName);

        Path target = tempDir.resolve(targetFileName);
        try (InputStream input = stream) {
            Files.copy(input, target);
        }
        return target;
    }
}

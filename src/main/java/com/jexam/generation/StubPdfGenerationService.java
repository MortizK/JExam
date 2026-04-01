package com.jexam.generation;

import com.jexam.model.Exam;

import java.nio.file.Path;

/**
 * Stub generator used where no real PDF implementation should run.
 */
public final class StubPdfGenerationService implements PdfGenerationService {
    /**
     * Always throws because this is intentionally a stub.
     *
     * @param exam exam model
     * @param mode generation mode
     * @param outputPath output path
     */
    @Override
    public void generate(
        final Exam exam,
        final GenerationMode mode,
        final Path outputPath
    ) {
        throw new UnsupportedOperationException(
            "PDF generation is planned for phase 3."
        );
    }
}

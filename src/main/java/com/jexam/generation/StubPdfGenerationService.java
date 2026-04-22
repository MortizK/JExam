package com.jexam.generation;

import com.jexam.model.Exam;

import java.nio.file.Path;

/**
 * Stub generator used where no real PDF implementation should run.
 *
 * @author Moritz
 */
public final class StubPdfGenerationService implements PdfGenerationService {
    /**
     * {@inheritDoc}
     *
     * Always throws because this is intentionally a stub.
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

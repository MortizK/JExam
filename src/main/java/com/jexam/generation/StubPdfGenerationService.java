package com.jexam.generation;

import com.jexam.model.Exam;

import java.nio.file.Path;

public class StubPdfGenerationService implements PdfGenerationService {
    @Override
    public void generate(Exam exam, GenerationMode mode, Path outputPath) {
        throw new UnsupportedOperationException("PDF generation is planned for phase 3.");
    }
}

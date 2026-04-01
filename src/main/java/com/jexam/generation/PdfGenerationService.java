package com.jexam.generation;

import com.jexam.model.Exam;

import java.nio.file.Path;

/**
 * Contract for exam PDF generation.
 */
public interface PdfGenerationService {
    /**
     * Generates a PDF representation of an exam.
     *
     * @param exam exam model
     * @param mode output mode
     * @param outputPath destination path
     */
    void generate(Exam exam, GenerationMode mode, Path outputPath);
}

package com.jexam.generation;

import com.jexam.model.Exam;

import java.nio.file.Path;

public interface PdfGenerationService {
    void generate(Exam exam, GenerationMode mode, Path outputPath);
}

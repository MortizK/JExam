package com.jexam.io;

import com.jexam.model.Exam;
import com.jexam.validation.ExamValidator;
import com.jexam.validation.ValidationResult;

import java.nio.file.Path;

public class ExamPersistenceService {
    private final ExamXmlLoader loader;
    private final ExamXmlWriter writer;
    private final ExamValidator validator;

    public ExamPersistenceService(ExamXmlLoader loader, ExamXmlWriter writer, ExamValidator validator) {
        this.loader = loader;
        this.writer = writer;
        this.validator = validator;
    }

    public Exam loadValidated(Path path) throws ExamXmlException {
        Exam exam = loader.load(path);
        ValidationResult result = validator.validate(exam);
        if (!result.isValid()) {
            throw new ExamXmlException("Loaded XML is invalid: " + result.getErrors());
        }
        return exam;
    }

    public void saveValidated(Exam exam, Path path) throws ExamXmlException {
        ValidationResult result = validator.validate(exam);
        if (!result.isValid()) {
            throw new ExamXmlException("Refusing to save invalid exam: " + result.getErrors());
        }
        writer.write(exam, path);
    }
}

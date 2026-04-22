package com.jexam.io;

import com.jexam.model.Exam;
import com.jexam.validation.ExamValidator;
import com.jexam.validation.ValidationResult;

import java.nio.file.Path;

/**
 * High-level persistence service combining XML I/O and validation policy.
 *
 * @author Moritz
 */
public class ExamPersistenceService {
    /**
     * XML loader dependency.
     */
    private final ExamXmlLoader loader;

    /**
     * XML writer dependency.
     */
    private final ExamXmlWriter writer;

    /**
     * Validation dependency.
     */
    private final ExamValidator validator;

    /**
     * Creates the persistence service.
     *
     * @param xmlLoader XML loader
     * @param xmlWriter XML writer
     * @param examValidator exam validator
     */
    public ExamPersistenceService(
        final ExamXmlLoader xmlLoader,
        final ExamXmlWriter xmlWriter,
        final ExamValidator examValidator
    ) {
        this.loader = xmlLoader;
        this.writer = xmlWriter;
        this.validator = examValidator;
    }

    /**
     * Loads an exam from XML and validates it.
     *
     * @param path XML path
     * @return validated exam
     * @throws com.jexam.io.ExamXmlException when loading or validation fails
     */
    public Exam loadValidated(final Path path) throws ExamXmlException {
        final Exam exam = loader.load(path);
        final ValidationResult result = validator.validate(exam);
        if (!result.isValid()) {
            throw new ExamXmlException(
                "Loaded XML is invalid: " + result.getErrors()
            );
        }
        return exam;
    }

    /**
     * Validates and saves an exam to XML.
     *
     * @param exam exam instance
     * @param path output path
     * @throws com.jexam.io.ExamXmlException when validation or writing fails
     */
    public void saveValidated(final Exam exam, final Path path)
        throws ExamXmlException {
        final ValidationResult result = validator.validate(exam);
        if (!result.isValid()) {
            throw new ExamXmlException(
                "Refusing to save invalid exam: " + result.getErrors()
            );
        }
        writer.write(exam, path);
    }
}

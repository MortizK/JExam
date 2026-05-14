package com.jexam.io;

import com.jexam.model.Exam;
import com.jexam.validation.ExamValidator;
import com.jexam.validation.ValidationResult;

import java.nio.file.Path;

/**
 * High-level persistence service combining XML I/O with validation policy.
 * 
 * This service enforces the invariant that all exams loaded from or saved to disk
 * are validated. It delegates to {@link ExamXmlLoader} and {@link ExamXmlWriter} for
 * XML handling and to {@link ExamValidator} for validation, ensuring clients do not
 * bypass validation by calling those classes directly.
 * 
 * The service is stateless and thread-safe (delegates are also stateless).
 *
 * @author Moritz
 */
public class ExamPersistenceService {
    /**
     * XML loader dependency (null-safe, assigned in constructor).
     */
    private final ExamXmlLoader loader;

    /**
     * XML writer dependency (null-safe, assigned in constructor).
     */
    private final ExamXmlWriter writer;

    /**
     * Validation dependency (null-safe, assigned in constructor).
     */
    private final ExamValidator validator;

    /**
     * Creates the persistence service with injected dependencies.
     * 
     * @param xmlLoader XML loader for parsing files (must not be null)
     * @param xmlWriter XML writer for serializing files (must not be null)
     * @param examValidator exam validator for validation policy (must not be null)
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
     * Loads an exam from XML and validates it before returning.
     * 
     * Delegates to XML loader to parse the file, then validates the result.
     * If validation fails, throws ExamXmlException with descriptive error list.
     * This ensures clients cannot bypass validation by calling loader directly.
     *
     * @param path XML file path to load
     * @return fully-loaded and validated Exam instance
     * @throws ExamXmlException if XML parsing fails or loaded exam is invalid
     */
    public Exam loadValidated(final Path path) throws ExamXmlException {
        // Parse XML file to Exam object
        final Exam exam = loader.load(path);
        // Validate loaded exam
        final ValidationResult result = validator.validate(exam);
        // If validation failed, report all errors and reject the load
        if (!result.isValid()) {
            throw new ExamXmlException(
                "Loaded XML is invalid: " + result.getErrors()
            );
        }
        return exam;
    }

    /**
     * Validates an exam and saves it to XML if validation succeeds.
     * 
     * Validates the exam first, then delegates to XML writer to serialize.
     * If validation fails, throws ExamXmlException without writing to disk.
     * This ensures corrupt exams are never persisted.
     *
     * @param exam exam instance to save
     * @param path output XML file path
     * @throws ExamXmlException if validation fails or XML writing fails
     */
    public void saveValidated(final Exam exam, final Path path)
        throws ExamXmlException {
        // Validate exam before writing
        final ValidationResult result = validator.validate(exam);
        // If validation failed, refuse to save and report all errors
        if (!result.isValid()) {
            throw new ExamXmlException(
                "Refusing to save invalid exam: " + result.getErrors()
            );
        }
        // Validation passed; write exam to file
        writer.write(exam, path);
    }
}

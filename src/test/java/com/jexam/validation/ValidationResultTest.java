package com.jexam.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationResultTest {
    @Test
    void validationErrorShouldExposeFieldsAndSupportEquality() {
        ValidationError first = new ValidationError("exam.name", "must not be blank");
        ValidationError same = new ValidationError("exam.name", "must not be blank");
        ValidationError different = new ValidationError("exam.chapters", "must not be empty");

        assertEquals("exam.name", first.getPath());
        assertEquals("must not be blank", first.getMessage());
        assertEquals("exam.name: must not be blank", first.toString());
        assertEquals(first, first);
        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, null);
        assertNotEquals(first, "not-a-validation-error");
        assertNotEquals(first, different);
    }

    @Test
    void addErrorAndAddAllShouldAggregateAndKeepListImmutable() {
        ValidationResult first = new ValidationResult();
        ValidationResult second = new ValidationResult();

        assertTrue(first.isValid());

        first.addError("chapter[0].name", "must not be blank");
        second.addError("chapter[0].tasks", "must not be empty");
        first.addAll(second);

        assertFalse(first.isValid());
        assertEquals(2, first.getErrors().size());
        assertEquals("chapter[0].name", first.getErrors().get(0).getPath());
        assertEquals("chapter[0].tasks", first.getErrors().get(1).getPath());

        assertThrows(
            UnsupportedOperationException.class,
            () -> first.getErrors().add(new ValidationError("x", "y"))
        );
    }

    @Test
    void addAllShouldHandleEmptySourceResult() {
        ValidationResult target = new ValidationResult();
        ValidationResult empty = new ValidationResult();

        target.addError("exam", "invalid");
        target.addAll(empty);

        assertEquals(1, target.getErrors().size());
    }
}

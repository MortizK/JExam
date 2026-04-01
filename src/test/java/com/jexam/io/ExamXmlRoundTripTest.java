package com.jexam.io;

import com.jexam.TestFixtures;
import com.jexam.model.Exam;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamXmlRoundTripTest {
    @TempDir
    Path tempDir;

    @Test
    void examShouldRoundTripViaXml() throws Exception {
        ExamXmlWriter writer = new ExamXmlWriter();
        ExamXmlLoader loader = new ExamXmlLoader();

        Exam original = TestFixtures.validExam();
        Path xmlPath = tempDir.resolve("exam.xml");

        writer.write(original, xmlPath);
        Exam loaded = loader.load(xmlPath);

        assertTrue(Files.exists(xmlPath));
        assertEquals(original, loaded);
    }
}

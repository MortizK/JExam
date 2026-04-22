package com.jexam.io;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamXmlLoaderWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void writerShouldCreateParentDirectoriesAndPreserveContent() throws Exception {
        ExamXmlWriter writer = new ExamXmlWriter();
        ExamXmlLoader loader = new ExamXmlLoader();
        Path nestedPath = tempDir.resolve(Path.of("deep", "nested", "exam.xml"));

        Exam exam = sampleExam();
        writer.write(exam, nestedPath);

        assertTrue(Files.exists(nestedPath));
        assertEquals(exam, loader.load(nestedPath));
    }

    @Test
    void loaderShouldRejectUnsupportedRootElement() throws Exception {
        Path invalid = tempDir.resolve("invalid-root.xml");
        Files.writeString(invalid, "<not-exam name='x'/>");

        ExamXmlException exception = assertThrows(
            ExamXmlException.class,
            () -> new ExamXmlLoader().load(invalid)
        );

        assertEquals("Root element must be <exam>.", exception.getMessage());
    }

  @Test
  void loaderShouldRejectMalformedXmlDocuments() throws Exception {
    Path malformed = tempDir.resolve("malformed.xml");
    Files.writeString(malformed, "<exam><chapter></exam>");

    ExamXmlException exception = assertThrows(
      ExamXmlException.class,
      () -> new ExamXmlLoader().load(malformed)
    );

    assertEquals("Failed to parse exam XML.", exception.getMessage());
  }

    @Test
    void loaderShouldRejectInvalidEnumValues() throws Exception {
        Path invalid = tempDir.resolve("invalid-enum.xml");
        Files.writeString(
            invalid,
            """
                <exam name="Demo">
                  <chapter name="Chapter">
                    <task name="Task" points="1.0" difficulty="unknown" scope="exam">
                      <variant>
                        <question>Q</question>
                        <answer>A</answer>
                      </variant>
                    </task>
                  </chapter>
                </exam>
                """
        );

        ExamXmlException exception = assertThrows(
            ExamXmlException.class,
            () -> new ExamXmlLoader().load(invalid)
        );

        assertEquals("Invalid XML enum value.", exception.getMessage());
    }

    @Test
    void loaderShouldIgnoreWhitespaceNodesWhileParsing() throws Exception {
        Path xml = tempDir.resolve("whitespace.xml");
        Files.writeString(
            xml,
            """
                <exam name="Whitespace Demo">

                  <chapter name="Chapter">

                    <task name="Task" points="2.0" difficulty="easy" scope="exam">
                      <variant>
                        <question>
                          What?
                        </question>
                        <answer>
                          Yes.
                        </answer>
                      </variant>
                    </task>
                  </chapter>
                </exam>
                """
        );

        Exam loaded = new ExamXmlLoader().load(xml);
        assertEquals("Whitespace Demo", loaded.getName());
        assertEquals(1, loaded.chapterCount());
        assertEquals("Chapter", loaded.chapterAt(0).getName());
        assertEquals("Task", loaded.taskAt(0, 0).getName());
        assertEquals("What?", loaded.variantAt(0, 0, 0).getQuestion().trim());
    }

      @Test
      void writerShouldHandleRelativePathWithoutParent() throws Exception {
        ExamXmlWriter writer = new ExamXmlWriter();
        ExamXmlLoader loader = new ExamXmlLoader();
        Path path = Path.of("target", "tmp", "writer-no-parent.xml").getFileName();

        try {
          writer.write(sampleExam(), path);

          assertTrue(Files.exists(path));
          assertEquals(sampleExam(), loader.load(path));
        } finally {
          Files.deleteIfExists(path);
        }
      }

    private Exam sampleExam() {
        Variant variant = new Variant("What is XML?", "Structured text");
        Task task = new Task(
            "XML Task",
            2.0,
            Difficulty.EASY,
            Scope.EXAM,
            List.of(variant)
        );
        Chapter chapter = new Chapter("XML Chapter", List.of(task));
        return new Exam("XML Exam", List.of(chapter));
    }
}

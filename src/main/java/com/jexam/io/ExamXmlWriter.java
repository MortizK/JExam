package com.jexam.io;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Writes {@link Exam} instances to the JExam XML format.
 */
public class ExamXmlWriter {
    /**
     * Writes an exam as XML to the provided file path.
     *
     * @param exam exam model to serialize
     * @param path output XML path
     * @throws ExamXmlException if XML creation or file writing fails
     */
    public void write(final Exam exam, final Path path) throws ExamXmlException {
        try {
            Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument();
            Element examElement = createExamElement(document, exam);
            for (Chapter chapter : exam.getChapters()) {
                writeChapter(document, examElement, chapter);
            }

            writeDocument(document, path);
        } catch (ParserConfigurationException | IOException | TransformerException e) {
            throw new ExamXmlException("Failed to write exam XML.", e);
        }
    }

    private Element createExamElement(final Document document, final Exam exam) {
        Element examElement = document.createElement("exam");
        examElement.setAttribute("name", exam.getName());
        document.appendChild(examElement);
        return examElement;
    }

    private void writeChapter(
        final Document document,
        final Element examElement,
        final Chapter chapter
    ) {
        Element chapterElement = document.createElement("chapter");
        chapterElement.setAttribute("name", chapter.getName());
        examElement.appendChild(chapterElement);

        for (Task task : chapter.getTasks()) {
            writeTask(document, chapterElement, task);
        }
    }

    private void writeTask(
        final Document document,
        final Element chapterElement,
        final Task task
    ) {
        Element taskElement = document.createElement("task");
        taskElement.setAttribute("name", task.getName());
        taskElement.setAttribute("points", Double.toString(task.getPoints()));
        taskElement.setAttribute(
            "difficulty",
            task.getDifficulty().toXmlValue()
        );
        taskElement.setAttribute("scope", task.getScope().toXmlValue());
        chapterElement.appendChild(taskElement);

        for (Variant variant : task.getVariants()) {
            writeVariant(document, taskElement, variant);
        }
    }

    private void writeVariant(
        final Document document,
        final Element taskElement,
        final Variant variant
    ) {
        Element variantElement = document.createElement("variant");
        appendTextElement(
            document,
            variantElement,
            "question",
            variant.getQuestion()
        );
        appendTextElement(
            document,
            variantElement,
            "answer",
            variant.getAnswer()
        );
        taskElement.appendChild(variantElement);
    }

    private void appendTextElement(
        final Document document,
        final Element parent,
        final String tag,
        final String text
    ) {
        Element child = document.createElement(tag);
        child.setTextContent(text);
        parent.appendChild(child);
    }

    private void writeDocument(final Document document, final Path path)
        throws IOException, TransformerException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
            "{http://xml.apache.org/xslt}indent-amount",
            "2"
        );

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            transformer.transform(
                new DOMSource(document),
                new StreamResult(outputStream)
            );
        }
    }
}

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
 * Writes {@link com.jexam.model.Exam} instances to the JExam XML format.
 * 
 * Serializes the exam object graph to an XML document matching the hierarchical structure:
 * <pre>{@code
 * <exam name="...">
 *   <chapter name="...">
 *     <task name="..." points="..." difficulty="..." scope="...">
 *       <variant>
 *         <question>...</question>
 *         <answer>...</answer>
 *       </variant>
 *     </task>
 *   </chapter>
 * </exam>
 * }</pre>
 * 
 * Output is formatted with 2-space indentation for human readability. Parent directories
 * are created automatically if they do not exist. The writer is stateless and thread-safe.
 *
 * @author Moritz
 */
public class ExamXmlWriter {
    /**
     * Creates an XML writer instance.
     */
    public ExamXmlWriter() {
    }

    /**
     * Writes an exam model as XML to the provided file path.
     * 
     * Recursively traverses the exam hierarchy (chapters -> tasks -> variants),
     * creating DOM elements and attributes, then serializes the document to disk
     * with automatic directory creation and formatted output.
     *
     * @param exam exam model to serialize (must not be null)
     * @param path output XML file path (parent directories created automatically)
     * @throws ExamXmlException if DOM creation or file I/O fails
     */
    public void write(final Exam exam, final Path path) throws ExamXmlException {
        try {
            // Create empty XML document
            Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument();
            // Create root <exam> element and populate with chapters
            Element examElement = createExamElement(document, exam);
            // Recursively write each chapter to the exam element
            for (Chapter chapter : exam.getChapters()) {
                writeChapter(document, examElement, chapter);
            }

            // Serialize DOM tree to XML file
            writeDocument(document, path);
        } catch (ParserConfigurationException e) {
            throw new ExamXmlException(
                "Failed to create XML document builder: " + e.getMessage(),
                e
            );
        } catch (IOException e) {
            throw new ExamXmlException(
                "Failed to write XML file to " + path + ": " + e.getMessage(),
                e
            );
        } catch (TransformerException e) {
            throw new ExamXmlException(
                "Failed to transform XML document: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Creates the root <exam> element with name attribute.
     * 
     * @param document DOM document
     * @param exam exam instance to serialize
     * @return <exam> root element
     */
    private Element createExamElement(final Document document, final Exam exam) {
        Element examElement = document.createElement("exam");
        // Set exam name as attribute
        examElement.setAttribute("name", exam.getName());
        // Append to document as root element
        document.appendChild(examElement);
        return examElement;
    }

    /**
     * Writes a <chapter> element with all its tasks.
     * 
     * @param document DOM document
     * @param examElement parent <exam> element
     * @param chapter chapter to serialize
     */
    private void writeChapter(
        final Document document,
        final Element examElement,
        final Chapter chapter
    ) {
        // Create <chapter> element with name attribute
        Element chapterElement = document.createElement("chapter");
        chapterElement.setAttribute("name", chapter.getName());
        // Append chapter to exam element
        examElement.appendChild(chapterElement);

        // Recursively write each task in the chapter
        for (Task task : chapter.getTasks()) {
            writeTask(document, chapterElement, task);
        }
    }

    /**
     * Writes a <task> element with all its metadata and variants.
     * 
     * Task attributes: name, points (as double string), difficulty (enum value),
     * scope (enum value).
     * 
     * @param document DOM document
     * @param chapterElement parent <chapter> element
     * @param task task to serialize
     */
    private void writeTask(
        final Document document,
        final Element chapterElement,
        final Task task
    ) {
        // Create <task> element and set attributes from task object
        Element taskElement = document.createElement("task");
        taskElement.setAttribute("name", task.getName());
        // Convert double points to string (e.g., 2.5)
        taskElement.setAttribute("points", Double.toString(task.getPoints()));
        // Serialize difficulty enum to its XML string value (e.g., "easy", "medium")
        taskElement.setAttribute(
            "difficulty",
            task.getDifficulty().toXmlValue()
        );
        // Serialize scope enum to its XML string value (e.g., "exam", "mock-exam")
        taskElement.setAttribute("scope", task.getScope().toXmlValue());
        // Append task to chapter element
        chapterElement.appendChild(taskElement);

        // Recursively write each variant in the task
        for (Variant variant : task.getVariants()) {
            writeVariant(document, taskElement, variant);
        }
    }

    /**
     * Writes a <variant> element with <question> and <answer> children.
     * 
     * @param document DOM document
     * @param taskElement parent <task> element
     * @param variant variant to serialize
     */
    private void writeVariant(
        final Document document,
        final Element taskElement,
        final Variant variant
    ) {
        // Create <variant> element as container for question and answer
        Element variantElement = document.createElement("variant");
        // Append question and answer as text-containing child elements
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
        // Append variant to task element
        taskElement.appendChild(variantElement);
    }

    /**
     * Creates and appends a text-containing element to a parent.
     * 
     * Utility method to reduce repetition when creating simple text elements
     * like <question>question text</question>.
     * 
     * @param document DOM document
     * @param parent parent element to append to
     * @param tag element tag name (e.g., "question", "answer")
     * @param text element text content
     */
    private void appendTextElement(
        final Document document,
        final Element parent,
        final String tag,
        final String text
    ) {
        // Create element with specified tag name
        Element child = document.createElement(tag);
        // Set text content
        child.setTextContent(text);
        // Append to parent
        parent.appendChild(child);
    }

    /**
     * Serializes a DOM document to an XML file with formatted output.
     * 
     * Creates parent directories if they do not exist. Output is formatted with
     * 2-space indentation and tries-with-resources ensures the output stream is closed.
     * 
     * @param document DOM document to write
     * @param path target XML file path
     * @throws IOException if file creation or writing fails
     * @throws TransformerException if XML transformation fails
     */
    private void writeDocument(final Document document, final Path path)
        throws IOException, TransformerException {
        // Ensure parent directories exist (creates parent if needed)
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        // Create transformer and configure for human-readable output
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        // Enable indentation for readable XML
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // Set indent amount to 2 spaces per level
        transformer.setOutputProperty(
            "{http://xml.apache.org/xslt}indent-amount",
            "2"
        );

        // Write DOM document to file with auto-closing stream
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            transformer.transform(
                new DOMSource(document),
                new StreamResult(outputStream)
            );
        }
    }
}

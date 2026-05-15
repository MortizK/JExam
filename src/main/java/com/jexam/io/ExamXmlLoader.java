package com.jexam.io;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads {@link com.jexam.model.Exam} instances from the JExam XML format.
 * 
 * Parses a hierarchical XML document with structure:
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
 * The loader is stateless and thread-safe. XML parsing errors and enum value
 * errors are wrapped in {@link ExamXmlException} with descriptive messages.
 *
 * @author Moritz
 */
public class ExamXmlLoader {
    /**
     * Creates an XML loader instance.
     */
    public ExamXmlLoader() {
    }

    /**
     * Parses an exam XML file into an {@link com.jexam.model.Exam} object graph.
     * 
     * Performs a complete parse of the XML document, recursively building chapters,
     * tasks, and variants. All SAX parsing errors and enum conversion errors are
     * caught and wrapped in ExamXmlException for consistent error handling.
     *
     * @param path path to the XML file to parse
     * @return fully-populated Exam instance with all chapters, tasks, and variants
    * @throws ExamXmlException if XML parsing fails, root element is not {@code <exam>},
     *         or enum values (Difficulty, Scope) are invalid
     */
    public Exam load(final Path path) throws ExamXmlException {
        try {
            // Parse XML file and get root <exam> element
            Element examElement = loadExamRootElement(path);
            // Create Exam with name from attribute and recursively parsed chapters
            return new Exam(
                examElement.getAttribute("name"),
                parseChapters(examElement)
            );
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // Wrap XML parsing infrastructure errors
            throw new ExamXmlException("Failed to parse exam XML.", e);
        } catch (IllegalArgumentException e) {
            // Wrap enum parsing errors (e.g., invalid Difficulty value)
            throw new ExamXmlException("Invalid XML enum value.", e);
        }
    }

    /**
     * Loads and validates the root XML element.
     * 
    * Parses the XML document and verifies that the root element is named {@code <exam>}.
     * 
     * @param path path to XML file
     * @return root Element (guaranteed to be named "exam")
     * @throws ParserConfigurationException if XML parser cannot be configured
     * @throws SAXException if XML is malformed
     * @throws IOException if file cannot be read
    * @throws ExamXmlException if root element is not {@code <exam>}
     */
    private Element loadExamRootElement(final Path path)
        throws ParserConfigurationException, SAXException, IOException,
        ExamXmlException {
        // Parse XML document from file
        Document document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(path.toFile());
        // Normalize document to collapse text nodes and handle whitespace
        document.getDocumentElement().normalize();

        Element examElement = document.getDocumentElement();
        // Validate that root element is <exam>, not <chapter> or other tag
        if (!"exam".equals(examElement.getTagName())) {
            throw new ExamXmlException("Root element must be <exam>.");
        }
        return examElement;
    }

    /**
     * Recursively parses all <chapter> elements from the exam element.
     * 
     * Iterates through child nodes, filtering for Element nodes with tag "chapter",
     * and recursively parses each chapter's tasks.
     * 
     * @param examElement root <exam> element
     * @return list of parsed Chapter objects (may be empty)
     */
    private List<Chapter> parseChapters(final Element examElement) {
        List<Chapter> chapters = new ArrayList<>();
        NodeList chapterNodes = examElement.getChildNodes();
        // Iterate through all child nodes (includes text nodes, element nodes, etc.)
        for (int i = 0; i < chapterNodes.getLength(); i++) {
            // Filter for <chapter> elements; skip text nodes and other node types
            Element chapterElement = asElementWithTag(
                chapterNodes.item(i),
                "chapter"
            );
            if (chapterElement != null) {
                // Recursively parse chapter and its tasks
                chapters.add(parseChapter(chapterElement));
            }
        }
        return chapters;
    }

    /**
     * Parses a single <chapter> element and its tasks.
     * 
     * @param chapterElement <chapter> element to parse
     * @return Chapter object with name and all tasks
     */
    private Chapter parseChapter(final Element chapterElement) {
        // Extract chapter name from XML attribute
        String name = chapterElement.getAttribute("name");
        List<Task> tasks = new ArrayList<>();

        NodeList taskNodes = chapterElement.getChildNodes();
        // Iterate through child nodes and filter for <task> elements
        for (int i = 0; i < taskNodes.getLength(); i++) {
            Element taskElement = asElementWithTag(taskNodes.item(i), "task");
            if (taskElement != null) {
                // Recursively parse task and its variants
                tasks.add(parseTask(taskElement));
            }
        }

        return new Chapter(name, tasks);
    }

    /**
     * Parses a single <task> element and its variants.
     * 
     * Extracts task metadata (name, points, difficulty, scope) from attributes
     * and recursively parses all <variant> child elements.
     * 
     * @param taskElement <task> element to parse
     * @return Task object with metadata and all variants
     * @throws IllegalArgumentException if Difficulty or Scope enum values are invalid
     */
    private Task parseTask(final Element taskElement) {
        // Extract task attributes from XML
        String name = taskElement.getAttribute("name");
        // Parse points as double (may raise NumberFormatException if not a valid number)
        double points = Double.parseDouble(taskElement.getAttribute("points"));
        // Parse difficulty enum from string (e.g., "easy", "medium", "hard")
        Difficulty difficulty = Difficulty.fromXmlValue(
            taskElement.getAttribute("difficulty")
        );
        // Parse scope enum from string (e.g., "exam", "mock-exam")
        Scope scope = Scope.fromXmlValue(taskElement.getAttribute("scope"));

        List<Variant> variants = new ArrayList<>();
        NodeList variantNodes = taskElement.getChildNodes();
        // Iterate through child nodes and filter for <variant> elements
        for (int i = 0; i < variantNodes.getLength(); i++) {
            Element variantElement = asElementWithTag(
                variantNodes.item(i),
                "variant"
            );
            if (variantElement != null) {
                // Parse variant with question and answer text
                variants.add(parseVariant(variantElement));
            }
        }

        return new Task(name, points, difficulty, scope, variants);
    }

    /**
     * Parses a single <variant> element (question-answer pair).
     * 
     * Iterates through child elements looking for <question> and <answer> tags,
     * extracting their text content.
     * 
     * @param variantElement <variant> element to parse
     * @return Variant object with question and answer text
     */
    private Variant parseVariant(final Element variantElement) {
        String question = null;
        String answer = null;

        NodeList children = variantElement.getChildNodes();
        // Iterate through all child nodes
        for (int i = 0; i < children.getLength(); i++) {
            Element childElement = asElement(children.item(i));
            // Skip non-element nodes (text nodes, comments, etc.)
            if (childElement == null) {
                continue;
            }
            // Extract question text from <question> child
            if ("question".equals(childElement.getTagName())) {
                question = childElement.getTextContent();
            }
            // Extract answer text from <answer> child
            if ("answer".equals(childElement.getTagName())) {
                answer = childElement.getTextContent();
            }
        }

        return new Variant(question, answer);
    }

    /**
     * Filters a node by type and tag name.
     * 
     * Returns the node as Element if it is an Element node with the specified tag name,
     * otherwise returns null. Used to skip text nodes and non-matching elements.
     * 
     * @param node DOM node to filter
     * @param tagName expected element tag name
     * @return Element if node matches, null otherwise
     */
    private Element asElementWithTag(final Node node, final String tagName) {
        Element element = asElement(node);
        // Return null if not an element or tag name doesn't match
        if (element == null || !tagName.equals(element.getTagName())) {
            return null;
        }
        return element;
    }

    /**
     * Converts a DOM node to an Element if possible.
     * 
     * @param node DOM node to convert
     * @return Element if node is an Element node, null otherwise
     */
    private Element asElement(final Node node) {
        // Return null for non-element nodes (text, comment, etc.)
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        return (Element) node;
    }
}

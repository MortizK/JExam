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
     * @param path path to the XML file
     * @return parsed exam instance
     * @throws com.jexam.io.ExamXmlException if parsing fails or XML values are invalid
     */
    public Exam load(final Path path) throws ExamXmlException {
        try {
            Element examElement = loadExamRootElement(path);
            return new Exam(
                examElement.getAttribute("name"),
                parseChapters(examElement)
            );
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ExamXmlException("Failed to parse exam XML.", e);
        } catch (IllegalArgumentException e) {
            throw new ExamXmlException("Invalid XML enum value.", e);
        }
    }

    private Element loadExamRootElement(final Path path)
        throws ParserConfigurationException, SAXException, IOException,
        ExamXmlException {
        Document document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(path.toFile());
        document.getDocumentElement().normalize();

        Element examElement = document.getDocumentElement();
        if (!"exam".equals(examElement.getTagName())) {
            throw new ExamXmlException("Root element must be <exam>.");
        }
        return examElement;
    }

    private List<Chapter> parseChapters(final Element examElement) {
        List<Chapter> chapters = new ArrayList<>();
        NodeList chapterNodes = examElement.getChildNodes();
        for (int i = 0; i < chapterNodes.getLength(); i++) {
            Element chapterElement = asElementWithTag(
                chapterNodes.item(i),
                "chapter"
            );
            if (chapterElement != null) {
                chapters.add(parseChapter(chapterElement));
            }
        }
        return chapters;
    }

    private Chapter parseChapter(final Element chapterElement) {
        String name = chapterElement.getAttribute("name");
        List<Task> tasks = new ArrayList<>();

        NodeList taskNodes = chapterElement.getChildNodes();
        for (int i = 0; i < taskNodes.getLength(); i++) {
            Element taskElement = asElementWithTag(taskNodes.item(i), "task");
            if (taskElement != null) {
                tasks.add(parseTask(taskElement));
            }
        }

        return new Chapter(name, tasks);
    }

    private Task parseTask(final Element taskElement) {
        String name = taskElement.getAttribute("name");
        double points = Double.parseDouble(taskElement.getAttribute("points"));
        Difficulty difficulty = Difficulty.fromXmlValue(
            taskElement.getAttribute("difficulty")
        );
        Scope scope = Scope.fromXmlValue(taskElement.getAttribute("scope"));

        List<Variant> variants = new ArrayList<>();
        NodeList variantNodes = taskElement.getChildNodes();
        for (int i = 0; i < variantNodes.getLength(); i++) {
            Element variantElement = asElementWithTag(
                variantNodes.item(i),
                "variant"
            );
            if (variantElement != null) {
                variants.add(parseVariant(variantElement));
            }
        }

        return new Task(name, points, difficulty, scope, variants);
    }

    private Variant parseVariant(final Element variantElement) {
        String question = null;
        String answer = null;

        NodeList children = variantElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Element childElement = asElement(children.item(i));
            if (childElement == null) {
                continue;
            }
            if ("question".equals(childElement.getTagName())) {
                question = childElement.getTextContent();
            }
            if ("answer".equals(childElement.getTagName())) {
                answer = childElement.getTextContent();
            }
        }

        return new Variant(question, answer);
    }

    private Element asElementWithTag(final Node node, final String tagName) {
        Element element = asElement(node);
        if (element == null || !tagName.equals(element.getTagName())) {
            return null;
        }
        return element;
    }

    private Element asElement(final Node node) {
        if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        return (Element) node;
    }
}

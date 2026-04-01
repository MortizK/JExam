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

public class ExamXmlLoader {
    public Exam load(Path path) throws ExamXmlException {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile());
            document.getDocumentElement().normalize();

            Element examElement = document.getDocumentElement();
            if (!"exam".equals(examElement.getTagName())) {
                throw new ExamXmlException("Root element must be <exam>.");
            }

            String examName = examElement.getAttribute("name");
            List<Chapter> chapters = new ArrayList<>();

            NodeList chapterNodes = examElement.getChildNodes();
            for (int i = 0; i < chapterNodes.getLength(); i++) {
                Node chapterNode = chapterNodes.item(i);
                if (chapterNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element chapterElement = (Element) chapterNode;
                if (!"chapter".equals(chapterElement.getTagName())) {
                    continue;
                }
                chapters.add(parseChapter(chapterElement));
            }

            return new Exam(examName, chapters);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ExamXmlException("Failed to parse exam XML.", e);
        } catch (IllegalArgumentException e) {
            throw new ExamXmlException("Invalid XML enum value.", e);
        }
    }

    private Chapter parseChapter(Element chapterElement) {
        String name = chapterElement.getAttribute("name");
        List<Task> tasks = new ArrayList<>();

        NodeList taskNodes = chapterElement.getChildNodes();
        for (int i = 0; i < taskNodes.getLength(); i++) {
            Node taskNode = taskNodes.item(i);
            if (taskNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element taskElement = (Element) taskNode;
            if (!"task".equals(taskElement.getTagName())) {
                continue;
            }
            tasks.add(parseTask(taskElement));
        }

        return new Chapter(name, tasks);
    }

    private Task parseTask(Element taskElement) {
        String name = taskElement.getAttribute("name");
        double points = Double.parseDouble(taskElement.getAttribute("points"));
        Difficulty difficulty = Difficulty.fromXmlValue(taskElement.getAttribute("difficulty"));
        Scope scope = Scope.fromXmlValue(taskElement.getAttribute("scope"));

        List<Variant> variants = new ArrayList<>();
        NodeList variantNodes = taskElement.getChildNodes();
        for (int i = 0; i < variantNodes.getLength(); i++) {
            Node variantNode = variantNodes.item(i);
            if (variantNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element variantElement = (Element) variantNode;
            if (!"variant".equals(variantElement.getTagName())) {
                continue;
            }
            variants.add(parseVariant(variantElement));
        }

        return new Task(name, points, difficulty, scope, variants);
    }

    private Variant parseVariant(Element variantElement) {
        String question = null;
        String answer = null;

        NodeList children = variantElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element childElement = (Element) child;
            if ("question".equals(childElement.getTagName())) {
                question = childElement.getTextContent();
            }
            if ("answer".equals(childElement.getTagName())) {
                answer = childElement.getTextContent();
            }
        }

        return new Variant(question, answer);
    }
}

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

public class ExamXmlWriter {
    public void write(Exam exam, Path path) throws ExamXmlException {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element examElement = document.createElement("exam");
            examElement.setAttribute("name", exam.getName());
            document.appendChild(examElement);

            for (Chapter chapter : exam.getChapters()) {
                Element chapterElement = document.createElement("chapter");
                chapterElement.setAttribute("name", chapter.getName());
                examElement.appendChild(chapterElement);

                for (Task task : chapter.getTasks()) {
                    Element taskElement = document.createElement("task");
                    taskElement.setAttribute("name", task.getName());
                    taskElement.setAttribute("points", Double.toString(task.getPoints()));
                    taskElement.setAttribute("difficulty", task.getDifficulty().toXmlValue());
                    taskElement.setAttribute("scope", task.getScope().toXmlValue());
                    chapterElement.appendChild(taskElement);

                    for (Variant variant : task.getVariants()) {
                        Element variantElement = document.createElement("variant");

                        Element questionElement = document.createElement("question");
                        questionElement.setTextContent(variant.getQuestion());
                        variantElement.appendChild(questionElement);

                        Element answerElement = document.createElement("answer");
                        answerElement.setTextContent(variant.getAnswer());
                        variantElement.appendChild(answerElement);

                        taskElement.appendChild(variantElement);
                    }
                }
            }

            writeDocument(document, path);
        } catch (ParserConfigurationException | IOException | TransformerException e) {
            throw new ExamXmlException("Failed to write exam XML.", e);
        }
    }

    private void writeDocument(Document document, Path path) throws IOException, TransformerException {
        Files.createDirectories(path.getParent());

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
        }
    }
}

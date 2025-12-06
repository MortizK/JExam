package dhbw.koehler.jexam.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import dhbw.koehler.jexam.service.EventService;

import java.io.IOException;

public class homeController {
    public Tab tabXML;
    public Tab tabPDF;
    public TabPane mainTabPane;
    @FXML SplitPane xmlBorderPane;
    @FXML SplitPane pdfBorderPane;

    public void initialize() {
        loadXMLTabContent();
        loadPDFTabContent();

        // Event Listener to update the Tabs, when unloaded
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
           if (newTab == tabXML) {
               EventService.triggerPdfUpdate();
           }
           if (newTab == tabPDF) {
               EventService.triggerXmlUpdate();
           }
        });
    }

    private void loadXMLTabContent() {
        try {
            // XML Tab FXML laden
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tabXML.fxml"));
            SplitPane xmlContent = loader.load();
            xmlContent.setDividerPosition(0, 0.15);

            // Optional: Zugriff auf den Controller
            tabXMLController controller = loader.getController();

            // In xmlBorderPane einfügen
            xmlBorderPane.getItems().clear();
            xmlBorderPane.getItems().add(xmlContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPDFTabContent() {
        try {
            // PDF Tab FXML laden
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tabPDF.fxml"));
            SplitPane pdfContent = loader.load();
            pdfContent.setDividerPosition(0, 0.4);

            // Optional: Zugriff auf den Controller
            tabPDFController controller = loader.getController();

            // In pdfBorderPane einfügen
            pdfBorderPane.getItems().clear();
            pdfBorderPane.getItems().add(pdfContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

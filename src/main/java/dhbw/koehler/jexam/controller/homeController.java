package dhbw.koehler.jexam.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class homeController {
    @FXML BorderPane xmlBorderPane;
    @FXML BorderPane pdfBorderPane;

    public void initialize() {
        loadXMLTabContent();
        loadPDFTabContent();
    }

    private void loadXMLTabContent() {
        try {
            // XML Tab FXML laden
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tabXML.fxml"));
            BorderPane xmlContent = loader.load();

            // Optional: Zugriff auf den Controller
            tabXMLController controller = loader.getController();

            // In xmlBorderPane einfügen
            xmlBorderPane.setCenter(xmlContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPDFTabContent() {
        try {
            // PDF Tab FXML laden
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tabPDF.fxml"));
            BorderPane pdfContent = loader.load();

            // Optional: Zugriff auf den Controller
            tabPDFController controller = loader.getController();

            // In pdfBorderPane einfügen
            pdfBorderPane.setCenter(pdfContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

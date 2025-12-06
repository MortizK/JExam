module dhbw.koehler.jexam {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;

    opens dhbw.koehler.jexam to javafx.fxml;
    opens dhbw.koehler.jexam.controller to javafx.fxml;
    exports dhbw.koehler.jexam;
}
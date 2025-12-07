module dhbw.koehler.jexam {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;

    opens jexam to javafx.fxml;
    opens jexam.controller to javafx.fxml;
    exports jexam;
}
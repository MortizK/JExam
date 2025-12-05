module dhbw.koehler.jexaminer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;

    opens dhbw.koehler.jexaminer to javafx.fxml;
    opens dhbw.koehler.jexaminer.controller to javafx.fxml;
    exports dhbw.koehler.jexaminer;
}
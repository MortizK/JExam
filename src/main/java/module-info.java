module dhbw.koehler.jexaminer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens dhbw.koehler.jexaminer to javafx.fxml;
    exports dhbw.koehler.jexaminer;
}
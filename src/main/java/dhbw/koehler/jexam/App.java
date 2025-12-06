package dhbw.koehler.jexam;

import dhbw.koehler.jexam.service.DataService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static DataService dataService = new DataService("My Exam");;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("JExam");
        stage.setScene(scene);
        stage.show();
    }

    public static DataService getDataService() {
        return dataService;
    }
}

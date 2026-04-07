import javafx.application.Application;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        var loader = new FXMLLoader(getClass().getResource("main.fxml"));

        final var scene = new Scene(loader.load(), 1050, 560);
        scene.getStylesheets().add(getClass().getResource("theme.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Task Manager");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
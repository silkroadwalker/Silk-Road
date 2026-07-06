package com.circlemarketplace.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setStage(stage);
        stage.setTitle("SilkRoad");
        SceneManager.switchScene("/fxml/login-view.fxml");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
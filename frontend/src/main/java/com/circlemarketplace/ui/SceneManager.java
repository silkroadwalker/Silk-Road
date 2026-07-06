package com.circlemarketplace.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.circlemarketplace.model.Ad;

import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;
    private static Ad selectedAd;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSelectedAd(Ad ad) {
        selectedAd = ad;
    }

    public static Ad getSelectedAd() {
        return selectedAd;
    }
}

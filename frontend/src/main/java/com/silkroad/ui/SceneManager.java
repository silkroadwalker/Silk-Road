package com.silkroad.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.silkroad.model.Ad;

import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;
    private static Ad selectedAd;
    private static Long selectedChatId;

    private static boolean viewingAsAdmin = false;
    private static String returnScene = "/fxml/home-view.fxml";


    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(SceneManager.class.getResource("/css/app.css").toExternalForm());
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

    public static void setSelectedChatId(Long chatId) {
        selectedChatId = chatId;
    }

    public static Long getSelectedChatId() {
        return selectedChatId;
    }

    public static void setViewingAsAdmin(boolean value) {
        viewingAsAdmin = value;
    }

    public static boolean isViewingAsAdmin() {
        return viewingAsAdmin;
    }

    public static void setReturnScene(String fxmlPath) {
        returnScene = fxmlPath;
    }

    public static String getReturnScene() {
        return returnScene;
    }
}
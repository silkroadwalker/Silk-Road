package com.circlemarketplace.ui;

import com.circlemarketplace.api.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showError("Please fill in both fields.");
            return;
        }

        try {
            ApiClient.ApiResult result = ApiClient.login(username, password);

            if (result.success) {
                Session.set(result.token, result.username, result.role);
                errorLabel.setVisible(false);
                System.out.println("Logged in as: " + result.username + " (" + result.role + ")");
                // TODO: navigate to main/home screen here
            } else {
                showError(result.message != null ? result.message : "Login failed.");
            }
        } catch (Exception e) {
            showError("Could not connect to server.");
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
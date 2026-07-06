package com.circlemarketplace.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.circlemarketplace.api.ApiClient;

public class SignupController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleSignup() {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();

        if (fullName.isBlank() || username.isBlank() || email.isBlank()
                || phone.isBlank() || password.isBlank()) {
            showError("Please fill in all fields.");
            return;
        }

        try {
            ApiClient.ApiResult result = ApiClient.signup(fullName, username, password, phone, email);

            if (result.success) {
                Session.set(result.token, result.username, result.role);
                errorLabel.setVisible(false);
                System.out.println("Signed up and logged in as: " + result.username);
                SceneManager.switchScene("/fxml/home-view.fxml"); // TODO
            } else {
                showError(result.message != null ? result.message : "Signup failed.");
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

    // for having an account
    @FXML
    private void goToLogin() {
        SceneManager.switchScene("/fxml/login-view.fxml");
    }
}
package com.silkroad.ui;

import com.silkroad.api.ApiClient;
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

    /**
     * Processes the login request initiated by the user.
     * <p>
     * The method validates that both username and password fields are filled,
     * sends the credentials to the backend authentication endpoint, and
     * updates the current session if authentication succeeds.
     * </p>
     * <p>
     * After a successful login, the user is redirected to the appropriate
     * dashboard based on their role. Administrators are taken to the admin
     * panel, while regular users are redirected to the home page.
     * Any authentication or connection errors are displayed through the
     * error label.
     * </p>
     */
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

                if ("ADMIN".equalsIgnoreCase(result.role)) {
                    SceneManager.switchScene("/fxml/admin-view.fxml");
                } else {
                    SceneManager.switchScene("/fxml/home-view.fxml");
                }
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

    /**
     * Navigates the user to the registration page.
     * <p>
     * This action is typically triggered when a user does not yet have an
     * account and chooses to create one from the login screen.
     * </p>
     */
    @FXML
    private void goToSignup() {
        SceneManager.switchScene("/fxml/signup-view.fxml");
    }
}
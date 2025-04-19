package ui;

import db.DBHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {
    public static int currentUserId; // <-- Added this line

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try (Connection conn = DBHelper.connect()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?");
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUserId = rs.getInt("id"); // <-- Save the logged-in user's ID
                messageLabel.setText("Login successful!");
                loadDashboard(); // Navigate to dashboard
            } else {
                messageLabel.setText("Invalid credentials.");
            }
        } catch (SQLException e) {
            messageLabel.setText("DB error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try (Connection conn = DBHelper.connect()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users(email, password) VALUES (?, ?)");
            stmt.setString(1, email);
            stmt.setString(2, password);

            stmt.executeUpdate();
            messageLabel.setText("Registered successfully. Please login.");
        } catch (SQLException e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/dashboard.fxml"));
            Scene dashboardScene = new Scene(loader.load(), 800, 500);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(dashboardScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

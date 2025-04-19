package ui;

import db.DBHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import model.Coin;

import java.util.List;

public class DashboardController {

    @FXML private TextField coinSymbolField;
    @FXML private TextField quantityField;
    @FXML private TableView<Coin> portfolioTable;
    @FXML private TableColumn<Coin, String> colPortSymbol;
    @FXML private TableColumn<Coin, Double> colPortQuantity;

    @FXML private TableView<Coin> marketTable;
    @FXML private TableColumn<Coin, String> colSymbol;
    @FXML private TableColumn<Coin, String> colName;
    @FXML private TableColumn<Coin, Double> colPrice;

    @FXML private TextField txtTempName;
    @FXML private TextField txtTempSymbol;
    @FXML private TextField txtTempQuantity;
    @FXML private TableView<Coin> tempPortfolioTable;
    @FXML private TableColumn<Coin, String> tempColSymbol;
    @FXML private TableColumn<Coin, Double> tempColQuantity;

    @FXML private Label lblPortfolioValue; // ✅ Label for main portfolio value
    @FXML private Label lblTempValue;      // ✅ Label for temp portfolio value

    @FXML
    public void initialize() {
        // Market table setup
        colSymbol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        List<Coin> coins = Coin.fetchTopCoins();
        if (coins != null) {
            ObservableList<Coin> coinList = FXCollections.observableArrayList(coins);
            marketTable.setItems(coinList);
        }

        // Portfolio table setup
        colPortSymbol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        colPortQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Temp portfolio table setup
        if (tempColSymbol != null && tempColQuantity != null) {
            tempColSymbol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
            tempColQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            tempPortfolioTable.setItems(FXCollections.observableArrayList());
        }

        loadPortfolio();
    }

    private void loadPortfolio() {
        List<Coin> portfolio = DBHelper.getUserPortfolio(LoginController.currentUserId);
        ObservableList<Coin> data = FXCollections.observableArrayList(portfolio);
        portfolioTable.setItems(data);
        updatePortfolioValue(data); // ✅ Update main value
    }

    private void updatePortfolioValue(ObservableList<Coin> portfolio) {
        double total = 0.0;
        for (Coin c : portfolio) {
            total += c.getValue(); // ✅ price * quantity
        }
        if (lblPortfolioValue != null) {
            lblPortfolioValue.setText(String.format("Total Value: $%.2f", total));
        }
    }

    private void updateTempPortfolioValue(ObservableList<Coin> tempPortfolio) {
        double total = 0.0;
        for (Coin c : tempPortfolio) {
            total += c.getValue(); // ✅ price * quantity
        }
        if (lblTempValue != null) {
            lblTempValue.setText(String.format("Temp Value: $%.2f", total));
        }
    }

    @FXML
    private void handleAddToPortfolio() {
        String symbol = coinSymbolField.getText().toUpperCase();
        String qtyText = quantityField.getText();
        if (symbol.isEmpty() || qtyText.isEmpty()) return;

        try {
            double quantity = Double.parseDouble(qtyText);
            DBHelper.addToPortfolio(LoginController.currentUserId, symbol, quantity, false);
            loadPortfolio();
            coinSymbolField.clear();
            quantityField.clear();
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity format.");
        }
    }

    // -------- TEMP PORTFOLIO METHODS --------

    @FXML
    private void handleAddToTemp() {
        String name = txtTempName.getText().trim();
        String symbol = txtTempSymbol.getText().trim();
        String qtyText = txtTempQuantity.getText().trim();

        if (name.isEmpty() || symbol.isEmpty() || qtyText.isEmpty()) {
            showAlert("Fill all temp portfolio fields.");
            return;
        }

        try {
            double qty = Double.parseDouble(qtyText);
            DBHelper.addToTempPortfolio(LoginController.currentUserId, symbol, qty, name);
            showAlert("Added to temp portfolio: " + name);
            txtTempSymbol.clear();
            txtTempQuantity.clear();
        } catch (NumberFormatException e) {
            showAlert("Invalid quantity.");
        }
    }

    @FXML
    private void handleViewTemp() {
        String name = txtTempName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Enter temp portfolio name.");
            return;
        }

        List<Coin> coins = DBHelper.getTempPortfolio(LoginController.currentUserId, name);
        ObservableList<Coin> data = FXCollections.observableArrayList(coins);
        tempPortfolioTable.setItems(data);
        updateTempPortfolioValue(data); // ✅ Update temp portfolio value
    }

    @FXML
    private void handleLockTemp() {
        String name = txtTempName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Enter temp portfolio name to lock.");
            return;
        }

        DBHelper.lockTempPortfolio(LoginController.currentUserId, name);
        showAlert("Locked temp portfolio: " + name);
        txtTempName.clear();
        tempPortfolioTable.setItems(null);
        if (lblTempValue != null) {
            lblTempValue.setText("Temp Value: $0.00"); // Reset after locking
        }
    }

    // --- Utility ---
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleLogout(ActionEvent e) {
        System.out.println("Logout clicked — navigation to be implemented.");
    }
}

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

    @FXML private TableView<Coin> marketTable;
    @FXML private TableColumn<Coin, String> colSymbol;
    @FXML private TableColumn<Coin, String> colName;
    @FXML private TableColumn<Coin, Double> colPrice;

    @FXML private TextField coinSymbolField;
    @FXML private TextField quantityField;
    @FXML private TableView<Coin> portfolioTable;
    @FXML private TableColumn<Coin, String> colPortSymbol;
    @FXML private TableColumn<Coin, Double> colPortQuantity;

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

        loadPortfolio();
    }

    @FXML
    public void handleLogout(ActionEvent e) {
        // Placeholder for future navigation
        System.out.println("Logout clicked — navigation to be implemented.");
    }

    private void loadPortfolio() {
        List<Coin> portfolio = DBHelper.getUserPortfolio(LoginController.currentUserId);
        ObservableList<Coin> data = FXCollections.observableArrayList(portfolio);
        portfolioTable.setItems(data);
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
}

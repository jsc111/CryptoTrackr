package ui;

import db.DBHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import model.Coin;
import javafx.scene.layout.HBox;
import javafx.scene.control.cell.*;
import javafx.util.Callback;
import java.util.List;

public class DashboardController {

    @FXML private TextField coinSymbolField;
    @FXML private TextField quantityField;
    @FXML private TableView<Coin> portfolioTable;
    @FXML private TableColumn<Coin, String> colPortSymbol;
    @FXML private TableColumn<Coin, Double> colPortQuantity;
    @FXML private TableColumn<Coin, Void> colPortActions; // ✅ NEW

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
    @FXML private TableColumn<Coin, Void> tempColActions; // ✅ NEW

    @FXML private Label lblPortfolioValue;
    @FXML private Label lblTempValue;

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
        addActionButtonsToPortfolio(); // ✅ Add action buttons

        // Temp portfolio setup
        if (tempColSymbol != null && tempColQuantity != null) {
            tempColSymbol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
            tempColQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            tempPortfolioTable.setItems(FXCollections.observableArrayList());
            addActionButtonsToTemp(); // ✅ Add action buttons
        }

        loadPortfolio();
    }

    private void loadPortfolio() {
        List<Coin> portfolio = DBHelper.getUserPortfolio(LoginController.currentUserId);
        ObservableList<Coin> data = FXCollections.observableArrayList(portfolio);
        portfolioTable.setItems(data);
        updatePortfolioValue(data);
    }

    private void updatePortfolioValue(ObservableList<Coin> portfolio) {
        double total = 0.0;
        for (Coin c : portfolio) {
            total += c.getValue();
        }
        if (lblPortfolioValue != null) {
            lblPortfolioValue.setText(String.format("Total Value: $%.2f", total));
        }
    }

    private void updateTempPortfolioValue(ObservableList<Coin> tempPortfolio) {
        double total = 0.0;
        for (Coin c : tempPortfolio) {
            total += c.getValue();
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

    // --- TEMP PORTFOLIO METHODS ---

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
        updateTempPortfolioValue(data);
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
            lblTempValue.setText("Temp Value: $0.00");
        }
    }

    // --- 🔧 Action Button Setup ---

    private void addActionButtonsToPortfolio() {
        colPortActions.setCellFactory(getActionCellFactory(portfolioTable, false));
    }

    private void addActionButtonsToTemp() {
        tempColActions.setCellFactory(getActionCellFactory(tempPortfolioTable, true));
    }

    private Callback<TableColumn<Coin, Void>, TableCell<Coin, Void>> getActionCellFactory(TableView<Coin> table, boolean isTemp) {
        return new Callback<>() {
            @Override
            public TableCell<Coin, Void> call(final TableColumn<Coin, Void> param) {
                return new TableCell<>() {
                    private final Button btnUpdate = new Button("Update");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox pane = new HBox(5, btnUpdate, btnDelete);

                    {
                        btnUpdate.setOnAction(event -> {
                            Coin coin = getTableView().getItems().get(getIndex());
                            TextInputDialog dialog = new TextInputDialog(String.valueOf(coin.getQuantity()));
                            dialog.setTitle("Update Quantity");
                            dialog.setHeaderText("Update " + coin.getSymbol());
                            dialog.setContentText("New quantity:");
                            dialog.showAndWait().ifPresent(input -> {
                                try {
                                    double newQty = Double.parseDouble(input);
                                    if (isTemp) {
                                        DBHelper.updatePortfolioEntry(LoginController.currentUserId, coin.getSymbol(), newQty, true, null);
                                        handleViewTemp(); // refresh temp
                                    } else {
                                        DBHelper.updatePortfolioEntry(LoginController.currentUserId, coin.getSymbol(), newQty, false, null);
                                        loadPortfolio(); // refresh main
                                    }
                                } catch (NumberFormatException e) {
                                    showAlert("Invalid number.");
                                }
                            });
                        });

                        btnDelete.setOnAction(event -> {
                            Coin coin = getTableView().getItems().get(getIndex());
                            if (isTemp) {
                                DBHelper.deletePortfolioEntry(LoginController.currentUserId, coin.getSymbol(), true, null);
                                handleViewTemp(); // refresh temp
                            } else {
                                DBHelper.deletePortfolioEntry(LoginController.currentUserId, coin.getSymbol(), false, null);
                                loadPortfolio(); // refresh main
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
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

package db;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Coin;

public class DBHelper {
    private static final String DB_PATH = "data/cryptotrackr.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    public static Connection connect() {
        try {
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdir();
                System.out.println("Created 'data' directory.");
            }

            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();

            // Tables
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS portfolio (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "symbol TEXT NOT NULL, " +
                    "quantity REAL NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS portfolios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "coin_symbol TEXT, " +
                    "quantity REAL, " +
                    "is_temp INTEGER DEFAULT 0)");

            // Add column if it doesn't exist (safe way)
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(portfolios)");
            boolean hasTempName = false;
            while (rs.next()) {
                if ("temp_name".equals(rs.getString("name"))) {
                    hasTempName = true;
                    break;
                }
            }
            if (!hasTempName) {
                stmt.execute("ALTER TABLE portfolios ADD COLUMN temp_name TEXT");
                System.out.println("Added 'temp_name' column.");
            }

            stmt.close();
            System.out.println("Database connected and tables ready.");
            return conn;
        } catch (SQLException e) {
            System.out.println("DB connection failed: " + e.getMessage());
            return null;
        }
    }

    // ✅ Add to main portfolio
    public static void addToPortfolio(int userId, String symbol, double quantity, boolean isTemp) {
        String query = "INSERT INTO portfolios (user_id, coin_symbol, quantity, is_temp) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, symbol.toUpperCase());
            stmt.setDouble(3, quantity);
            stmt.setInt(4, isTemp ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding to portfolio: " + e.getMessage());
        }
    }

    // ✅ Add to temp portfolio (with name)
    public static void addToTempPortfolio(int userId, String symbol, double quantity, String tempName) {
        String query = "INSERT INTO portfolios (user_id, coin_symbol, quantity, is_temp, temp_name) VALUES (?, ?, ?, 1, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, symbol.toUpperCase());
            stmt.setDouble(3, quantity);
            stmt.setString(4, tempName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding to temp portfolio: " + e.getMessage());
        }
    }

    // ✅ Get all coins in a named temp portfolio (with live prices)
    public static List<Coin> getTempPortfolio(int userId, String tempName) {
        List<Coin> coins = new ArrayList<>();
        String query = "SELECT coin_symbol, quantity FROM portfolios WHERE user_id = ? AND is_temp = 1 AND temp_name = ?";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, tempName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String symbol = rs.getString("coin_symbol");
                double quantity = rs.getDouble("quantity");
                coins.add(Coin.withPrice(symbol, quantity));  // ✅ Include live price
            }
        } catch (SQLException e) {
            System.out.println("Error fetching temp portfolio: " + e.getMessage());
        }
        return coins;
    }

    // ✅ Lock temp portfolio by name
    public static void lockTempPortfolio(int userId, String tempName) {
        String query = "UPDATE portfolios SET is_temp = 0, temp_name = NULL WHERE user_id = ? AND is_temp = 1 AND temp_name = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, tempName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error locking portfolio: " + e.getMessage());
        }
    }

    // 🆕 Get distinct temp portfolio names (optional helper for dropdowns)
    public static Set<String> getTempPortfolioNames(int userId) {
        Set<String> names = new HashSet<>();
        String query = "SELECT DISTINCT temp_name FROM portfolios WHERE user_id = ? AND is_temp = 1 AND temp_name IS NOT NULL";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                names.add(rs.getString("temp_name"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching temp names: " + e.getMessage());
        }

        return names;
    }

    // ✅ Get user’s locked (real) portfolio (with live prices)
    public static List<Coin> getUserPortfolio(int userId) {
        List<Coin> coins = new ArrayList<>();
        String query = "SELECT coin_symbol, quantity FROM portfolios WHERE user_id = ? AND is_temp = 0";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String symbol = rs.getString("coin_symbol");
                double quantity = rs.getDouble("quantity");
                coins.add(Coin.withPrice(symbol, quantity));  // ✅ Include live price
            }
        } catch (SQLException e) {
            System.out.println("Error fetching portfolio: " + e.getMessage());
        }

        return coins;
    }
}

package db;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL)");

            // Old portfolio table (keep if needed for backup)
            stmt.execute("CREATE TABLE IF NOT EXISTS portfolio (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "symbol TEXT NOT NULL, " +
                    "quantity REAL NOT NULL)");

            // New improved portfolios table
            stmt.execute("CREATE TABLE IF NOT EXISTS portfolios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "coin_symbol TEXT, " +
                    "quantity REAL, " +
                    "is_temp INTEGER DEFAULT 0)");

            stmt.close();
            System.out.println("Database connected and tables ready.");
            return conn;
        } catch (SQLException e) {
            System.out.println("DB connection failed: " + e.getMessage());
            return null;
        }
    }

    // ✅ Add coin to portfolio
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

    // ✅ Get all non-temp portfolio coins for a user
    public static List<Coin> getUserPortfolio(int userId) {
        List<Coin> coins = new ArrayList<>();
        String query = "SELECT coin_symbol, quantity FROM portfolios WHERE user_id = ? AND is_temp = 0";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                coins.add(new Coin(rs.getString("coin_symbol"), rs.getString("coin_symbol"), rs.getDouble("quantity")));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching portfolio: " + e.getMessage());
        }
        return coins;
    }

    // ✅ Get temp portfolios
    public static List<Coin> getTempPortfolio(int userId) {
        List<Coin> coins = new ArrayList<>();
        String query = "SELECT coin_symbol, quantity FROM portfolios WHERE user_id = ? AND is_temp = 1";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                coins.add(new Coin(rs.getString("coin_symbol"), rs.getString("coin_symbol"), rs.getDouble("quantity")));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching temp portfolio: " + e.getMessage());
        }
        return coins;
    }

    // ✅ Lock temp portfolio (set is_temp = 0)
    public static void lockTempPortfolio(int userId) {
        String query = "UPDATE portfolios SET is_temp = 0 WHERE user_id = ? AND is_temp = 1";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error locking portfolio: " + e.getMessage());
        }
    }
}

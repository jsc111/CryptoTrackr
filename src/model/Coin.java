package model;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import com.google.gson.*;

public class Coin {
    private String symbol;
    private String name;
    private double price;     // Live market price from API
    private double quantity;  // User-defined quantity for portfolios

    // Constructor for API coins (without quantity)
    public Coin(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.quantity = 0.0;
    }

    // Constructor for portfolio coins (with quantity)
    public Coin(String symbol, String name, double price, double quantity) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Constructor for DB fetch (symbol + quantity only)
    public Coin(String symbol, double quantity) {
        this.symbol = symbol;
        this.name = "";
        this.price = 0.0;
        this.quantity = quantity;
    }

    // NEW: Constructor with price for DB-based coins
    public Coin(String symbol, double quantity, double price) {
        this.symbol = symbol;
        this.name = "";
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getQuantity() { return quantity; }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Returns value of coin in USD = price * quantity
    public double getValue() {
        return this.price * this.quantity;
    }

    // Fetch top 10 coins with name, symbol, price
    public static List<Coin> fetchTopCoins() {
        List<Coin> coins = new ArrayList<>();
        try {
            String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=10&page=1";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Accept", "application/json");

            JsonArray arr = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonArray();

            for (JsonElement elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                String symbol = obj.get("symbol").getAsString().toUpperCase();
                String name = obj.get("name").getAsString();
                double price = obj.get("current_price").getAsDouble();

                coins.add(new Coin(symbol, name, price));
            }

        } catch (Exception e) {
            System.out.println("Error fetching coin data: " + e.getMessage());
        }

        return coins;
    }

    // NEW: Fetch current price of a given symbol (case-insensitive)
    public static double fetchPrice(String symbol) {
        try {
            String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=100&page=1";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Accept", "application/json");

            JsonArray arr = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonArray();

            for (JsonElement elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                String fetchedSymbol = obj.get("symbol").getAsString().toUpperCase();
                if (fetchedSymbol.equalsIgnoreCase(symbol)) {
                    return obj.get("current_price").getAsDouble();
                }
            }

        } catch (Exception e) {
            System.out.println("Error fetching price for " + symbol + ": " + e.getMessage());
        }

        return 0.0; // Fallback price
    }

    // NEW: Create Coin object with live price fetched
    public static Coin withPrice(String symbol, double quantity) {
        double price = fetchPrice(symbol);
        return new Coin(symbol, quantity, price);
    }
}

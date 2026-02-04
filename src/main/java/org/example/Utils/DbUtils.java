package org.example.Utils;

import org.example.Classes.Stock;
import org.example.Classes.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class
DbUtils {
    private Connection connection;
    @PostConstruct
    public void init() {
        try {
            // 1. שליפת המשתנים שהגדרנו ב-Render
            String url = System.getenv("SPRING_DATASOURCE_URL");
            String user = System.getenv("SPRING_DATASOURCE_USERNAME");
            String pass = System.getenv("SPRING_DATASOURCE_PASSWORD");

            // 2. בדיקה שהמשתנים אכן הגיעו (יופיע בלוגים של Render)
            if (url == null || user == null || pass == null) {
                System.err.println("❌ ERROR: Missing Environment Variables for Database!");
                return;
            }

            // 3. יצירת החיבור היחיד והנכון
            this.connection = DriverManager.getConnection(url, user, pass);

            if (this.connection != null) {
                System.out.println("✅ Connection established successfully to: " + url);
            }
        } catch (SQLException e) {
            System.err.println("❌ CRITICAL: Database connection failed!");
            e.printStackTrace();
        }
    }
    public boolean insertTicker(Stock stock, int userId) {
        try {
            // שימוש ב-UPPER כדי למנוע כפילויות של אותיות גדולות/קטנות
            PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT stock_id FROM stocks WHERE UPPER(ticker) = UPPER(?)");
            ps.setString(1, stock.getTicker());
            ResultSet rs = ps.executeQuery();

            int id;
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                // הכנסת מניה חדשה - הכל בכתב קטן (lowercase)
                ps = this.connection.prepareStatement("INSERT INTO stocks (" +
                        "ticker, price, rsi, trend, pattern, sma50, sma150, timestamp, reasoning," +
                        "resistance, expectation, vol, action)" +
                        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, stock.getTicker().toUpperCase()); // שמירה תמיד באותיות גדולות
                ps.setDouble(2, stock.getPrice());
                ps.setDouble(3, stock.getRSI());
                ps.setString(4, stock.getTrend());
                ps.setString(5, stock.getPattern());
                ps.setDouble(6, stock.getSMA50());
                ps.setDouble(7, stock.getSMA150());
                ps.setString(9, stock.getReasoning());
                ps.setDouble(10, stock.getResistance());
                ps.setString(11, stock.getExpectation());
                ps.setString(12, stock.getVol());
                ps.setString(13, stock.getAction());

                if (stock.getTimeStamp() != null) {
                    ps.setTimestamp(8, new java.sql.Timestamp(stock.getTimeStamp().getTime()));
                } else {
                    ps.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
                }

                ps.executeUpdate();
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get generated stock ID");
                }
            }
            // שלב סופי: חיבור המניה למשתמש
            return insertTickerUser(id, userId);

        } catch (SQLException e) {
            System.err.println("❌ SQL Error in insertTicker: " + e.getMessage());
            return false;
        }
    }

    public boolean insertTickerUser(int stockId, int userId) {
        try {
            // בדיקה אם הקשר כבר קיים
            PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT * FROM user_stock WHERE user_id = ? AND stock_id = ?");
            ps.setInt(1, userId);
            ps.setInt(2, stockId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                ps = this.connection.prepareStatement("INSERT INTO user_stock (user_id, stock_id) VALUES(?, ?)");
                ps.setInt(1, userId);
                ps.setInt(2, stockId);
                return ps.executeUpdate() == 1;
            }
            return true; // כבר קיים
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in insertTickerUser: " + e.getMessage());
            return false;
        }
    }
    public List<Stock> loadAllStocksPerUser(int UserId) {
        List<Stock> stocks = new ArrayList<>();
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT * from stocks s JOIN user_stock us ON us.Stock_id=s.Stock_id WHERE us.User_id=?");
            ps.setInt(1,UserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                stocks.add(new Stock(rs.getString("Ticker"),rs.getFloat("Price"),
                        rs.getDouble("RSI"),rs.getString("Trend"),rs.getString("Pattern"),
                        rs.getDouble("SMA50"),rs.getDouble("SMA150"),
                        rs.getString("Reasoning"),rs.getFloat("Resistance"),
                        rs.getString("Expectation"),rs.getDate("TimeStamp"),rs.getString("Pattern Info"),rs.getString("Action")));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return stocks;
    }
    public boolean removeStockUser(int userid, String Ticker) {
        try{
            PreparedStatement ps = this.connection.prepareStatement("Select Stock_id FROM stocks WHERE Ticker=?");
            ps.setString(1,Ticker);
            ResultSet rs = ps.executeQuery();
            int stockid;
            if (rs.next()) {
                stockid = rs.getInt("Stock_id");

                ps = this.connection.prepareStatement("DELETE FROM user_stock WHERE User_id=? AND Stock_id=?");
                ps.setInt(1, userid);
                ps.setInt(2, stockid);
                int rows = ps.executeUpdate();
            }
            return true;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT user_id, password, username, email, phone from users");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("user_id"),rs.getString("username"),rs.getString("password"),
                        rs.getString("email"),rs.getString("phone")));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return users;
    }
    public void insertUser(String username, String password, String email,String phone) {
        try {
            PreparedStatement ps = this.connection.prepareStatement(
                    "INSERT INTO users (username, password,email,phone) " +
                    "VALUES (?, ?, ?,?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

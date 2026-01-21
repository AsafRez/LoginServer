package org.example;

import com.github.javafaker.DateAndTime;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class DbUtils {
    private Connection connection;
    @PostConstruct
    public void init() {
        try {
            String schema = "stocks_watcher";
            String url = "jdbc:mysql://localhost:3306/" + schema;

            this.connection = DriverManager.getConnection(url, "root", "1234");

            if (this.connection != null) {
                System.out.println("✅ Connection established successfully");
            }
        } catch (SQLException e) {
            System.err.println("❌ CRITICAL: Database connection failed!");
            e.printStackTrace();
        }
    }
    public void insertTickerUser(int StockId,int UserId) {
        try{
            PreparedStatement ps = this.connection.prepareStatement("INSERT INTO user_stock (User_id,Stock_id) VALUES(?,?)");
            ps.setInt(1,UserId);
            ps.setInt(2,StockId);
            ps.executeUpdate();

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
    public List<Stock> loadAllStocksPerUser(int UserId) {
        List<Stock> stocks = new ArrayList<>();
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT Ticker from stocks s JOIN user_stock us ON us.Stock_id=s.Stock_id WHERE us.User_id=?");
            ps.setInt(1,UserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                stocks.add(new Stock(rs.getString("Ticker")));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return stocks;
    }
    public void insertTicker(String ticker,int UserId) {
        try{
            PreparedStatement ps = this.connection.prepareStatement("SELECT Stock_id FROM Stocks WHERE ticker = ?");
            ps.setString(1, ticker);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                int id = rs.getInt(1);
                insertTickerUser(id,UserId);
            }else {
                ps = this.connection.prepareStatement("INSERT INTO Stocks (Ticker) VALUES (?)");
                ps.setString(1, ticker);
                ps.executeUpdate();
                insertTicker(ticker,UserId);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);

        }
    }
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT User_id,password,UserName,Email,Phone from Users");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("User_id"),rs.getString("UserName"),rs.getString("password"),rs.getString("Email"),rs.getString("Phone")));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return users;
    }
    public void insertUser(String username, String password, String email,String phone) {
        try {
            PreparedStatement ps = this.connection.prepareStatement(
                    "INSERT INTO users (UserName, Password,Email,Phone) " +
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

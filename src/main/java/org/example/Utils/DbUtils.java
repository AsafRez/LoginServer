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
    public boolean insertTickerUser(int StockId,int UserId) {
        try{
            PreparedStatement ps=this.connection.prepareStatement("SELECT * FROM user_stock where user_id=? AND stock_id=?");
            ps.setInt(1,UserId);
            ps.setInt(2,StockId);
            ResultSet rs=ps.executeQuery();
            if(!rs.next()){
            ps = this.connection.prepareStatement("INSERT INTO user_stock (User_id,Stock_id) VALUES(?,?)");
            ps.setInt(1,UserId);
            ps.setInt(2,StockId);
            int rowsEf = ps.executeUpdate();
            return rowsEf==1;
            }

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return true;
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
    public boolean insertTicker(Stock stock,int UserId) {
        try{
            PreparedStatement ps = this.connection.prepareStatement("SELECT Stock_id FROM stocks WHERE ticker = ?");
            ps.setString(1, stock.getTicker());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                int id = rs.getInt(1);
                return insertTickerUser(id,UserId);
            }else {
                ps = this.connection.prepareStatement("INSERT INTO stocks (" +
                        "Ticker,Price,RSI,Trend,Pattern,SMA50,SMA150,TimeStamp,Reasoning," +
                        "Resistance,Expectation,Vol,Action)" +
                        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                ps.setString(1, stock.getTicker());
                ps.setDouble(2, stock.getPrice());
                ps.setDouble(3, stock.getRSI());
                ps.setString(4, stock.getTrend());
                ps.setString(5, stock.getPattern());
                ps.setDouble(6,stock.getSMA50());
                ps.setDouble(7,stock.getSMA150());
                ps.setString(9,stock.getReasoning());
                ps.setDouble(10,stock.getResistance());
                ps.setString(11,stock.getExpectation());
                ps.setString(12,stock.getVol());
                ps.setString(13,stock.getAction());
                if (stock.getTimeStamp() != null) {
                    ps.setTimestamp(8, new java.sql.Timestamp(stock.getTimeStamp().getTime()));
                } else {
                    ps.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
                }
                ps.executeUpdate();
                return insertTicker(stock,UserId);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);

        }
    }
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT User_id,password,UserName,Email,Phone from users");
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

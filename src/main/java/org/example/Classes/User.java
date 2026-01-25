package org.example.Classes;

public class User {
    private int id;
    private String password;
    private String username;
    private String email;
    private String tel;

    public String getPassword() {
        return password;
    }

    public int getId() {
        return id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public User(int id,String username,String password, String email, String tel) {
        this.id=id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.tel = tel;

    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

}

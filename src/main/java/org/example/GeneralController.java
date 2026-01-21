package org.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.example.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.example.DbUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class GeneralController {
    @Autowired
    private DbUtils dbUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @PostConstruct
    public void init() {
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // ביטול הגנת CSRF שחוסמת בקשות מ-React
                .cors().and()      // הפעלת תמיכה ב-CORS בתוך מערכת האבטחה
                .authorizeRequests()
                .antMatchers("/Register").permitAll() // אפשר לכולם להירשם
                .anyRequest().permitAll(); // זמנית, אפשר הכל כדי לוודא שזה עובד

        return http.build();
    }
    @RequestMapping("/Register")
    public boolean Register(String username, String password,String email, String phone) {
        if (phone.isBlank() || username.isBlank() || password.isBlank() ||email.isBlank()|| password.length() < 4 || username.length() < 4) {
            return false;
        } else {
            String hashedPassword = passwordEncoder.encode(password);
            if (!checkUsername(username)) {
                return false;
            } else {
                dbUtils.insertUser(username, hashedPassword, email, phone);
                return true;
            }
        }
    }

    private boolean checkUsername(String username) {
        List<User> currentUsers=dbUtils.getAllUsers();
        for (User user : currentUsers) {
            if(user.getUserName().equals(username)) {
                return false;
            }
        }
        return true;
    }
    @RequestMapping("/save-to-DB")
    public void saveToDB(
            @RequestParam("ticks") List<String> ticks,
            @RequestParam("userid") int userid) {

        for(String tick : ticks) {
            dbUtils.insertTicker(tick, userid);
        }
    }

    @RequestMapping("/Load-From-DB")
    public List<Stock> loadFromDB(int userid) {
        List<Stock> tickers=dbUtils.loadAllStocksPerUser(userid);
        System.out.println("List content: " + tickers);
        return tickers;
    }
    @RequestMapping("/Login")
    public int Login(String username, String password) {
        if (username.isBlank()||password.isBlank()) {
            return 0;
        }else{
            String hashedPassword = passwordEncoder.encode(password);
            List<User> usersLIST=dbUtils.getAllUsers();
            for(User user:usersLIST) {
                boolean isMatch = passwordEncoder.matches(password,user.getPassword());
                if(user.getUserName().equals(username)&&isMatch) {
                    return user.getId();
                }
            }

            return 0;}
    }


    private int sendSMS(String tel, int OTPSend) {
        // 1. הגדרת ה-URL המלא (כבר כולל את הפרמטרים בתוכו)
        String serverURL = "https://backend-qcf9.onrender.com/send-sms?token=Almog464@&phoneNumber="
                + tel + "&message=Hello your one time password is:" + OTPSend
                + " please do not share it with others PLZZZZZZ";
        // 2. יצירת אובייקט RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(serverURL, null, String.class);

            // בדיקה אם הסטטוס קוד הוא 200 (OK)
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("SMS נשלח בהצלחה: " + response.getBody());
                return 1; // החזרת הצלחה
            } else {
                return 0; // נכשל
            }
        } catch (Exception e) {
            System.err.println("שגיאה בשליחת SMS: " + e.getMessage());
            return -1; // שגיאת רשת
        }
    }
    private int generateOTP(){
        Random random = new Random();
        int otp = random.nextInt(10000,99999);
        return otp;
    }
//    @RequestMapping("/check-username")
//    public UserAvailableResponse checkusername(String username) {
//        if (username.isBlank()) {
//            return new UserAvailableResponse(false, ERROR_NO_USERNAME, false);
//        }
//        if(dbUtils.doesUsernameExist(username)){
//        return new UserAvailableResponse(false, ERROR_USERNAME_EXISTS, false);
//            }
//            if (username.length() < 4) {
//                return new UserAvailableResponse(false, ERROR_SHORT_USERNAME, false);
//            }
//
//        return new UserAvailableResponse(true,0,true);
//
//    }
//    @GetMapping("/Size")
//    public void getSize(){
//        System.out.println(usersLIST.size());
//    }
//    @GetMapping("/get-all-users")
//    public UsersResponse getAllUsers() {
//        List<User> usersFromDB = dbUtils.getAllUsers();
//
//        return new UsersResponse(true, null, usersFromDB);
//    }
////    public UsersResponse getAllUsers(){
////        return new UsersResponse(true,null,usersLIST);
////    }
//    @RequestMapping("/create-user")
//    public BasicResponse createUser(String username, String password) {
//        if(username.isBlank() ){
//            return new BasicResponse(false,ERROR_NO_USERNAME);
//        }
//        if(password.isBlank() ){
//            return new BasicResponse(false,ERROR_NO_PASSWORD);
//        }
//        if(password.length()<4){
//            return new BasicResponse(false,ERROR_SHORT_PASSWORD);
//        }
//            if(dbUtils.doesUsernameExist(username)){
//                    return new BasicResponse(false,ERROR_USERNAME_EXISTS);
//                }
//        dbUtils.addUser(username,password);
//        return new BasicResponse(true,0);
//    }
}

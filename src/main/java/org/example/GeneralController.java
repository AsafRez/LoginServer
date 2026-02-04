package org.example;


import org.example.Classes.*;
import org.example.Utils.*;
import org.example.responses.BasicResponse;
import org.example.responses.StocksResponse;
import org.example.responses.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import static org.example.Utils.Errors.*;

@RestController
@CrossOrigin(origins = "https://stock-scanner-user.onrender.com", allowCredentials = "true")
public class GeneralController {
    @Autowired
    private DbUtils dbUtils;
    @Autowired
    private JwtUtils jwtUtils;
    @PostConstruct
    public void init() {
    }
    @RequestMapping("/")
    public String index() {
        return "Hello World";
    }

    @RequestMapping("/Register")
    public BasicResponse Register(String username, String password, String email, String phone) {
        if (phone.isBlank() || username.isBlank() || password.isBlank() ||email.isBlank()|| password.length() < 4 || username.length() < 4) {
            return new BasicResponse(false,ERROR_MISSING_INFO);
        } else {
            if (!checkUsername(username)) {
                return new BasicResponse(false,ERROR_USERNAME_TAKEN);
            } else {
                String hashedPassword=generateMD5(username,password);
                dbUtils.insertUser(username, hashedPassword, email, phone);
                return new BasicResponse(true,null);
            }
        }
    }
    private String generateMD5(String username, String password) {
        try {
            // שילוב שם המשתמש והסיסמה כפי שעשה המרצה
            String source = username + password;

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(source.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b)); // אותיות קטנות
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
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
    @PostMapping("/save-to-DB")
    public BasicResponse saveToDB(@RequestBody Map<String, Object> payload,
                                  @RequestHeader("Authorization") String header) {
        List<Stock> ticks = (List<Stock>) payload.get("ticks");
        String time= (String) payload.get("time");
        System.out.println(time);
        if (ticks == null || ticks.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_INFO);
        }

        try {
            String token = header.substring(7);
            int userId = jwtUtils.extractUserId(token);
            System.out.println(userId);
            // 2. אין צורך ב-ObjectMapper! פשוט רצים על הרשימה הקיימת
            for (Stock tick : ticks) {
                if (!dbUtils.insertTicker(tick, userId)) {
                    return new BasicResponse(false, ERROR_DB_NOT_UPDATED);
                }
            }
            return new BasicResponse(true, null);

        } catch (Exception e) {
            e.printStackTrace();
            return new BasicResponse(false, ERROR_DATABASE); // החזרת false בשגיאה
        }
    }

    @RequestMapping("/Load-From-DB")
    public BasicResponse loadFromDB(@RequestHeader("Authorization") String header) {
        String token = header.substring(7);
        int userId = jwtUtils.extractUserId(token);
        List<Stock> tickers = dbUtils.loadAllStocksPerUser(userId);
        if (tickers != null) {
            return new StocksResponse(true,null,tickers) ;
        }
        return new BasicResponse(false,ERROR_DATABASE);
    }
    @RequestMapping("/check-session")
    public BasicResponse checkSession(
            @CookieValue(name = "token", required = false) String token) {

        if (token != null && !token.isBlank()) {
            return new UserResponse(true, null, null, token);
        }
        return new BasicResponse(false, ERROR_WRONG_INFO);
    }
    @RequestMapping("/Remove-Stock")
    public BasicResponse removeStock(@RequestHeader("Authorization") String header, String Ticker) {
        String token = header.substring(7);
        int userId = jwtUtils.extractUserId(token);
        if(dbUtils.removeStockUser(userId, Ticker)){
            return new BasicResponse(true,null);
        }
        return new BasicResponse(false,ERROR_DATABASE);
    }
    @RequestMapping("/Login")
    public BasicResponse Login(@RequestParam("username") String username, @RequestParam("password") String password) {
        if (username.isBlank()||password.isBlank()) {
            return new BasicResponse(false,ERROR_MISSING_INFO);
        }else{
            String hashedPassword =generateMD5(username,password) ;
            List<User> usersLIST=dbUtils.getAllUsers();
            for(User user:usersLIST) {

                if(user.getUserName().equals(username)&&user.getPassword().equals(hashedPassword)) {
                    String token = jwtUtils.generateToken(username, user.getId());

                    return new UserResponse(true,null,user,token);
                }
            }

            return new BasicResponse(false,ERROR_WRONG_INFO);}
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

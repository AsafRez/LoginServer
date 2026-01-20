package org.example;


import org.example.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@CrossOrigin(origins = "http://localhost:5174")
public class GeneralController {
    private List<User> usersLIST;

    @PostConstruct
    public void init() {
        usersLIST.add(new User("admin", "admin","0546377362"));
    }

    @Autowired
//    private DbUtils dbUtils;


    @RequestMapping("/Register")
    public boolean Register(String username, String password, String tel) {
        if (tel.isBlank() || username.isBlank() || password.isBlank() || password.length() < 4 || username.length() < 4) {
            return false;
        } else {
            if (checkUsername(username)) {
                this.usersLIST.add(new User(username, password, tel));
                System.out.println(usersLIST.size());
            } else {
                return false;
            }
            return true;
        }
    }

    private boolean checkUsername(String username) {
        for (User user : usersLIST) {
            if(user.getUserName().equals(username)) {
                return false;
            }
        }
        return true;
    }
    @RequestMapping("/Login")
    public boolean Login(String username, String password) {
        if (username.isBlank()||password.isBlank()) {
            return false;
        }else{
            for(User user:usersLIST) {
                if(user.getUserName().equals(username)&&user.getPassword().equals(password)) {
                    user.setOTP(generateOTP());
                    sendSMS(user.getTel(),user.getOTP());
                }else{
                  return false;
                }
            }

            return true;}
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

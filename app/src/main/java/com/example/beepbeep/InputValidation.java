package com.example.beepbeep;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidation {
    /**
     * Check if a given string of email address are valid
     * @param email String
     * @return true if email is the right format
     */
    static public boolean validEmail(String email){
        String pattern = "^[a-zA-Z0-9\\-!#$%&'*+/=?^_`{|}~.]+@\\w+\\.\\w+$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(email);
        return m.find();
    }

    /**
     * Check if a given string of phone number are valid
     * @param phone String
     * @return true if phone is the right format
     */
    static public boolean validPhone(String phone){
        String pattern = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(phone);
        return m.find();
    }

    /**
     * Check if a given string of username are valid
     * @param username String
     * @return true if username is the right format
     */
    static public boolean validUsername(String username){
        String pattern = "^[A-Za-z0-9_-]{5,15}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(username);
        return m.find();
    }
}

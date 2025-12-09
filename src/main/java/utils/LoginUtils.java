package utils;

import java.util.Random;

public class LoginUtils {
    public static String generateVerificationCode() {
        Random random = new Random();

        String data = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int index=random.nextInt(62);
            char c=data.charAt(index);
            sb.append(c);
        }
        return sb.toString();

    }
}

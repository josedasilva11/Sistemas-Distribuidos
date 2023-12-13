package src.server;

import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserManager {
    private Map<String, String> userCredentials = new HashMap<>();

    public UserManager() {
    }

    public boolean authenticate(String username, String password) {
        String storedPasswordHash = userCredentials.get(username);
        String providedPasswordHash = hashPassword(password);
        return storedPasswordHash != null && storedPasswordHash.equals(providedPasswordHash);
    }

    public boolean registerUser(String username, String password) {
        if (userCredentials.containsKey(username)) {
            return false;
        }
        userCredentials.put(username, hashPassword(password));
        return true;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao hash a password", e);
        }
    }
}

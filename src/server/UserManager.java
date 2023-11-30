package src.server;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private Map<String, String> userCredentials = new HashMap<>();

    public UserManager() {
    }

    public boolean authenticate(String username, String password) {
        String validPassword = userCredentials.get(username);
        return validPassword != null && validPassword.equals(password);
    }

    public boolean registerUser(String username, String password) {
        if (!userCredentials.containsKey(username)) {
            userCredentials.put(username, password);
            return true; // Registro bem-sucedido
        }
        return false; // Nome de utilizador já existe
    }

}

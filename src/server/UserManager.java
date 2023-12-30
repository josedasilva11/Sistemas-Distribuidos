package src.server;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserManager {
    private Map<String, String> userCredentials = new HashMap<>();
    private static final String USER_DATA_FILE = "userCredentials.txt";

    public UserManager() {
        loadUserData();
    }

    public String hashPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    private void loadUserData() {
        File file = new File(USER_DATA_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile(); // Cria um novo arquivo se ele não existir
            } catch (IOException e) {
                System.out.println("Não foi possível criar o arquivo de credenciais do usuário: " + e.getMessage());
                return; // Sair do método se não puder criar o arquivo
            }
        }

        // Continuação da leitura do arquivo, agora sabendo que ele existe
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    userCredentials.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler dados dos usuários: " + e.getMessage());
        }
    }

    private void saveUserData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_DATA_FILE))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar dados dos usuários: " + e.getMessage());
        }
    }

    public boolean authenticate(String username, String password) {
        String storedPasswordHash = userCredentials.get(username);
        String providedPasswordHash = hashPassword(password);

        System.out.println("Nome de utilizador recebido: " + username);
        System.out.println("Password recebida: " + password);
        System.out.println("Hash armazenado: " + storedPasswordHash);
        System.out.println("Hash fornecido: " + providedPasswordHash);

        return storedPasswordHash != null && storedPasswordHash.equals(providedPasswordHash);
    }

    public boolean registerUser(String username, String password) {
        if (userCredentials.containsKey(username)) {
            System.out.println("Registration failed: Nome de utilizador já existe.");
            return false; // Nome de utilizador já existe
        }
        String passwordHash = hashPassword(password);
        userCredentials.put(username, passwordHash);
        saveUserData(); // Salva os dados atualizados no arquivo
        System.out.println("Registration successful: Nome de utilizador - " + username);
        return true;
    }
}

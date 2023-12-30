package src.server;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserManager {
    private Map<String, String> userCredentials = new HashMap<>(); // mapa para armazenar as credenciais
    private static final String USER_DATA_FILE = "userCredentials.txt"; // ficheiro para armazenar as credenciais

    public UserManager() {
        loadUserData();
    }

    // Gerar uma hash para a palavra-passe
    private String hashPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    // Regista as credênciais no ficheiro
    private void loadUserData() {
        File file = new File(USER_DATA_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile(); // Cria um novo arquivo se ele não existir
            } catch (IOException e) {
                System.out.println("Não foi possível criar o arquivo de credenciais do utilizador: " + e.getMessage());
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
            System.out.println("Erro ao ler dados dos utilizadores: " + e.getMessage());
        }
    }

    // Guardar as credenciais no arquivo
    private void saveUserData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_DATA_FILE))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Erro ao guardar dados dos utilizadores: " + e.getMessage());
        }
    }

    // Autenticar um utilizador
    public boolean authenticate(String username, String password) {
        String storedPasswordHash = userCredentials.get(username);
        String providedPasswordHash = hashPassword(password);

        System.out.println("Hash armazenado: " + storedPasswordHash);
        System.out.println("Hash fornecido: " + providedPasswordHash);

        return storedPasswordHash != null && storedPasswordHash.equals(providedPasswordHash);
    }

    // Registas um utilizador
    public boolean registerUser(String username, String password) {
        if (userCredentials.containsKey(username)) {
            return false; // Nome de utillizador já existe
        }
        String passwordHash = hashPassword(password);
        userCredentials.put(username, passwordHash);
        saveUserData(); // Guarda os dados atualizados no arquivo
        return true;
    }

}

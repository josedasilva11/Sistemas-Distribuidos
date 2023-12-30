package src.server;

import java.io.*;
import java.net.*;

public class AuthenticationServer {
    private static UserManager userManager = new UserManager();

    public static void main(String[] args) {
        int port = 1234; // Ou outra porta de sua escolha
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor de autenticação iniciado na porta " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    System.out.println(
                            "Nova conexão de autenticação aceita: " + clientAddress);

                    // Adicione um log indicando a hora em que a conexão foi aceita
                    System.out.println("Hora da conexão: " + java.time.LocalTime.now());

                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                    String action = in.readUTF();
                    System.out.println("Ação do cliente: " + action);

                    if ("register".equals(action)) {
                        handleRegister(in, out);
                    } else if ("login".equals(action)) {
                        handleLogin(in, out);
                    }
                } catch (IOException e) {
                    System.out.println("Erro no servidor de autenticação: " + e.getMessage());
                    // Continue o loop para aceitar outras conexões
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor de autenticação: " + e.getMessage());
        }
    }

    private static void handleRegister(DataInputStream in, DataOutputStream out) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();

        boolean success = userManager.registerUser(username, password);
        String response = success ? "Registo bem-sucedido." : "Nome de utilizador já existe.";

        String passwordHash = userManager.hashPassword(password); // Calculate the password hash

        System.out.println("Ação do cliente: register");
        System.out.println("Nome de utilizador recebido: " + username);
        System.out.println("Password recebida: " + password);
        System.out.println("Hash armazenado: " + passwordHash); // Use the calculated hash
        System.out.println("Hash fornecido: " + password);
        System.out.println("Resposta ao cliente: " + response);

        out.writeUTF(response);
    }

    private static void handleLogin(DataInputStream in, DataOutputStream out) throws IOException {
        String username = in.readUTF();
        System.out.println("Ação do cliente: login");
        System.out.println("Nome de utilizador recebido: " + username);

        String password = in.readUTF();
        System.out.println("Password recebida: " + password);

        boolean isAuthenticated = userManager.authenticate(username, password);
        String response = isAuthenticated ? "Bem-vindo " + username : "Falha na autenticação.";
        System.out.println("Resposta ao cliente: " + response);
        out.writeUTF(response);
    }
}

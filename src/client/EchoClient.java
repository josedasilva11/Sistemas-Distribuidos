package src.client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class EchoClient {
    private static volatile boolean isLoggedIn = false;
    private static CompletableFuture<Void> loginFuture = new CompletableFuture<>();

    public static void main(String[] args) {
        String hostName = "localhost";
        int port = 1234;

        try {
            Socket socket = new Socket(hostName, port);
            System.out.println("Conectado ao servidor em " + hostName + ":" + port);

            ResponseCallback callback = response -> {
                System.out.println("Resposta do servidor: " + response);
                if (response.startsWith("Bem-vindo")) {
                    isLoggedIn = true;
                    loginFuture.complete(null); // Completa o futuro quando o login é bem-sucedido
                }
            };
            CommunicationThread commThread = new CommunicationThread(socket, callback);
            Thread commThreadRunner = new Thread(commThread);
            commThreadRunner.start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (!isLoggedIn) {
                    System.out.println("Digite 'register', 'login' ou 'exit' para a ação correspondente:");
                    String action = scanner.nextLine();
                    if ("exit".equals(action)) {
                        break;
                    }
                    handleUserInput(action, scanner, commThread);

                    if ("login".equals(action)) {
                        loginFuture.join(); // Aguarda a conclusão do futuro
                        loginFuture = new CompletableFuture<>(); // Reset para próximos logins
                    }
                } else {
                    System.out.println("Digite 'enviar', 'status' ou 'exit' para a ação correspondente:");
                    String action = scanner.nextLine();
                    if ("exit".equals(action)) {
                        break;
                    }
                    handleUserInput(action, scanner, commThread);
                }
            }

            scanner.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Erro ao conectar com o servidor: " + e.getMessage());
        }
    }

    private static void handleUserInput(String action, Scanner scanner, CommunicationThread commThread) {
        switch (action) {
            case "register":
            case "login":
                System.out.println(action.equals("register") ? "Registrando usuário..." : "Autenticando usuário...");
                System.out.println("Digite o seu nome de utilizador:");
                String username = scanner.nextLine();
                System.out.println("Digite a sua password:");
                String password = scanner.nextLine();

                commThread.addRequest(new Request(action, username)); // Envia o nome de usuário
                commThread.addRequest(new Request("password", password)); // Envia a senha
                break;
            case "status":
                System.out.println("Solicitando status do servidor...");
                commThread.addRequest(new Request("status", ""));
                break;
            case "enviar":
                System.out.println("Enviando arquivo de tarefa...");
                handleFileSending(scanner, commThread);
                break;
            default:
                System.out.println("Ação desconhecida.");
                break;
        }
    }

    private static void handleAuthentication(Scanner scanner, CommunicationThread commThread, String action) {
        System.out.println("Digite o seu nome de utilizador:");
        String username = scanner.nextLine();
        System.out.println("Digite a sua password:");
        String password = scanner.nextLine();

        String combinedCredentials = username + ";" + password; // Combina o nome de usuário e senha
        commThread.addRequest(new Request(action, combinedCredentials));
    }

    private static void handleFileSending(Scanner scanner, CommunicationThread commThread) {
        try {
            System.out.println("Escreva o caminho do arquivo de tarefa:");
            String filePath = scanner.nextLine();
            File file = new File(filePath);
            byte[] fileContent = Files.readAllBytes(file.toPath());

            System.out.println("Escreva a quantidade de memória necessária para a tarefa (em MB):");
            String memoryRequired = scanner.nextLine();

            Request fileRequest = new Request("enviar", new String(fileContent) + ";" + memoryRequired);
            commThread.addRequest(fileRequest);
        } catch (IOException e) {
            System.out.println("Erro ao manipular o arquivo: " + e.getMessage());
        }
    }
}

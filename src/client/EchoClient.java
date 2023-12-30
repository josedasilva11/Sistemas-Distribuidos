package src.client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.Base64;

public class EchoClient {
    private static volatile boolean isLoggedIn = false;
    private static CompletableFuture<Void> loginFuture = new CompletableFuture<>();
    private static CompletableFuture<Void> responseFuture = new CompletableFuture<>();
    private static final String AUTH_SERVER_HOST = "localhost";
    private static final int AUTH_SERVER_PORT = 1234;
    private static final int QUEUE_MANAGER_PORT = 12345;
    private static CompletableFuture<Void> actionFuture = CompletableFuture.completedFuture(null);
    private static CommunicationThread commThread;
    private static Thread commThreadRunner;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try (Socket authSocket = new Socket(AUTH_SERVER_HOST, AUTH_SERVER_PORT)) {
            System.out.println("Conectado ao servidor de autenticação em " + AUTH_SERVER_HOST + ":" + AUTH_SERVER_PORT);

            ResponseCallback callback = response -> {
                System.out.println("Resposta do servidor: " + response);
                if (response.startsWith("Bem-vindo")) {
                    isLoggedIn = true;
                    loginFuture.complete(null);
                    // Imprime a mensagem de boas-vindas e continua com o menu de usuário logado
                    System.out.println(response);
                    while (isLoggedIn) {
                        System.out.println(
                                "Digite 'enviar', 'status', 'statusQueue' ou 'exit' para a ação correspondente:");
                        String action = scanner.nextLine();
                        handleUserInput(action, scanner);
                        if ("exit".equals(action)) {
                            commThreadRunner.interrupt();
                            break;
                        }
                        responseFuture = new CompletableFuture<>();
                        if (!"login".equals(action)) {
                            actionFuture.join();
                        }
                        responseFuture.join();
                        if ("login".equals(action)) {
                            loginFuture.join();
                            loginFuture = new CompletableFuture<>();
                        }
                    }
                }
            };

            commThread = new CommunicationThread(authSocket, callback);
            commThreadRunner = new Thread(commThread);
            commThreadRunner.start();

            while (true) {
                if (!isLoggedIn) {
                    System.out.println("Digite 'register', 'login' ou 'exit' para a ação correspondente:");
                } else {
                    System.out
                            .println("Digite 'enviar', 'status', 'statusQueue' ou 'exit' para a ação correspondente:");
                }

                String action = scanner.nextLine();
                if ("exit".equals(action)) {
                    commThreadRunner.interrupt(); // Interrompe a thread de comunicação
                    break;
                }
                handleUserInput(action, scanner);
                if ("login".equals(action)) {
                    actionFuture = loginFuture; // Use a CompletableFuture correta
                }

                responseFuture = new CompletableFuture<>();

                if (!"login".equals(action)) {
                    actionFuture.join(); // Aguarde a ação ser concluída
                }
                responseFuture.join(); // Aguarda a resposta do servidor

                if ("login".equals(action)) {
                    loginFuture.join(); // Aguarda a conclusão do futuro
                    loginFuture = new CompletableFuture<>(); // Reset para próximos logins
                }
            }

            scanner.close();
        } catch (IOException e) {
            System.out.println("Erro ao conectar com o servidor: " + e.getMessage());
        }
    }

    private static void handleUserInput(String action, Scanner scanner) {
        switch (action) {
            case "register":
            case "login":
                sendCredentials(scanner, action);
                break;
            case "enviar":
                if (isLoggedIn) {
                    handleFileSending(scanner);
                } else {
                    System.out.println("Você precisa estar logado para enviar tarefas.");
                }
                break;
            case "status":
                if (isLoggedIn) {
                    requestStatusFromAuthServer();
                } else {
                    System.out.println("Você precisa estar logado para verificar o status.");
                }
                break;
            case "statusQueue":
                if (isLoggedIn) {
                    requestStatusFromQueueManager();
                } else {
                    System.out.println("Você precisa estar logado para verificar o status da fila.");
                }
                break;
            case "exit":
                System.out.println("Encerrando o programa.");
                break;
            default:
                System.out.println("Ação desconhecida.");
                break;
        }
    }

    private static void sendCredentials(Scanner scanner, String actionType) {
        System.out.println("Digite o seu nome de utilizador:");
        String username = scanner.nextLine();
        System.out.println("Digite a sua password:");
        String password = scanner.nextLine();

        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
        System.out.println("Enviando ação: " + actionType);
        System.out.println("Enviando nome de utilizador: " + username);
        System.out.println("Enviando password: " + encodedPassword);

        commThread.addRequest(new Request(actionType, username));
        commThread.addRequest(new Request("password", encodedPassword));
    }

    private static void requestStatusFromAuthServer() {
        System.out.println("Solicitando status do servidor de autenticação...");
        commThread.addRequest(new Request("status", "auth"));
    }

    private static void requestStatusFromQueueManager() {
        System.out.println("Solicitando status do QueueManager...");
        try (Socket socket = new Socket(AUTH_SERVER_HOST, QUEUE_MANAGER_PORT)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.writeUTF("statusQueue");

            String response = in.readUTF();
            System.out.println("Status do QueueManager: " + response);
        } catch (IOException e) {
            System.out.println("Erro ao solicitar status do QueueManager: " + e.getMessage());
        }
    }

    // Método para lidar com o envio do arquivo
    private static void handleFileSending(Scanner scanner) {
        try {
            System.out.println("Escreva o caminho do arquivo de tarefa:");
            String filePath = scanner.nextLine();
            File file = new File(filePath);
            byte[] fileContent = Files.readAllBytes(file.toPath());

            System.out.println("Escreva a quantidade de memória necessária para a tarefa (em MB):");
            long memoryRequired = Long.parseLong(scanner.nextLine()) * 1024 * 1024; // Convertendo de MB para bytes

            DataOutputStream out = commThread.getDataOutputStream();

            // Primeiro, envia o tamanho do conteúdo do arquivo
            System.out.println("Enviando tamanho do arquivo: " + fileContent.length + " bytes");
            out.writeInt(fileContent.length);

            // Em seguida, envia o conteúdo do arquivo
            System.out.println("Enviando conteúdo do arquivo...");
            out.write(fileContent);

            // Por último, envia a quantidade de memória requerida
            System.out.println("Enviando memória requerida para a tarefa: " + memoryRequired + " bytes");
            out.writeLong(memoryRequired);

            System.out.println("Arquivo de tarefa e memória requerida enviados para o servidor...");
        } catch (IOException e) {
            System.out.println("Erro ao enviar arquivo: " + e.getMessage());
        }
    }
}

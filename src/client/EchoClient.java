package src.client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.Base64;

public class EchoClient {
    private static volatile boolean isLoggedIn = false; // guarda o estado de login
    private static CompletableFuture<Void> loginFuture = new CompletableFuture<>(); // Future para a ação login
    private static CompletableFuture<Void> responseFuture = new CompletableFuture<>(); // Future para a resposta do server

    public static void main(String[] args) {
        String hostName = "localhost";
        int port = 1234;

        try {
            Socket socket = new Socket(hostName, port);
            System.out.println("Conectado ao servidor em " + hostName + ":" + port);

            // Define o callback quando há uma resposta
            ResponseCallback callback = response -> {
                System.out.println("Resposta do servidor: " + response);
                responseFuture.complete(null); // Sinaliza que a resposta foi recebida
                if (response.startsWith("Bem-vindo")) {
                    isLoggedIn = true;
                    loginFuture.complete(null); // Completa o futuro quando o login é bem-sucedido
                }
            };

            // Cria e inicia a thread 
            CommunicationThread commThread = new CommunicationThread(socket, callback);
            Thread commThreadRunner = new Thread(commThread);
            commThreadRunner.start();

            // Apresenta as opções consoante o estado de autenticação  
            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (!isLoggedIn) {
                    System.out.println("Digite 'register', 'login' ou 'exit' para a ação correspondente:");
                } else {
                    System.out.println("Digite 'enviar', 'status' ou 'exit' para a ação correspondente:");
                }

                String action = scanner.nextLine();
                if ("exit".equals(action)) {
                    commThreadRunner.interrupt(); // Interrompe a thread de comunicação
                    break;
                }
                handleUserInput(action, scanner, commThread, socket); // Passa o socket como argumento

                responseFuture = new CompletableFuture<>();
                responseFuture.join(); // Aguarda a resposta do servidor

                if ("login".equals(action)) {
                    loginFuture.join(); // Aguarda a conclusão do futuro
                    loginFuture = new CompletableFuture<>(); // Reset para os logins seguintes
                }
            }

            scanner.close();
            if (!socket.isClosed()) {
                socket.close(); // Fecha o socket
            }
        } catch (IOException e) {
            System.out.println("Erro ao conectar com o servidor: " + e.getMessage());
        }
    }

    // Define o comportamento conforme a ação escolhida pelo cliente
    private static void handleUserInput(String action, Scanner scanner, CommunicationThread commThread, Socket socket) {
        switch (action) {
            case "register":
                System.out.println("Registando utillizador...");
                sendCredentials(scanner, commThread, "register");
                break;
            case "login":
                System.out.println("Autenticando utillizador...");
                sendCredentials(scanner, commThread, "login");
                break;
            case "status":
                System.out.println("Solicitando status do servidor...");
                commThread.addRequest(new Request("status", ""));
                break;
            case "enviar":
                System.out.println("Enviando arquivo de tarefa...");
                handleFileSending(scanner, commThread);
                break;
            case "exit":
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close(); // Fechar o socket
                    }
                    System.out.println("Conexão encerrada. Saindo do programa.");
                    System.exit(0); // Encerrar o programa
                } catch (IOException e) {
                    System.out.println("Erro ao fechar o socket: " + e.getMessage());
                }
                break;
            default:
                System.out.println("Ação desconhecida.");
                break;
        }
    }

    // Envia as credenciais para registo/autenticação
    private static void sendCredentials(Scanner scanner, CommunicationThread commThread, String actionType) {
        System.out.println("Digite o seu nome de utilizador:");
        String username = scanner.nextLine();
        System.out.println("Digite a sua password:");
        String password = scanner.nextLine();

        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());

        commThread.addRequest(new Request(actionType, username));
        commThread.addRequest(new Request("password", encodedPassword));
    }

    // Responsável pelo envio de um arquivo
    private static void handleFileSending(Scanner scanner, CommunicationThread commThread) {
        try {
            System.out.println("Escreva o caminho do arquivo de tarefa:");
            String filePath = scanner.nextLine();
            File file = new File(filePath);
            byte[] fileContent = Files.readAllBytes(file.toPath());

            System.out.println("Escreva a quantidade de memória necessária para a tarefa (em MB):");
            long memoryRequired = Long.parseLong(scanner.nextLine()) * 1024 * 1024; // Converte de MB para bytes

            DataOutputStream out = commThread.getDataOutputStream();

            // Envia o tamanho
            System.out.println("Enviando tamanho do arquivo: " + fileContent.length + " bytes");
            out.writeInt(fileContent.length);

            // Envia o conteúdo
            System.out.println("Enviando conteúdo do arquivo...");
            out.write(fileContent);

            // Envia a quantiade de memória necessária
            System.out.println("Enviando memória requerida para a tarefa: " + memoryRequired + " bytes");
            out.writeLong(memoryRequired);

            System.out.println("Arquivo de tarefa e memória requerida enviados para o servidor...");
        } catch (IOException e) {
            System.out.println("Erro ao enviar arquivo: " + e.getMessage());
        }
    }
}
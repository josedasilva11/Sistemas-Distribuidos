package src.client;

import java.nio.file.Files;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EchoClient {
    public static void main(String[] args) {
        String hostName = "localhost";
        int port = 1234;
        Scanner scanner = new Scanner(System.in);

        try (Socket socket = new Socket(hostName, port)) {
            ResponseCallback callback = response -> System.out.println("Resposta do servidor: " + response);
            CommunicationThread commThread = new CommunicationThread(socket, callback);
            new Thread(commThread).start();

            while (true) {
                System.out.println(
                        "Digite 'register', 'login', 'enviar', 'status' ou 'exit' para a ação correspondente:");
                String action = scanner.nextLine();

                if ("exit".equals(action)) {
                    break; // Encerra o loop e fecha o cliente
                }

                handleUserInput(action, scanner, commThread);
            }
        } catch (IOException e) {
            System.out.println("Erro ao conectar com o servidor: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void handleUserInput(String action, Scanner scanner, CommunicationThread commThread) {
        switch (action) {
            case "register":
            case "login":
                handleAuthentication(scanner, commThread, action);
                break;
            case "enviar":
                handleFileSending(scanner, commThread);
                break;
            case "status":
                Request statusRequest = new Request("status", "");
                commThread.addRequest(statusRequest);
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

        Request authRequest = new Request(action, username + ";" + password);
        commThread.addRequest(authRequest);
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

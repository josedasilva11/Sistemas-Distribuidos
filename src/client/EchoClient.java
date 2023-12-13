package src.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EchoClient {
    public static void main(String[] args) {
        // Definição do endereço do servidor e porta
        String hostName = "localhost";
        int port = 1234;
        // Inicia o scanner para leitura de entrada do utilizador
        Scanner scanner = new Scanner(System.in);

        // Tentativa de estabelecer conexão com o servidor
        try (Socket socket = new Socket(hostName, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // Solicita ao utilizador para escolher entre registo ou login
            System.out.println("Digite 'register' para se registar ou 'login' para entrar:");
            String action = scanner.nextLine();

            // Lógica para registro
            if ("register".equals(action)) {
                System.out.println("Digite o nome de utilizador para registo:");
                String username = scanner.nextLine();
                System.out.println("Digite a password:");
                String password = scanner.nextLine();
                // Enviando comandos e dados para o servidor
                out.writeUTF("register");
                out.writeUTF(username);
                out.writeUTF(password);
            }
            // Lógica para login
            else {
                System.out.println("Digite o seu nome de utilizador:");
                String username = scanner.nextLine();
                System.out.println("Digite a sua password:");
                String password = scanner.nextLine();
                // Envia comandos e dados para o servidor
                out.writeUTF("login");
                out.writeUTF(username);
                out.writeUTF(password);
            }

            // Recebe e exibe a resposta do servidor
            String response = in.readUTF();
            System.out.println(response);

            // Autenticação bem-sucedida
            if (response.startsWith("Bem-vindo")) {
                // Oferece ao utilizador a escolha da ação
                System.out.println(
                        "Digite 'enviar' para enviar uma tarefa ou 'status' para consultar o estado do serviço:");
                String nextAction = scanner.nextLine();

                // Lógica para enviar uma tarefa
                if ("enviar".equals(nextAction)) {
                    System.out.println("Escreva a quantidade de memória necessária para a tarefa (em MB):");
                    long memoryRequired = Long.parseLong(scanner.nextLine());
                    // Envia dados da tarefa para o servidor
                    out.writeLong(memoryRequired);
                    out.writeUTF("dados da tarefa");
                    // Recebe e exibe a resposta do servidor sobre a tarefa
                    String taskResponse = in.readUTF();
                    System.out.println("Resposta do servidor: " + taskResponse);
                }
                // Lógica para consultar o estado do serviço
                else if ("status".equals(nextAction)) {
                    // Envia o comando de consulta para o servidor
                    out.writeUTF("status");
                    // Recebe e exibe informações de memória e tarefas pendentes
                    String memoryStatus = in.readUTF();
                    String taskStatus = in.readUTF();
                    System.out.println(memoryStatus);
                    System.out.println(taskStatus);
                }
            }

        }
        // Tratamento de exceções relacionadas à rede
        catch (IOException e) {
            System.out.println("Erro ao conectar com o servidor: " + e.getMessage());
        }
        // Bloco finally para garantir que o scanner é fechado
        finally {
            // Fechar o scanner para evitar libertação de recursos
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}

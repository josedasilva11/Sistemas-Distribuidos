package src.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EchoClient {
    public static void main(String[] args) {
        String hostName = "localhost";
        int port = 1234;
        Scanner scanner = new Scanner(System.in);

        try (Socket socket = new Socket(hostName, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())) {

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
                System.out.println(
                        "Digite 'enviar' para enviar uma tarefa ou 'status' para consultar o estado do serviço:");
                String nextAction = scanner.nextLine();

                if ("enviar".equals(nextAction)) {
                    System.out.println("Escreva o caminho do arquivo de tarefa:");
                    String filePath = scanner.nextLine();
                    File file = new File(filePath);
                    byte[] fileContent = new byte[(int) file.length()];
                    try (FileInputStream fis = new FileInputStream(file)) {
                        fis.read(fileContent);
                    } catch (FileNotFoundException e) {
                        System.out.println("Arquivo não encontrado: " + e.getMessage());
                        return;
                    } catch (IOException e) {
                        System.out.println("Erro ao ler o arquivo: " + e.getMessage());
                        return;
                    }

                    System.out.println("Escreva a quantidade de memória necessária para a tarefa (em MB):");
                    long memoryRequired = Long.parseLong(scanner.nextLine());

                    out.writeLong(memoryRequired);
                    out.writeInt(fileContent.length); // Envia o tamanho do arquivo
                    out.write(fileContent); // Envia o conteúdo do arquivo

                    String taskResponse = in.readUTF();
                    System.out.println("Resposta do servidor: " + taskResponse);
                } else if ("status".equals(nextAction)) {
                    // [Código para consulta de status]
                }
            }

        } catch (IOException e) {
            System.out.println("Erro ao conectar com o servidor: " + e.getMessage());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}
package src.server;

import java.io.*;
import java.net.*;

public class EchoServer {
    private static final long TOTAL_MEMORY = Runtime.getRuntime().maxMemory();
    private static TaskQueueManager taskQueueManager = new TaskQueueManager(TOTAL_MEMORY);
    private static UserManager userManager = new UserManager();

    public static void main(String[] args) {
        int port = 1234;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão aceita: " + clientSocket.getInetAddress().getHostAddress());

                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                try {
                    String action = in.readUTF();
                    System.out.println("Ação recebida: " + action);
                    handleAction(action, in, out, clientSocket);
                } catch (IOException e) {
                    System.out.println("Erro de comunicação: " + e.getMessage());
                    clientSocket.close(); // Fechar o socket do cliente em caso de erro
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private static void handleAction(String action, DataInputStream in, DataOutputStream out, Socket clientSocket)
            throws IOException {
        switch (action) {
            case "register":
                System.out.println("Registrando usuário...");
                handleRegister(in, out);
                break;
            case "login":
                // Lê o nome de usuário como antes
                String username = in.readUTF();
                // Agora espera a próxima ação para ser "password"
                String passwordAction = in.readUTF();
                if ("password".equals(passwordAction)) {
                    String password = in.readUTF(); // Lê a senha
                    handleLogin(username, password, out);
                }
                break;
            case "status":
                System.out.println("Enviando status do servidor...");
                handleStatus(out);
                break;
            case "enviarTarefa":
                System.out.println("Processando tarefa...");
                handleTaskSubmission(in, clientSocket);
                break;
            default:
                out.writeUTF("Ação desconhecida.");
                break;
        }
    }

    private static void handleRegister(DataInputStream in, DataOutputStream out) throws IOException {
        try {
            String combinedCredentials = in.readUTF();
            String[] credentials = combinedCredentials.split(";");
            if (credentials.length != 2) {
                out.writeUTF("Dados inválidos.");
                return;
            }
            String username = credentials[0].trim();
            String password = credentials[1].trim();

            boolean success = userManager.registerUser(username, password);
            String response = success ? "Registo bem-sucedido." : "Nome de utilizador já existe.";
            out.writeUTF(response);
        } catch (Exception e) {
            out.writeUTF("Erro no registro: " + e.getMessage());
        }
    }

    private static void handleLogin(String username, String password, DataOutputStream out) throws IOException {
        System.out.println("Entrando no método handleLogin");
        System.out.println("Nome de usuário lido: " + username);
        System.out.println("Senha recebida (antes do hash): '" + password + "'"); // Log para depuração

        boolean isAuthenticated = userManager.authenticate(username, password);
        String response = isAuthenticated ? "Bem-vindo " + username : "Falha na autenticação.";
        out.writeUTF(response);
        System.out.println("Resposta enviada: " + response);
    }

    private static void handleStatus(DataOutputStream out) throws IOException {
        out.writeUTF("Memória disponível: " + getAvailableMemory() + " bytes");
        out.writeUTF("Tarefas pendentes: " + taskQueueManager.getTaskCount());
    }

    private static void handleTaskSubmission(DataInputStream in, Socket clientSocket) throws IOException {
        byte[] taskData = receiveTaskData(in);
        long memoryRequired = in.readLong();
        Task task = new Task(clientSocket, taskData, memoryRequired);
        taskQueueManager.addTask(task);
        new Thread(() -> processTask(task)).start();
    }

    private static long getAvailableMemory() {
        return taskQueueManager.getAvailableMemory();
    }

    private static void processTask(Task task) {
        try {
            byte[] result = sd23.JobFunction.execute(task.getData());
            sendTaskResult(task.getClientSocket(), "Sucesso", result);
        } catch (sd23.JobFunctionException e) {
            sendTaskResult(task.getClientSocket(), "Erro na execução da tarefa: " + e.getMessage(), null);
        } finally {
            taskQueueManager.releaseMemory(task.getMemoryRequired());
            closeClientSocket(task.getClientSocket());
        }
    }

    private static void sendTaskResult(Socket clientSocket, String message, byte[] result) {
        try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            out.writeUTF(message);
            if (result != null) {
                out.write(result); // Envia resultado ao cliente
            }
        } catch (IOException e) {
            System.out.println("Erro ao enviar resultado da tarefa: " + e.getMessage());
        }
    }

    private static void closeClientSocket(Socket clientSocket) {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Erro ao fechar o socket do cliente: " + e.getMessage());
        }
    }

    private static byte[] receiveTaskData(DataInputStream in) throws IOException {
        int dataSize = in.readInt();
        byte[] data = new byte[dataSize];
        in.readFully(data);
        return data;
    }
}

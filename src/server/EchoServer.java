package src.server;

import java.io.*;
import java.net.*;
//import java.util.concurrent.ConcurrentLinkedQueue;

public class EchoServer {
    // private static final long WAIT_TIME_BEFORE_RETRYING = 5000;
    private static final long TOTAL_MEMORY = Runtime.getRuntime().maxMemory();
    private static TaskQueueManager taskQueueManager = new TaskQueueManager(TOTAL_MEMORY);
    private static UserManager userManager = new UserManager();

    public static void main(String[] args) {
        int port = 1234;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado na porta " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                    String action = in.readUTF();
                    handleAction(action, in, out, clientSocket);
                } catch (IOException e) {
                    System.out.println("Erro de comunicação: " + e.getMessage());
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
                handleRegister(in, out);
                break;
            case "login":
                handleLogin(in, out);
                break;
            case "status":
                handleStatus(out);
                break;
            case "enviarTarefa":
                handleTaskSubmission(in, clientSocket);
                break;
            default:
                out.writeUTF("Ação desconhecida.");
                break;
        }
    }

    private static void handleRegister(DataInputStream in, DataOutputStream out) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        boolean success = userManager.registerUser(username, password);
        out.writeUTF(success ? "Registo bem-sucedido." : "Nome de utilizador já existe.");
    }

    private static void handleLogin(DataInputStream in, DataOutputStream out) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        boolean isAuthenticated = userManager.authenticate(username, password);
        out.writeUTF(isAuthenticated ? "Bem-vindo " + username : "Falha na autenticação.");
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
package src.server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EchoServer {
    private static final long TOTAL_MEMORY = Runtime.getRuntime().maxMemory();
    private static TaskQueueManager taskQueueManager = new TaskQueueManager(TOTAL_MEMORY);
    private static UserManager userManager = new UserManager();

    public static void main(String[] args) {
        int port = 1234;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado na porta " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Nova conexão aceita: " + clientSocket.getInetAddress().getHostAddress());

                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                    while (!clientSocket.isClosed()) {
                        try {
                            String action = in.readUTF();
                            System.out.println("Ação recebida: " + action);
                            handleAction(action, in, out, clientSocket);
                        } catch (EOFException e) {
                            System.out
                                    .println("Cliente desconectado: " + clientSocket.getInetAddress().getHostAddress());
                            break; // Encerrar o loop se o cliente desconectar
                        } catch (IOException e) {
                            System.out.println("Erro de comunicação: " + e.getMessage());
                            break; // Encerrar o loop em caso de outro erro de comunicação
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Erro com o socket do cliente: " + e.getMessage());
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
                handleTaskSubmission(in, clientSocket, out);
                break;
            case "exit":
                handleClientExit(clientSocket);
                break;
            default:
                out.writeUTF("Ação desconhecida.");
                break;
        }
    }

    private static void handleRegister(DataInputStream in, DataOutputStream out) throws IOException {
        String username = in.readUTF(); // Lê o nome de usuário
        String password = in.readUTF(); // Lê a senha

        boolean success = userManager.registerUser(username, password);
        String response = success ? "Registo bem-sucedido." : "Nome de utilizador já existe.";
        out.writeUTF(response);
    }

    private static void handleLogin(DataInputStream in, DataOutputStream out) throws IOException {
        String username = in.readUTF(); // Lê o nome de usuário
        String password = in.readUTF(); // Lê a senha

        boolean isAuthenticated = userManager.authenticate(username, password);
        String response = isAuthenticated ? "Bem-vindo " + username : "Falha na autenticação.";
        out.writeUTF(response);
    }

    private static void handleStatus(DataOutputStream out) throws IOException {
        long availableMemory = getAvailableMemory();
        int pendingTasks = taskQueueManager.getTaskCount();

        System.out.println("Enviando status: Memória disponível - " + availableMemory +
                " bytes, Tarefas pendentes - " + pendingTasks);

        out.writeUTF("Memória disponível: " + availableMemory + " bytes");
        out.writeUTF("Tarefas pendentes: " + pendingTasks);
    }

    private static void handleTaskSubmission(DataInputStream in, Socket clientSocket, DataOutputStream out)
            throws IOException {
        try {
            System.out.println("Preparando para ler dados da tarefa...");

            // Lê o tamanho dos dados da tarefa
            System.out.println("Lendo tamanho dos dados da tarefa...");
            int dataSize = in.readInt();
            System.out.println("Tamanho dos dados da tarefa recebido: " + dataSize + " bytes");

            // Lê os dados da tarefa
            System.out.println("Lendo dados da tarefa...");
            byte[] taskData = new byte[dataSize];
            in.readFully(taskData);
            System.out.println("Dados da tarefa recebidos com sucesso. Tamanho: " + dataSize + " bytes");

            // Lê a memória requerida para a tarefa
            System.out.println("Lendo memória requerida para a tarefa...");
            long memoryRequired = in.readLong();
            System.out.println("Memória requerida para a tarefa: " + memoryRequired + " bytes");

            // Processa a tarefa
            processReceivedTaskData(clientSocket, taskData, memoryRequired, out);
        } catch (IOException e) {
            System.out.println("Erro ao ler dados da tarefa: " + e.getMessage());
            throw e;
        }
    }

    private static void processReceivedTaskData(Socket clientSocket, byte[] taskData, long memoryRequired,
            DataOutputStream out) throws IOException {
        System.out.println("Processando dados da tarefa recebidos...");
        Task task = new Task(clientSocket, taskData, memoryRequired);

        if (taskQueueManager.canAddTask(task)) {
            taskQueueManager.addTask(task);
            new Thread(() -> processTask(task)).start();
            System.out.println("Tarefa adicionada à fila e processamento iniciado.");
            out.writeUTF("Tarefa adicionada à fila");
        } else {
            System.out.println("Falha ao adicionar tarefa: memória insuficiente.");
            out.writeUTF("Não há memória suficiente para executar a tarefa");
        }
    }

    private static long getAvailableMemory() {
        return taskQueueManager.getAvailableMemory();
    }

    private static void processTask(Task task) {
        try {
            System.out.println("Processando tarefa...");
            byte[] result = sd23.JobFunction.execute(task.getData());
            sendTaskResult(task.getClientSocket(), "Sucesso", result);
            System.out.println("Tarefa processada com sucesso. Enviando resultados...");
        } catch (sd23.JobFunctionException e) {
            sendTaskResult(task.getClientSocket(), "Erro na execução da tarefa: " + e.getMessage(), null);
            System.out.println("Erro na execução da tarefa: " + e.getMessage());
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

    private static void handleClientExit(Socket clientSocket) {
        try {
            System.out.println("Fechando conexão com o cliente: " + clientSocket.getInetAddress().getHostAddress());
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Erro ao fechar o socket do cliente: " + e.getMessage());
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
        try {
            int dataSize = in.readInt();
            byte[] data = new byte[dataSize];
            in.readFully(data);
            System.out.println("Dados da tarefa recebidos com sucesso. Tamanho: " + dataSize + " bytes");
            return data;
        } catch (IOException e) {
            System.out.println("Erro ao receber dados da tarefa: " + e.getMessage());
            throw e;
        }
    }

}

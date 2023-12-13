package src.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EchoServer {
    private static final long WAIT_TIME_BEFORE_RETRYING = 5000;
    private static ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<>();
    private static UserManager userManager = new UserManager(); // Utilizando UserManager

    public static void main(String[] args) {
        int port = 1234; // Porta para o servidor escutar

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado na porta " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                    String action = in.readUTF();
                    if ("register".equals(action)) {
                        String username = in.readUTF();
                        String password = in.readUTF();
                        boolean success = userManager.registerUser(username, password);
                        out.writeUTF(success ? "Registo bem-sucedido." : "Nome de utilizador já existe.");
                    } else if ("login".equals(action)) {
                        String username = in.readUTF();
                        String password = in.readUTF();
                        boolean isAuthenticated = userManager.authenticate(username, password);
                        out.writeUTF(isAuthenticated ? "Bem-vindo " + username : "Falha na autenticação.");
                    } else if ("status".equals(action)) {
                        // Consulta de status do servidor
                        out.writeUTF("Memória disponível: " + getAvailableMemory() + " bytes");
                        out.writeUTF("Tarefas pendentes: " + taskQueue.size());
                    } else if ("enviarTarefa".equals(action)) {
                        // Processamento de tarefas enviadas
                        byte[] taskData = receiveTaskData(in);
                        long memoryRequired = in.readLong();
                        addTaskToQueue(clientSocket, taskData, memoryRequired);
                        processTasks();
                    }
                } catch (IOException e) {
                    System.out.println("Erro de comunicação: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private static void addTaskToQueue(Socket clientSocket, byte[] data, long memoryRequired) {
        taskQueue.add(new Task(clientSocket, data, memoryRequired));
    }

    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static long getAvailableMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() - getUsedMemory();
    }

    private static void processTasks() {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.peek(); // Vê a tarefa sem removê-la

            if (getAvailableMemory() >= task.memoryRequired) {
                taskQueue.poll(); // Remove a tarefa da fila

                try (DataOutputStream out = new DataOutputStream(task.clientSocket.getOutputStream())) {
                    try {
                        byte[] output = sd23.JobFunction.execute(task.data); // Executa a tarefa
                        out.writeUTF("Sucesso");
                        out.write(output); // Envia resultado ao cliente
                    } catch (sd23.JobFunctionException e) {
                        out.writeUTF("Erro na execução da tarefa: " + e.getMessage());
                    }
                } catch (Exception e) {
                    try {
                        (new DataOutputStream(task.clientSocket.getOutputStream()))
                                .writeUTF("Erro ao processar a tarefa: " + e.getMessage());
                        System.out.println("Erro ao processar a tarefa: " + e.getMessage());
                    } catch (IOException ioException) {
                        System.out.println("Erro ao enviar mensagem de erro: " + ioException.getMessage());
                    }
                } finally {
                    try {
                        task.clientSocket.close();
                    } catch (IOException e) {
                        System.out.println("Erro ao fechar o socket: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Memória insuficiente para processar a tarefa. Aguardando...");
                try {
                    Thread.sleep(WAIT_TIME_BEFORE_RETRYING);
                } catch (InterruptedException e) {
                    System.out.println("Interrupção durante a espera: " + e.getMessage());
                    break; // Sair do loop se a thread for interrompida
                }
            }
        }
    }

    private static byte[] receiveTaskData(DataInputStream in) throws IOException {
        int dataSize = in.readInt();
        byte[] data = new byte[dataSize];
        in.readFully(data);
        return data;
    }

    private static class Task {
        Socket clientSocket;
        byte[] data;
        long memoryRequired;

        Task(Socket clientSocket, byte[] data, long memoryRequired) {
            this.clientSocket = clientSocket;
            this.data = data;
            this.memoryRequired = memoryRequired;
        }
    }
}
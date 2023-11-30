package src.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashMap;
import java.util.Map;

public class EchoServer {
    private static final long WAIT_TIME_BEFORE_RETRYING = 5000; // Tempo de espera antes de tentar novamente processar
                                                                // tarefas
    private static ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<>(); // Fila para armazenar tarefas
    private static Map<String, String> userCredentials = new HashMap<>(); // Mapa para armazenar dados de utilizador

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
                        // Lógica de registo de utilizador
                        String username = in.readUTF();
                        String password = in.readUTF();
                        userCredentials.put(username, password);
                        out.writeUTF("Registo bem-sucedido.");
                    } else if ("login".equals(action)) {
                        // Lógica de login de utilizador
                        String username = in.readUTF();
                        String password = in.readUTF();
                        if (password.equals(userCredentials.getOrDefault(username, ""))) {
                            out.writeUTF("Bem-vindo " + username);
                        } else {
                            out.writeUTF("Falha na autenticação.");
                        }
                    } else if ("status".equals(action)) {
                        // Consulta de status do servidor
                        out.writeUTF("Memória disponível: " + getAvailableMemory() + " bytes");
                        out.writeUTF("Tarefas pendentes: " + taskQueue.size());
                    } else if (action.equals("enviarTarefa")) {
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

    // Método para adicionar tarefas na fila
    private static void addTaskToQueue(Socket clientSocket, byte[] data, long memoryRequired) {
        taskQueue.add(new Task(clientSocket, data, memoryRequired));
    }

    // Método para obter a memória usada
    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // Método para obter a memória disponível
    private static long getAvailableMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() - getUsedMemory();
    }

    // Método para processar tarefas na fila
    private static void processTasks() {
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.peek(); // Vê a tarefa sem removê-la

            if (getAvailableMemory() >= task.memoryRequired) {
                taskQueue.poll(); // Remove a tarefa da fila

                try (DataOutputStream out = new DataOutputStream(task.clientSocket.getOutputStream())) {
                    byte[] output = sd23.JobFunction.execute(task.data); // Substituir pela logica de processamento

                    out.writeUTF("Sucesso");
                    out.write(output); // Envia resultado ao cliente
                } catch (Exception e) {
                    try {
                        (new DataOutputStream(task.clientSocket.getOutputStream())).writeUTF("Erro: " + e.getMessage());
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

    // Método para receber dados da tarefa
    private static byte[] receiveTaskData(DataInputStream in) throws IOException {
        int dataSize = in.readInt(); // Supondo que o tamanho dos dados é enviado primeiro
        byte[] data = new byte[dataSize];
        in.readFully(data); // Lê os dados da tarefa
        return data;
    }

    // Classe interna Task para representar as tarefas na fila
    private static class Task {
        Socket clientSocket; // Socket do cliente
        byte[] data; // Dados da tarefa
        long memoryRequired; // Memória requerida para a tarefa

        Task(Socket clientSocket, byte[] data, long memoryRequired) {
            this.clientSocket = clientSocket;
            this.data = data;
            this.memoryRequired = memoryRequired;
        }
    }
}

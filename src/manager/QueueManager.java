package src.manager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueManager {
    private ServerSocket serverSocket;
    private ServerSocket clientSocket;
    private ConcurrentLinkedQueue<String> taskQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Socket> serverList = new ConcurrentLinkedQueue<>();

    public QueueManager(int port, int clientPort) throws IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = new ServerSocket(clientPort);
    }

    public void start() {
        new Thread(this::acceptExecutionServers).start();
        new Thread(this::acceptClientTasks).start();

        while (true) {
            if (!taskQueue.isEmpty() && !serverList.isEmpty()) {
                String task = taskQueue.poll();
                distributeTask(task);
            }
        }
    }

    private void acceptExecutionServers() {
        while (!serverSocket.isClosed()) {
            try {
                Socket server = serverSocket.accept();
                serverList.add(server);
                System.out.println("Servidor de execução conectado: " + server.getInetAddress());

                // Adicione um log indicando a hora em que o servidor de execução foi conectado
                System.out.println("Hora da conexão do servidor de execução: " + java.time.LocalTime.now());
            } catch (IOException e) {
                System.out.println("Erro ao aceitar servidor de execução: " + e.getMessage());
            }
        }
    }

    private void acceptClientTasks() {
        while (!clientSocket.isClosed()) {
            try {
                Socket client = clientSocket.accept();
                DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream());

                String task = in.readUTF();

                if ("status".equals(task)) {
                    sendStatus(out);
                } else {
                    taskQueue.add(task);
                    System.out.println("Tarefa recebida do cliente: " + task);

                    // Adicione um log indicando a hora em que a tarefa foi recebida
                    System.out.println("Hora da recepção da tarefa: " + java.time.LocalTime.now());
                }
            } catch (IOException e) {
                System.out.println("Erro ao aceitar tarefa do cliente: " + e.getMessage());
            }
        }
    }

    private void sendStatus(DataOutputStream out) throws IOException {
        String status = "Tarefas na fila: " + taskQueue.size() + ", Servidores conectados: " + serverList.size();
        out.writeUTF(status);
    }

    private void distributeTask(String task) {
        try {
            Socket server = serverList.poll();
            if (server != null) {
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF(task);
                serverList.add(server);
                System.out.println("Tarefa distribuída para o servidor de execução");
            } else {
                System.out.println("Nenhum servidor disponível para distribuir tarefa.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao distribuir tarefa: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            QueueManager manager = new QueueManager(12345, 12346);
            manager.start();
        } catch (IOException e) {
            System.out.println("Não foi possível iniciar o Gerenciador de Fila: " + e.getMessage());
        }
    }
}

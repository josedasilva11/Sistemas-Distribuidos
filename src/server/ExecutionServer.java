package src.server;

import java.io.*;
import java.net.Socket;
import sd23.JobFunction;
import sd23.JobFunctionException;

public class ExecutionServer {
    private Socket managerSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private int tasksInProcess = 0;

    public ExecutionServer(String managerHost, int managerPort) throws IOException {
        managerSocket = new Socket(managerHost, managerPort);
        in = new DataInputStream(managerSocket.getInputStream());
        out = new DataOutputStream(managerSocket.getOutputStream());
    }

    public void start() {
        while (!managerSocket.isClosed()) {
            try {
                String taskData = in.readUTF();

                if ("status".equals(taskData)) {
                    sendStatus();
                } else {
                    byte[] result = processTask(taskData);
                    reportCompletion(result);
                }
            } catch (IOException e) {
                System.out.println("Erro na comunicação com o gerenciador: " + e.getMessage());
                break;
            }
        }
    }

    private void sendStatus() throws IOException {
        // Exemplo simples de status. Adapte conforme necessário.
        String status = "Servidor disponível. Tarefas em andamento: " + tasksInProcess;
        out.writeUTF(status);
    }

    private byte[] processTask(String taskData) {
        try {
            // Aqui você pode converter a string da tarefa em um formato adequado
            byte[] job = taskData.getBytes();
            return JobFunction.execute(job); // Executa a tarefa
        } catch (JobFunctionException e) {
            System.out.println("Falha ao processar a tarefa: " + e.getMessage());
            return null;
        }
    }

    private void reportCompletion(byte[] result) {
        try {
            if (result != null) {
                out.writeUTF("Tarefa concluída com sucesso. Resultado: " + new String(result));
            } else {
                out.writeUTF("Falha na execução da tarefa.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao reportar a conclusão da tarefa: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            ExecutionServer server = new ExecutionServer("localhost", 12345); // Porta do QueueManager
            server.start();
        } catch (IOException e) {
            System.out.println("Não foi possível iniciar o servidor de execução: " + e.getMessage());
        }
    }
}

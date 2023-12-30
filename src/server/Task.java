// src/server/Task.java
package src.server;

import java.net.Socket;

// Define a tarefa
public class Task {
    private final Socket clientSocket; // Sockjet do cliente que enviou a tarefa
    private final byte[] data; // Dados associados à tarefa
    private final long memoryRequired; // Memória necessária

    public Task(Socket clientSocket, byte[] data, long memoryRequired) {
        this.clientSocket = clientSocket;
        this.data = data;
        this.memoryRequired = memoryRequired;
    }

    // Obtém o Socket
    public Socket getClientSocket() {
        return clientSocket;
    }

    // Obtém os dados
    public byte[] getData() {
        return data;
    }

    // Obtém a memória necessária
    public long getMemoryRequired() {
        return memoryRequired;
    }
}

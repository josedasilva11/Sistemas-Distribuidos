// src/server/Task.java
package src.server;

import java.net.Socket;

public class Task {
    private final Socket clientSocket;
    private final byte[] data;
    private final long memoryRequired;
    private final long enqueuedAt;
    private int priority;

    public Task(Socket clientSocket, byte[] data, long memoryRequired) {
        this.clientSocket = clientSocket;
        this.data = data;
        this.memoryRequired = memoryRequired;
        this.enqueuedAt = System.currentTimeMillis();
        this.priority = 0; // Prioridade inicial
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public byte[] getData() {
        return data;
    }

    public long getMemoryRequired() {
        return memoryRequired;
    }

    public long getEnqueuedAt() {
        return enqueuedAt;
    }

    public void increasePriority() {
        this.priority++;
    }

    public int getPriority() {
        return priority;
    }
}

// src/server/Task.java
package src.server;

import java.net.Socket;

public class Task {
    private final Socket clientSocket;
    private final byte[] data;
    private final long memoryRequired;

    public Task(Socket clientSocket, byte[] data, long memoryRequired) {
        this.clientSocket = clientSocket;
        this.data = data;
        this.memoryRequired = memoryRequired;
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
}

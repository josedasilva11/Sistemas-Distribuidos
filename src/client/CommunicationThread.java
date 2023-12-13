package src.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommunicationThread implements Runnable {
    private final DataOutputStream out;
    private final DataInputStream in;
    private final ConcurrentLinkedQueue<Request> requestQueue;
    private final ResponseCallback responseCallback;

    public CommunicationThread(Socket socket, ResponseCallback responseCallback) throws IOException {
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.responseCallback = responseCallback;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                sendPendingRequests();
                listenForResponses();
            }
        } catch (IOException e) {
            // Handle exceptions
        }
    }

    private void sendPendingRequests() throws IOException {
        while (!requestQueue.isEmpty()) {
            Request request = requestQueue.poll();
            // Send request to server
            out.writeUTF(request.getAction());
            // ... Other data as required
        }
    }

    private void listenForResponses() {
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String response = in.readUTF();
                    responseCallback.onResponseReceived(response);
                }
            } catch (IOException e) {
                // Logar ou lidar com a exceção
            }
        }).start();
    }

    public void addRequest(Request request) {
        requestQueue.add(request);
    }

    // Define a Request class or use a suitable data structure for your requests
}

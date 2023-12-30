package src.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommunicationThread implements Runnable {
    private final DataOutputStream out;
    private final DataInputStream in;
    private final Socket socket;
    private final ConcurrentLinkedQueue<Request> requestQueue;
    private final ResponseCallback responseCallback;

    public CommunicationThread(Socket socket, ResponseCallback responseCallback) throws IOException {
        this.socket = socket; // Adicionar esta linha
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
            System.out.println("Erro de IO no CommunicationThread: " + e.getMessage());
        }
    }

    public DataOutputStream getDataOutputStream() {
        return out;
    }

    private void sendPendingRequests() throws IOException {
        while (!requestQueue.isEmpty()) {
            Request request = requestQueue.poll();
            System.out.println("Enviando pedido: " + request.getAction());
            out.writeUTF(request.getAction());
            out.writeUTF(request.getData()); // Adicione esta linha
        }
    }

    private void listenForResponses() {
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !this.socket.isClosed()) {
                    try {
                        String response = in.readUTF();
                        responseCallback.onResponseReceived(response);
                    } catch (EOFException e) {
                        System.out.println("Fim do stream alcançado, fechando a thread de escuta.");
                        break;
                    }
                    Thread.sleep(100);
                }
            } catch (IOException e) {
                if (!this.socket.isClosed()) {
                    System.out.println("Erro ao escutar respostas: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                System.out.println("Thread de escuta interrompida.");
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void addRequest(Request request) {
        requestQueue.add(request);
    }

}

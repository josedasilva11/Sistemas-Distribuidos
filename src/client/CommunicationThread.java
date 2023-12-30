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

    // Construtor
    public CommunicationThread(Socket socket, ResponseCallback responseCallback) throws IOException {
        this.socket = socket; // Socket do cliente
        this.out = new DataOutputStream(socket.getOutputStream()); // Enviar dados
        this.in = new DataInputStream(socket.getInputStream()); // Receber dados
        this.requestQueue = new ConcurrentLinkedQueue<>(); // Queue de requets
        this.responseCallback = responseCallback; // Callback para as respostas
    }

    @Override
    // Envia as requests e escuta as respostas
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

    // Envia as requests presentes na queue
    private void sendPendingRequests() throws IOException {
        while (!requestQueue.isEmpty()) {
            Request request = requestQueue.poll();
            System.out.println("A enviar pedido: " + request.getAction());
            out.writeUTF(request.getAction());
            out.writeUTF(request.getData()); // Adicione esta linha
        }
    }

    // Escuta as respostas do server
    private void listenForResponses() {
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !this.socket.isClosed()) {
                    try {
                        String response = in.readUTF();
                        responseCallback.onResponseReceived(response);
                    } catch (EOFException e) {
                        System.out.println("Fim do stream alcançado, a fechar a thread de escuta.");
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

    // Adiciona uma request à queue
    public void addRequest(Request request) {
        requestQueue.add(request);
    }

}

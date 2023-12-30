package src.client;

// Define o método que lida com as repostas do servidor
@FunctionalInterface
public interface ResponseCallback {
    void onResponseReceived(String response);
}

package src.client;

// Define a estrutura de uma request
public class Request {
    private final String action;
    private final String data; 

    // construtor
    public Request(String action, String data) {
        this.action = action;
        this.data = data;
    }

    // Permite obter a ação
    public String getAction() {
        return action;
    }

    // Permite obter os dados
    public String getData() {
        return data;
    }
}

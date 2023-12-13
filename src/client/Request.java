package src.client;

public class Request {
    private final String action;
    private final String data; // Ou qualquer outro tipo de dado necessário

    public Request(String action, String data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return action;
    }

    public String getData() {
        return data;
    }

    // Adicione outros métodos conforme necessário
}

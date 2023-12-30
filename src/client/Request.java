package src.client;

public class Request {

    private final String action;
    private final String data; // Dados adicionais para a ação, se necessário

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

    // Método estático para criar um pedido de status
    public static Request createStatusRequest(String serverType) {
        return new Request("status", serverType); // serverType pode ser "QueueManager" ou "AuthenticationServer"
    }

    // Método estático para criar um pedido de envio de tarefa
    public static Request createTaskRequest(String taskData) {
        return new Request("task", taskData);
    }

    // Outros métodos estáticos para diferentes tipos de requisições podem ser
    // adicionados aqui
}

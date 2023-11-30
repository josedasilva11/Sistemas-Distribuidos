package src.server;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    // Mapa para armazenar as credenciais dos utilizadores. A chave é o nome de
    // utilizador e o valor é a password.
    private Map<String, String> userCredentials = new HashMap<>();

    // Construtor da classe UserManager. Atualmente, ele não faz nada além de
    // inicializar a classe.
    public UserManager() {
    }

    // Método para autenticar um utilizador.
    // Retorna verdadeiro se o nome de utilizador existir no mapa e a password
    // fornecida corresponder à password armazenada.
    public boolean authenticate(String username, String password) {
        String validPassword = userCredentials.get(username); // Obtém a password armazenada para o nome de utilizador
                                                              // dado
        // Verifica se a password é não-nula e corresponde à password fornecida
        return validPassword != null && validPassword.equals(password);
    }

    // Método para registrar um novo utilizador.
    // Adiciona o utilizador ao mapa se o nome de utilizador ainda não existir.
    // Retorna verdadeiro se o registo for bem-sucedido e falso se o nome de
    // utilizador já existir.
    public boolean registerUser(String username, String password) {
        if (!userCredentials.containsKey(username)) {
            userCredentials.put(username, password); // Adiciona o novo utilizador ao mapa
            return true; // Indica sucesso no registo
        }
        return false; // Indica que o nome de utilizador já existe
    }

    // Aqui podemos adicionar mais métodos conforme necessário, por exemplo, para
    // alterar password, excluir utilizador, etc.
}

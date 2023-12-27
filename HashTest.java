import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashTest {
    public static void main(String[] args) {
        String password = "123";
        String hashedPassword = hashPassword(password);
        System.out.println("Hash da senha '123': " + hashedPassword);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao hash a password", e);
        }
    }
}

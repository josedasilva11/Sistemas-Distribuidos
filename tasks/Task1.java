package tasks;
import java.util.Random;

public class Task1 {
    public static void main(String[] args) {
        Random random = new Random();
        int randomNumber = random.nextInt(10) + 1; // Generates a random number between 1 and 10

        System.out.println("Generated number: " + randomNumber);

        if (randomNumber < 4) {
            throw new RuntimeException("Error: Generated number is less than 4");
        }

        System.out.println("The number is greater than or equal to 4, continuing execution...");
    }
}

// Generates a random number between 1 and 10 and returns an error if it is less than 4
// TaskQueueManager.java
package src.server;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.*;

public class TaskQueueManager {

    private final AtomicLong availableMemory;
    private final LinkedList<Task> taskQueue = new LinkedList<>();
    private static final long WAIT_TIME_THRESHOLD = 60000;
    private static final int PRIORITY_LIMIT = 5; // Exemplo: limite de prioridade

    public TaskQueueManager(long totalMemory) {
        this.availableMemory = new AtomicLong(totalMemory);
    }

    public void addTask(Task task) {
        taskQueue.add(task);
    }

    public Task getNextTask() {
        if (taskQueue.isEmpty()) {
            return null;
        }

        Task nextTask = taskQueue.poll(); // Remove a primeira tarefa
        if (availableMemory.get() >= nextTask.getMemoryRequired()) {
            availableMemory.addAndGet(-nextTask.getMemoryRequired());
            return nextTask; // Executa a tarefa se houver memória suficiente
        } else {
            checkAndIncreasePriority(nextTask); // Aumenta a prioridade se necessário
            taskQueue.addLast(nextTask); // Re-adiciona ao final da fila se não puder ser executada
        }
        return null;
    }

    private void checkAndIncreasePriority(Task task) {
        long waitTime = System.currentTimeMillis() - task.getEnqueuedAt();
        if (waitTime > WAIT_TIME_THRESHOLD) {
            task.increasePriority();
            // Se a prioridade atingir um certo limite, pode-se considerar a execução
            // imediata da tarefa
            if (task.getPriority() >= PRIORITY_LIMIT) {
                // Lógica para execução imediata, se necessário
                // Por exemplo, você pode adicionar a tarefa de volta na frente da fila
            }
        }
    }

    public void releaseMemory(long memory) {
        availableMemory.addAndGet(memory);
    }

    public long getAvailableMemory() {
        return availableMemory.get();
    }

    public int getTaskCount() {
        return taskQueue.size();

    }

    public boolean canAddTask(Task task) {
        return availableMemory.get() >= task.getMemoryRequired();
    }
}

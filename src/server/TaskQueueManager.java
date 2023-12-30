// TaskQueueManager.java
package src.server;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

// Define a queue que gere as tarefas
public class TaskQueueManager {
    private final Queue<Task> taskQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong availableMemory;

    public TaskQueueManager(long totalMemory) {
        this.availableMemory = new AtomicLong(totalMemory);
    }

    public void addTask(Task task) {
        taskQueue.add(task);
    }

    public Task getNextTask() {
        Task nextTask = taskQueue.peek(); // Próxima Tarefa
        if (nextTask != null && availableMemory.get() >= nextTask.getMemoryRequired()) {
            availableMemory.addAndGet(-nextTask.getMemoryRequired());
            return taskQueue.poll();
        }
        return null;
    }

    // Liberta a memória quando a tarefa acaba
    public void releaseMemory(long memory) {
        availableMemory.addAndGet(memory);
    }

    // Obtém a memória disponível
    public long getAvailableMemory() {
        return availableMemory.get();
    }

    // Obtém o número de tarefas na fila
    public int getTaskCount() {
        return taskQueue.size();

    }

    // Devolve true se for possível adicionar uma tarefa
    public boolean canAddTask(Task task) {
        return availableMemory.get() >= task.getMemoryRequired();
    }
}

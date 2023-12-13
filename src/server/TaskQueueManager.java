// TaskQueueManager.java
package src.server;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

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
        Task nextTask = taskQueue.peek();
        if (nextTask != null && availableMemory.get() >= nextTask.getMemoryRequired()) {
            availableMemory.addAndGet(-nextTask.getMemoryRequired());
            return taskQueue.poll();
        }
        return null;
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
}

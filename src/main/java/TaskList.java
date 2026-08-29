import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Stores the tasks managed by Caesar and exposes task-list operations.
 *
 * <p>This class deliberately does not know about the user interface or file
 * format. It owns collection rules, such as the maximum number of tasks and
 * the one-based numbering used by commands.</p>
 */
public class TaskList implements Iterable<Task> {
    private static final int MAX_TASKS = 100;
    private static final String INVALID_TASK_NUMBER =
            "I couldn't locate that specific item number on our list. Take a quick look at /list/ to check the numbering.";

    private final ArrayList<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        tasks = new ArrayList<>(MAX_TASKS);
    }

    /**
     * Creates a task list containing a copy of the supplied tasks.
     *
     * @param initialTasks tasks loaded from storage
     * @throws CaesarException if there are more than 100 tasks
     */
    public TaskList(Collection<Task> initialTasks) throws CaesarException {
        if (initialTasks.size() > MAX_TASKS) {
            throw new CaesarException("Task file contains more than " + MAX_TASKS + " tasks.");
        }
        tasks = new ArrayList<>(initialTasks);
    }

    /** Adds a task when the list has not reached its capacity. */
    public void add(Task task) throws CaesarException {
        if (tasks.size() >= MAX_TASKS) {
            throw new CaesarException("You have too many tasks undone. Please finish some first before adding more");
        }
        tasks.add(task);
    }

    /** Removes and returns a task using the one-based number shown to users. */
    public Task delete(int taskNumber) throws CaesarException {
        validateTaskNumber(taskNumber);
        return tasks.remove(taskNumber - 1);
    }

    /** Inserts a task at a one-based position, preserving the displayed order. */
    public void insert(int taskNumber, Task task) throws CaesarException {
        if (tasks.size() >= MAX_TASKS) {
            throw new CaesarException("You have too many tasks undone. Please finish some first before adding more");
        }
        if (taskNumber < 1 || taskNumber > tasks.size() + 1) {
            throw new CaesarException(INVALID_TASK_NUMBER);
        }
        tasks.add(taskNumber - 1, task);
    }

    /** Returns a task using the one-based number shown to users. */
    public Task get(int taskNumber) throws CaesarException {
        validateTaskNumber(taskNumber);
        return tasks.get(taskNumber - 1);
    }

    /** Marks a task as done using the one-based number shown to users. */
    public void mark(int taskNumber) throws CaesarException {
        get(taskNumber).markAsDone();
    }

    /** Marks a task as not done using the one-based number shown to users. */
    public void unmark(int taskNumber) throws CaesarException {
        get(taskNumber).markAsNotDone();
    }

    /** Returns a copy sorted with pending tasks before completed tasks. */
    public ArrayList<Task> sortedByStatus() {
        ArrayList<Task> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort(Comparator.comparing(Task::isDone));
        return sortedTasks;
    }

    //Can add filtering methods here, e.g. by date, by type, etc.

    /** Returns the number of tasks in the list. */
    public int size() {
        return tasks.size();
    }

    /** Returns whether the list contains no tasks. */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /** Returns whether every task in the list is complete. */
    public boolean areAllDone() {
        return tasks.stream().allMatch(Task::isDone);
    }

    /** Returns the task descriptions in the same list-style form as the backing collection. */
    @Override
    public String toString() {
        return tasks.toString();
    }

    /** Allows storage code to read tasks without owning the collection.Allows for-loops over tasks */
    @Override
    public Iterator<Task> iterator() {
        return List.copyOf(tasks).iterator();
    }

    private void validateTaskNumber(int taskNumber) throws CaesarException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new CaesarException(INVALID_TASK_NUMBER);
        }
    }
}

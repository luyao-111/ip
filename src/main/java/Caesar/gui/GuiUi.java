package caesar.gui;

import caesar.exception.CaesarException;
import caesar.task.Task;
import caesar.task.TaskList;
import caesar.ui.Ui;

import java.util.ArrayList;

/** Collects command responses for display in the JavaFX conversation window. */
public class GuiUi extends Ui {
    private final StringBuilder response = new StringBuilder();

    /** Creates a user interface that stores responses instead of printing them. */
    public GuiUi() {
        super();
    }

    /** Clears the response collected from the previous command. */
    public void clearResponse() {
        response.setLength(0);
    }

    /** Returns the latest command response and clears it for the next command. */
    public String consumeResponse() {
        String message = response.toString();
        clearResponse();
        return message;
    }

    /** Records feedback after adding a task. */
    @Override
    public void showTaskAdded(Task task, TaskList tasks) {
        response.append("Got it. I've safely recorded this for you:\n")
                .append(task)
                .append("\nNow you have ")
                .append(tasks.size())
                .append(" tasks in the list.");
    }

    /** Records feedback after deleting a task. */
    @Override
    public void showTaskDeleted(Task task, TaskList tasks) {
        response.append("Noted. I've removed this task:\n")
                .append(task)
                .append("\nNow you have ")
                .append(tasks.size())
                .append(" tasks in the list.");
    }

    /** Records feedback after marking a task complete. */
    @Override
    public void showTaskMarked(Task task) {
        response.append("Well done. I've marked this as complete:\n").append(task);
    }

    /** Records feedback after returning a task to pending. */
    @Override
    public void showTaskUnmarked(Task task) {
        response.append("I've set this task back to pending:\n").append(task);
    }

    /** Records the current tasks with one-based numbering. */
    @Override
    public void showTaskList(Iterable<Task> tasks) throws CaesarException {
        ArrayList<Task> taskItems = new ArrayList<>();
        for (Task task : tasks) {
            taskItems.add(task);
        }

        if (taskItems.isEmpty()) {
            throw new CaesarException("Your task list is empty right now.");
        }

        response.append("Here are the tasks in your list:\n\n");
        for (int i = 0; i < taskItems.size(); i++) {
            response.append(i + 1).append(". ").append(taskItems.get(i)).append("\n");
        }
    }

    /** Records the farewell message. */
    @Override
    public void showGoodbye() {
        response.append("You handled today wonderfully. Until next time!");
    }
}

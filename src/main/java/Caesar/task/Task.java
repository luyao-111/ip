package Caesar.task;

import Caesar.exception.CaesarException;

/**
 * A task that can be completed or left pending.
 */
public class Task {
    private final String description;
    private boolean done;

    /** Creates a pending task with the supplied description. */
    public Task(String description) {
        this.description = description;
    }

    /** Marks this task as complete, rejecting an already-complete task. */
    public void markAsDone() throws CaesarException {
        if (done) {
            throw new CaesarException("You have marked this task!");
        }
        done = true;
    }

    /** Marks this task as pending, rejecting an already-pending task. */
    public void markAsNotDone() throws CaesarException {
        if (!done) {
            throw new CaesarException("You have unmarked this task!");
        }
        done = false;
    }

    /** Returns whether this task has been marked complete. */
    public boolean isDone() {
        return done;
    }

    /**
     * Returns the task text used when saving the task.
     */
    public String getDescription() {
        return description;
    }

    /** Returns the task in the standard completion-status display format. */
    @Override
    public String toString() {
        return (done ? "[X] " : "[ ] ") + description;
    }
}

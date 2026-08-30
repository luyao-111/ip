package caesar.command;

import caesar.exception.CaesarException;
import caesar.storage.Storage;
import caesar.task.Task;
import caesar.task.TaskList;
import caesar.ui.Ui;

/** Marks one task as pending and persists the updated task list. */
public class UnmarkCommand extends Command {
    /** One-based task number selected by the user. */
    private final int taskNumber;

    /** Creates an unmark command for the supplied one-based task number. */
    public UnmarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /** Unmarks the task, rolls back on a save failure, and reports success. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws CaesarException {
        Task task = tasks.get(taskNumber);
        tasks.unmark(taskNumber);
        try {
            storage.save(tasks);
        } catch (CaesarException exception) {
            task.markAsDone();
            throw exception;
        }
        ui.showTaskUnmarked(task);
    }
}

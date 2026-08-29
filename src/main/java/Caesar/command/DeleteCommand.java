package Caesar.command;

import Caesar.exception.CaesarException;
import Caesar.storage.Storage;
import Caesar.task.Task;
import Caesar.task.TaskList;
import Caesar.ui.Ui;

/** Deletes one task and persists the updated task list. */
public class DeleteCommand extends Command {
    /** One-based task number selected by the user. */
    private final int taskNumber;

    /** Creates a delete command for the supplied one-based task number. */
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /** Deletes the task, restoring it if saving fails, and reports success. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws CaesarException {
        Task removedTask = tasks.delete(taskNumber);
        try {
            storage.save(tasks);
        } catch (CaesarException exception) {
            tasks.insert(taskNumber, removedTask);
            throw exception;
        }
        ui.showTaskDeleted(removedTask, tasks);
    }
}

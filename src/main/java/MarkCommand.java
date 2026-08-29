/** Marks one task as complete and persists the updated task list. */
public class MarkCommand extends Command {
    /** One-based task number selected by the user. */
    private final int taskNumber;

    /** Creates a mark command for the supplied one-based task number. */
    public MarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /** Marks the task, rolls back on a save failure, and reports success. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws CaesarException {
        Task task = tasks.get(taskNumber);
        tasks.mark(taskNumber);
        try {
            storage.save(tasks);
        } catch (CaesarException exception) {
            task.markAsNotDone();
            throw exception;
        }
        ui.showTaskMarked(task);
    }
}

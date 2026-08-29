/** Adds one task to the task list and persists the updated list. */
public class AddCommand extends Command {
    /** The task that will be added when this command executes. */
    private final Task task;

    /** Creates an add command for the supplied task. */
    public AddCommand(Task task) {
        this.task = task;
    }

    /** Adds the task, rolls back on a save failure, and reports success. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws CaesarException {
        tasks.add(task);
        try {
            storage.save(tasks);
        } catch (CaesarException exception) {
            tasks.delete(tasks.size());
            throw exception;
        }
        ui.showTaskAdded(task, tasks);
    }
}

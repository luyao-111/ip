/** Lists the current tasks, optionally placing pending tasks first. */
public class ListCommand extends Command {
    /** Whether pending tasks should be shown before completed tasks. */
    private final boolean sorted;

    /** Creates a list command with the requested sorting behavior. */
    public ListCommand(boolean sorted) {
        this.sorted = sorted;
    }

    /** Displays either the current task order or the status-sorted order. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws CaesarException {
        if (sorted) {
            ui.showTaskList(tasks.sortedByStatus());
        } else {
            ui.showTaskList(tasks);
        }
    }
}

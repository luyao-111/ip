package caesar.command;

import caesar.exception.CaesarException;
import caesar.storage.Storage;
import caesar.task.TaskList;
import caesar.ui.Ui;

public class FindCommand extends Command {
    private final String task;

    /** Creates an find command for the supplied task. */
    public FindCommand(String task) {
        this.task = task;
    }

    /**Finds tasks including key word. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws CaesarException {
        TaskList foundTasks = tasks.find(task);
        ui.showTaskFound(foundTasks);
    }
}

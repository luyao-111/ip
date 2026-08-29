package Caesar.command;

import Caesar.storage.Storage;
import Caesar.task.TaskList;
import Caesar.ui.Ui;

/** A command that says goodbye and ends the application. */
public class ExitCommand extends Command {
    /** Displays the farewell message. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showGoodbye();
    }

    /** An exit command always ends the application loop. */
    @Override
    public boolean isExit() {
        return true;
    }
}

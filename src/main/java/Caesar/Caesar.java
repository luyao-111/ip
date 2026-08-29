package Caesar;

import Caesar.command.Command;
import Caesar.exception.CaesarException;
import Caesar.parser.Parser;
import Caesar.storage.Storage;
import Caesar.task.Task;
import Caesar.task.TaskList;
import Caesar.ui.Ui;

import java.util.ArrayList;

/**
 * Runs the Caesar command-line task assistant and coordinates task persistence.
 */
public class Caesar {
    /** Provides access to the task file. */
    private final Storage storage;
    /** Handles all console input and output. */
    private final Ui ui;
    /** Interprets commands entered by the user. */
    private final Parser parser;
    /** Holds the tasks currently managed by the application. */
    private final TaskList tasks;
    /** Records whether storage created the task file during construction. */
    private final boolean fileWasCreated;
    /** Stores a loading error so it can be displayed when {@link #run()} starts. */
    private final CaesarException loadingError;

    /** Supported command keywords. Kept here for compatibility with earlier levels. */
    public enum CommandType {
        TODO,
        DEADLINE,
        EVENT,
        LIST,
        MARK,
        UNMARK,
        DELETE,
        BYE,
        UNKNOWN;

        /** Converts a command keyword into its corresponding command type. */
        public static CommandType fromString(String command) {
            if (command == null || command.isBlank()) {
                return UNKNOWN;
            }

            try {
                return CommandType.valueOf(command.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                return UNKNOWN;
            }
        }
    }

    /** Creates Caesar with the supplied task-file path. */
    public Caesar(String filePath) {
        storage = new Storage(filePath);
        ui = new Ui();
        parser = new Parser();

        TaskList loadedTasks;
        CaesarException loadFailure = null;
        boolean createdFile = false;
        try {
            loadedTasks = new TaskList(storage.load());
            createdFile = storage.wasFileCreated();
        } catch (CaesarException exception) {
            loadFailure = exception;
            loadedTasks = new TaskList();
        }
        tasks = loadedTasks;
        fileWasCreated = createdFile;
        loadingError = loadFailure;
    }

    /** Runs the command loop until the user enters {@code bye} or input ends. */
    public void run() {
        ui.showWelcome();
        if (loadingError != null) {
            ui.showLoadingError(loadingError);
        } else if (fileWasCreated) {
            ui.showFileCreated(storage.getFilePath());
        }

        try {
            while (ui.hasNextCommand()) {
                String command = ui.readCommand();
                ui.showDivider();

                try {
                    Command commandObject = parser.parseCommand(command);
                    commandObject.execute(tasks, ui, storage);
                    if (commandObject.isExit()) {
                        return;
                    }
                } catch (CaesarException exception) {
                    ui.showError(exception);
                }
            }
        } finally {
            ui.close();
        }
    }

    /** Starts Caesar with its default task-file location. */
    public static void main(String[] args) {
        new Caesar("data/tasks.txt").run();
    }

    /**
     * Compatibility wrapper that accepts a string path for callers from earlier levels.
     */
    static ArrayList<Task> LoadTasksfromFile(String filePath) throws CaesarException {
        ArrayList<Task> tasks = new Storage(filePath).load();
        // Reuse TaskList's capacity validation for callers of the old helper.
        new TaskList(tasks);
        return tasks;
    }

}

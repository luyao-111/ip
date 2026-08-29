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

                Parser.ParsedCommand parsedCommand = parser.parse(command);
                CommandType commandType = parsedCommand.getType();
                String details = parsedCommand.getDetails();

                try {
                    switch (commandType) {
                        case TODO -> addTask(new ToDo(parser.requireDetails(details, "todo <description>")));
                        case DEADLINE -> addTask(parser.createDeadline(parser.requireDetails(
                                details, "deadline <description> /by <date or time>")));
                        case EVENT -> addTask(parser.createEvent(parser.requireDetails(
                                details, "event <description> /from <start> /to <end>")));
                        case LIST -> {
                            if ("sorted".equals(details)) {
                                ui.showTaskList(tasks.sortedByStatus());
                            } else {
                                //add list by date and time sorting later. maybe also just tasks of a specific date.
                                ui.showTaskList(tasks);
                            }
                        }
                        case MARK -> updateTaskStatus(CommandType.MARK, details);
                        case UNMARK -> updateTaskStatus(CommandType.UNMARK, details);
                        case DELETE -> deleteTask(details);
                        case BYE -> {
                            if (details != null) {
                                throw parser.unknownCommand();
                            }
                            Command exitCommand = new ExitCommand();
                            exitCommand.execute(tasks, ui, storage);
                            if (exitCommand.isExit()) {
                                return;
                            }
                        }
                        case UNKNOWN -> throw parser.unknownCommand();
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

    private void addTask(Task task) throws CaesarException {
        tasks.add(task);
        try {
            saveTasks();
        } catch (CaesarException exception) {
            tasks.delete(tasks.size());
            throw exception;
        }
        ui.showTaskAdded(task, tasks);
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

    // Compatibility wrapper that accepts a string path for callers from earlier levels.
    private void saveTasks() throws CaesarException {
        storage.save(tasks);
    }

    private void deleteTask(String details) throws CaesarException {
        int taskNumber = parser.parseTaskNumber(details, "delete <task number>");
        Task removedTask = tasks.delete(taskNumber);
        try {
            saveTasks();
        } catch (CaesarException exception) {
            // Restore the removed task at its original position if saving fails.
            tasks.insert(taskNumber, removedTask);
            throw exception;
        }
        ui.showTaskDeleted(removedTask, tasks);
    }

    private void updateTaskStatus(CommandType action, String details) throws CaesarException {
        int taskNumber = parser.parseTaskNumber(details, action.name().toLowerCase() + " <task number>");
        Task task = tasks.get(taskNumber);
        if (action == CommandType.MARK) {
            tasks.mark(taskNumber);
            try {
                saveTasks();
            } catch (CaesarException exception) {
                task.markAsNotDone();
                throw exception;
            }
            ui.showTaskMarked(task);
        } else {
            tasks.unmark(taskNumber);
            try {
                saveTasks();
            } catch (CaesarException exception) {
                task.markAsDone();
                throw exception;
            }
            ui.showTaskUnmarked(task);
        }
    }

}

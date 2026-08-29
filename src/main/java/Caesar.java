import java.util.ArrayList;

/**
 * Runs the Caesar command-line task assistant and coordinates task persistence.
 */
public class Caesar {
    /** Relative path so the application can be moved to another computer or OS. */
    private static final Storage STORAGE = new Storage("data/tasks.txt");

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

    public static void main(String[] args) {
        try (Ui ui = new Ui()) {
            ui.showWelcome();

            TaskList tasks;
            try {
                tasks = new TaskList(STORAGE.load());
                if (STORAGE.wasFileCreated()) {
                    ui.showFileCreated(STORAGE.getFilePath());
                }
            } catch (CaesarException exception) {
                ui.showLoadingError(exception);
                tasks = new TaskList();
            }

            Parser parser = new Parser();
            while (ui.hasNextCommand()) {
                String command = ui.readCommand();
                ui.showDivider();

                Parser.ParsedCommand parsedCommand = parser.parse(command);
                CommandType commandType = parsedCommand.getType();
                String details = parsedCommand.getDetails();

                try {
                    switch (commandType) {
                        case TODO -> addTask(tasks,
                                new ToDo(parser.requireDetails(details, "todo <description>")), ui);
                        case DEADLINE -> addTask(tasks, parser.createDeadline(parser.requireDetails(
                                details, "deadline <description> /by <date or time>")), ui);
                        case EVENT -> addTask(tasks, parser.createEvent(parser.requireDetails(
                                details, "event <description> /from <start> /to <end>")), ui);
                        case LIST -> {
                            if ("sorted".equals(details)) {
                                ui.showTaskList(tasks.sortedByStatus());
                            } else {
                                //add list by date and time sorting later. maybe also just tasks of a specific date.
                                ui.showTaskList(tasks);
                            }
                        }
                        case MARK -> updateTaskStatus(tasks, CommandType.MARK, details, parser, ui);
                        case UNMARK -> updateTaskStatus(tasks, CommandType.UNMARK, details, parser, ui);
                        case DELETE -> deleteTask(tasks, details, parser, ui);
                        case BYE -> {
                            if (details != null) {
                                throw parser.unknownCommand();
                            }
                            ui.showGoodbye();
                            return;
                        }
                        case UNKNOWN -> throw parser.unknownCommand();
                    }
                } catch (CaesarException exception) {
                    ui.showError(exception);
                }
            }
        }
    }
    private static void addTask(TaskList tasks, Task task, Ui ui) throws CaesarException {
        tasks.add(task);
        try {
            saveTasks(tasks);
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
    private static void saveTasks(TaskList tasks) throws CaesarException {
        STORAGE.save(tasks);
    }

    private static void deleteTask(TaskList tasks, String details, Parser parser, Ui ui) throws CaesarException {
        int taskNumber = parser.parseTaskNumber(details, "delete <task number>");
        Task removedTask = tasks.delete(taskNumber);
        try {
            saveTasks(tasks);
        } catch (CaesarException exception) {
            // Restore the removed task at its original position if saving fails.
            tasks.insert(taskNumber, removedTask);
            throw exception;
        }
        ui.showTaskDeleted(removedTask, tasks);
    }

    private static void updateTaskStatus(TaskList tasks, CommandType action,
                                         String details, Parser parser, Ui ui) throws CaesarException {
        int taskNumber = parser.parseTaskNumber(details, action.name().toLowerCase() + " <task number>");
        Task task = tasks.get(taskNumber);
        if (action == CommandType.MARK) {
            tasks.mark(taskNumber);
            try {
                saveTasks(tasks);
            } catch (CaesarException exception) {
                task.markAsNotDone();
                throw exception;
            }
            ui.showTaskMarked(task);
        } else {
            tasks.unmark(taskNumber);
            try {
                saveTasks(tasks);
            } catch (CaesarException exception) {
                task.markAsDone();
                throw exception;
            }
            ui.showTaskUnmarked(task);
        }
    }

}

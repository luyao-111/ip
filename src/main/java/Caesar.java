import java.util.ArrayList;
import java.util.Scanner;

/**
 * Runs the Caesar command-line task assistant and coordinates task persistence.
 */
public class Caesar {
    private static final String DIVIDER = "____________________________________________________________";
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
        String banner = "██████╗ █████╗ ███████╗███████╗ █████╗ ██████╗\n"
                + "██╔════╝██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗\n"
                + "██║     ███████║█████╗  ███████╗███████║██████╔╝\n"
                + "██║     ██╔══██║██╔══╝  ╚════██║██╔══██║██╔══██╗\n"
                + "╚██████╗██║  ██║███████╗███████║██║  ██║██║  ██║\n"
                + " ╚═════╝╚═╝  ╚═╝╚══════╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝\n";

        System.out.print(DIVIDER + "\n" + "\n");
        System.out.println(banner);
        System.out.println("Hello! I'm Caesar.\nYou look even brighter than the last time we spoke.\nHow may I ease your day today?");
        System.out.println("\nYou can enter the following commands: "
                + Parser.getCommandInstructions());
        System.out.println(DIVIDER);

        TaskList tasks;
        try {
            tasks = new TaskList(STORAGE.load());
            if (STORAGE.wasFileCreated()) {
                System.out.println("Task file not found. A new task file has been created at: "
                        + STORAGE.getFilePath());
            }
        } catch (CaesarException e) {
            System.out.println("Error loading tasks from file: " + e.getMessage());
            tasks = new TaskList();
        }

        Parser parser = new Parser();
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(DIVIDER);

                Parser.ParsedCommand parsedCommand = parser.parse(command);
                CommandType commandType = parsedCommand.getType();
                String details = parsedCommand.getDetails();

                try {
                    switch (commandType) {
                        case TODO -> addTask(tasks,
                                new ToDo(parser.requireDetails(details, "todo <description>")));
                        case DEADLINE -> addTask(tasks, parser.createDeadline(parser.requireDetails(
                                details, "deadline <description> /by <date or time>")));
                        case EVENT -> addTask(tasks, parser.createEvent(parser.requireDetails(
                                details, "event <description> /from <start> /to <end>")));
                        case LIST -> {
                            if ("sorted".equals(details)) {
                                printTaskList(tasks.sortedByStatus());
                            } else {
                                //add list by date and time sorting later. maybe also just tasks of a specific date.
                                printTaskList(tasks);
                            }
                        }
                        case MARK -> updateTaskStatus(tasks, CommandType.MARK, details, parser);
                        case UNMARK -> updateTaskStatus(tasks, CommandType.UNMARK, details, parser);
                        case DELETE -> deleteTask(tasks, details, parser);
                        case BYE -> {
                            if (details != null) {
                                throw parser.unknownCommand();
                            }
                            System.out.println("You handled today wonderfully. \nUntil next time—I'm always in your corner.");
                            System.out.println(DIVIDER);
                            return;
                        }
                        case UNKNOWN -> throw parser.unknownCommand();
                    }
                } catch (CaesarException exception) {
                    System.out.println(exception.getMessage());
                    System.out.println(DIVIDER);
                }
            }
        }
    }
    private static void addTask(TaskList tasks, Task task) throws CaesarException {
        tasks.add(task);
        try {
            saveTasks(tasks);
        } catch (CaesarException exception) {
            tasks.delete(tasks.size());
            throw exception;
        }
        System.out.println("Got it. I've safely recorded this for you:\n" + task);
        DynamicComment(tasks);
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

    private static void DynamicComment(TaskList tasks) {
        if (tasks.size() < 3) {
            System.out.println("Now you have " + tasks.size() + " tasks in the list.\n" 
            + "Here is what we have lined up: \n" + tasks +" \nA light and manageable day ahead—you've got this effortlessly."
            );
        } else if (tasks.size() < 7) {
            System.out.println("\nNow you have " + tasks.size() + " tasks in the list.\n"
            + "Here is your schedule for today: \n" + tasks + "\nSteady pace, one thing at a time—I'm right beside you:");
        } else {
            System.out.println("\nNow you have " + tasks.size() + " tasks in the list.\n"
            + "You have a full plate today: \n" + tasks + "\nRemember to take breaks and stay hydrated—let's tackle them together step by step!");
        }
        System.out.println(DIVIDER);
    }

    private static void deleteTask(TaskList tasks, String details, Parser parser) throws CaesarException {
        int taskNumber = parser.parseTaskNumber(details, "delete <task number>");
        Task removedTask = tasks.delete(taskNumber);
        try {
            saveTasks(tasks);
        } catch (CaesarException exception) {
            // Restore the removed task at its original position if saving fails.
            tasks.insert(taskNumber, removedTask);
            throw exception;
        }
        System.out.println("Noted. I've removed this task:\n" + removedTask
                + "\nNow you have " + tasks.size() + " tasks in the list.\nI'm glad that you got some of your own time");
        System.out.println(DIVIDER);
    }

    private static void printTaskList(Iterable<Task> tasks) throws CaesarException {
        ArrayList<Task> taskItems = new ArrayList<>();
        for (Task task : tasks) {
            taskItems.add(task);
        }
        if (taskItems.isEmpty()) {
            throw new CaesarException("Your schedule is completely clear right now. Take this time to relax and recharge");
        }

        System.out.println("Here are the tasks in your list:\n");
        for (int i = 0; i < taskItems.size(); i++) {
            System.out.println((i + 1) + "." + taskItems.get(i));
        }

        boolean allDone = taskItems.stream().allMatch(Task::isDone);
        if (allDone) {
            System.out.println("\nCongrats! You have completed all your tasks!");
        }
        System.out.println(DIVIDER);
    }

    private static void updateTaskStatus(TaskList tasks, CommandType action,
                                         String details, Parser parser) throws CaesarException {
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
            System.out.println("Well done, proud of your progress. I've marked this as complete:\n" + task);
        } else {
            tasks.unmark(taskNumber);
            try {
                saveTasks(tasks);
            } catch (CaesarException exception) {
                task.markAsDone();
                throw exception;
            }
            System.out.println("No worries at all, no need to rush. I've set this back to pending:\n" + task);
        }
        System.out.println(DIVIDER);
    }

}

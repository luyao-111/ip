import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Runs the Caesar command-line task assistant and coordinates task persistence.
 */
public class Caesar {
    private static final String DIVIDER = "____________________________________________________________";
    /** Relative path so the application can be moved to another computer or OS. */
    private static final Path TASK_FILE = Paths.get("data", "tasks.txt");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
    private static final String COMMANDS = "todo <description>, deadline <description> /by <date>, "
            + "event <description> /from <start> /to <end>, list, mark <number>, "
            + "unmark <number>, delete <number>, or bye";

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
        System.out.println("\nYou can enter the following commands: " + COMMANDS);
        System.out.println(DIVIDER);

        TaskList tasks;
        try {
            tasks = new TaskList(loadTasksFromFile(TASK_FILE));
        } catch (CaesarException e) {
            System.out.println("Error loading tasks from file: " + e.getMessage());
            tasks = new TaskList();
        }

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(DIVIDER);

                String[] commandParts = command.trim().split("\\s+", 2);
                String prefix = commandParts.length > 0 ? commandParts[0] : "";
                String details = commandParts.length > 1 ? commandParts[1] : null;
                CommandType commandType = CommandType.fromString(prefix);

                try {
                    switch (commandType) {
                        case TODO -> addTask(tasks,
                                new ToDo(requireDetails(details, "todo <description>")));
                        case DEADLINE -> addTask(tasks, createDeadline(requireDetails(
                                details, "deadline <description> /by <date or time>")));
                        case EVENT -> addTask(tasks, createEvent(requireDetails(
                                details, "event <description> /from <start> /to <end>")));
                        case LIST -> {
                            if ("sorted".equals(details)) {
                                printTaskList(tasks.sortedByStatus());
                            } else {
                                //add list by date and time sorting later. maybe also just tasks of a specific date.
                                printTaskList(tasks);
                            }
                        }
                        case MARK -> updateTaskStatus(tasks, CommandType.MARK, details);
                        case UNMARK -> updateTaskStatus(tasks, CommandType.UNMARK, details);
                        case DELETE -> deleteTask(tasks, details);
                        case BYE -> {
                            if (details != null) {
                                throw unknownCommand();
                            }
                            System.out.println("You handled today wonderfully. \nUntil next time—I'm always in your corner.");
                            System.out.println(DIVIDER);
                            return;
                        }
                        case UNKNOWN -> throw unknownCommand();
                    }
                } catch (CaesarException exception) {
                    System.out.println(exception.getMessage());
                    System.out.println(DIVIDER);
                }
            }
        }
    }
    
    /**
     * Converts a date string in the format YYYY-MM-DD to a more readable format.
     *
     * @param time the date string to convert
     * @return the converted date string in the format MMM d yyyy
     * @throws CaesarException if the input date string is not in the expected format
     */

    private static String convertTime(String time) throws CaesarException {
        String trimmed = time.trim();
        LocalDate date;

        try {
            // 1. Try standard ISO format: yyyy-MM-dd
            date = LocalDate.parse(trimmed);
        } catch (DateTimeParseException e1) {
            try {
                // 2. Fallback: try MMM d yyyy to avoid error when reading the file
                date = LocalDate.parse(trimmed, DISPLAY_FORMAT);
            } catch (DateTimeParseException e2) {
                throw new CaesarException("Invalid date format. Please use YYYY-MM-DD or MMM d yyyy.");
            }
        }
        return date.format(DISPLAY_FORMAT);
    }

    /**
     * Loads all saved tasks from a relative, platform-independent path.
     * A missing file and its parent directory are created automatically.
     *
     * @param filePath path of the task file
     * @return tasks read from the file
     * @throws CaesarException if the file cannot be read or contains invalid data
     */
    private static ArrayList<Task> loadTasksFromFile(Path filePath) throws CaesarException {
        ArrayList<Task> tasks = new ArrayList<>();
        File file = filePath.toFile();
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("Task file not found. A new task file has been created at: " + filePath);
            }

            // Scanner reads the saved file line by line.
            try (Scanner fileScanner = new Scanner(file)) {
                int lineNumber = 0;
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    lineNumber++;
                    if (line.isBlank()) {
                        continue;
                    }
                    tasks.add(parseTask(line, lineNumber));
                }
            }
        } catch (FileNotFoundException exception) {
            throw new CaesarException("Failed to read task file " + filePath + ": " + exception.getMessage());
        } catch (IOException exception) {
            throw new CaesarException("Failed to read task file " + filePath + ": " + exception.getMessage());
        }
        return tasks;
    }

    /**
     * Compatibility wrapper that accepts a string path for callers from earlier levels.
     */
    static ArrayList<Task> LoadTasksfromFile(String filePath) throws CaesarException {
        return loadTasksFromFile(Paths.get(filePath));
    }

    /**
     * Saves the complete task list to the task file.
     */
    private static void saveTasksToFile(Iterable<Task> tasks, Path filePath) throws CaesarException {
        Path parent = filePath.getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            // FileWriter overwrites the file by default; the full task list is saved each time.
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                for (Task task : tasks) {
                    writer.write(serializeTask(task));
                    writer.write(System.lineSeparator()); //OS-independent line separator (replace other \n later?)
                }
            }
        } catch (IOException exception) {
            throw new CaesarException("Failed to save tasks to file " + filePath + ": " + exception.getMessage());
        }
    }

    private static String serializeTask(Task task) {
        String status = task.isDone() ? "1" : "0";
        if (task instanceof Deadline deadline) {
            return String.join(" | ", "D", status, task.getDescription(), deadline.getBy());
        }
        if (task instanceof Event event) {
            return String.join(" | ", "E", status, task.getDescription(), event.getStart(), event.getEnd());
        }
        return String.join(" | ", "T", status, task.getDescription());
    }

    private static Task parseTask(String line, int lineNumber) throws CaesarException {
        String[] parts = line.split("\\|", -1);
        String type = parts.length > 0 ? parts[0].trim() : "";
        int expectedParts = switch (type) {
            case "T" -> 3;
            case "D" -> 4;
            case "E" -> 5;
            default -> throw invalidTaskLine(lineNumber, "unknown task type " + type);
        };
        if (parts.length != expectedParts) {
            throw invalidTaskLine(lineNumber, "expected " + expectedParts + " fields but found " + parts.length);
        }

        boolean isDone = parseStatus(parts[1].trim(), lineNumber);
        String description = requireFileField(parts[2], "description", lineNumber);
        Task task;
        if ("T".equals(type)) {
            task = new ToDo(description);
        } else if ("D".equals(type)) {
            task = new Deadline(description, requireFileField(parts[3], "deadline", lineNumber));
        } else {
            task = new Event(description,
                    requireFileField(parts[3], "event start", lineNumber),
                    requireFileField(parts[4], "event end", lineNumber));
        }

        if (isDone) {
            try {
                task.markAsDone();
            } catch (CaesarException exception) {
                throw invalidTaskLine(lineNumber, exception.getMessage());
            }
        }
        return task;
    }

    private static boolean parseStatus(String status, int lineNumber) throws CaesarException {
    if ("1".equals(status)) {
        return true;
    }
    if ("0".equals(status)) {
        return false;
    }
    throw invalidTaskLine(lineNumber, "status must be 1 or 0");
    }

    private static String requireFileField(String value, String fieldName, int lineNumber) throws CaesarException {
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw invalidTaskLine(lineNumber, fieldName + " cannot be empty");
        }
        return trimmedValue;
    }

    private static CaesarException invalidTaskLine(int lineNumber, String reason) {
        return new CaesarException("Invalid task data on line " + lineNumber + ": " + reason);
    }

    // Compatibility wrapper that accepts a string path for callers from earlier levels.
    private static void saveTasks(TaskList tasks) throws CaesarException {
        saveTasksToFile(tasks, TASK_FILE);
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

    private static void deleteTask(TaskList tasks, String details) throws CaesarException {
        int taskNumber = parseTaskNumber(details, "delete <task number>");
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
                                         String details) throws CaesarException {
        int taskNumber = parseTaskNumber(details, action.name().toLowerCase() + " <task number>");
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

    private static Task createDeadline(String details) throws CaesarException {
        String[] commandParts = details.split("/by", 2);
        if (commandParts.length != 2) {
            throw missingDetails("deadline <description> /by <date or time>");
        }

        String description = commandParts[0].trim();
        String by = commandParts[1].trim();
        String formattedBy = convertTime(by);
        if (description.isEmpty() || by.isEmpty()) {
            throw missingDetails("deadline <description> /by <date or time>");
        }
        return new Deadline(description, formattedBy);
    }

    private static Task createEvent(String details) throws CaesarException {
        String[] commandParts = details.split("/from", 2);
        if (commandParts.length != 2) {
            throw missingDetails("event <description> /from <start> /to <end>");
        }

        String[] timeParts = commandParts[1].split("/to", 2);
        if (timeParts.length != 2) {
            throw missingDetails("event <description> /from <start> /to <end>");
        }

        String description = commandParts[0].trim();
        String start = timeParts[0].trim();
        String end = timeParts[1].trim();
        String formattedStart = convertTime(start);
        String formattedEnd = convertTime(end);
        if (description.isEmpty() || start.isEmpty() || end.isEmpty()) {
            throw missingDetails("event <description> /from <start> /to <end>");
        }
        return new Event(description, formattedStart, formattedEnd);
    }

    private static int parseTaskNumber(String details, String format) throws CaesarException {
        if (details == null || details.trim().isEmpty()) {
            throw missingDetails(format);
        }

        try {
            return Integer.parseInt(details.trim());
        } catch (NumberFormatException exception) {
            throw new CaesarException("Please provide a valid task number.");
        }
    }

    private static String requireDetails(String details, String format) throws CaesarException {
        if (details == null || details.trim().isEmpty()) {
            throw missingDetails(format);
        }
        return details.trim();
    }

    private static CaesarException missingDetails(String format) {
        return new CaesarException("I'd love to organize that for you, but I just need more details. Try enter in this format: " + format);
    }

    private static CaesarException unknownCommand() {
        return new CaesarException("I'm not quite sure I caught that command, but take your time. Let's try again. "
                + "Try these commands: " + COMMANDS);
    }
}

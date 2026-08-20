import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Caesar {
    private static final String DIVIDER = "____________________________________________________________";
    private static final int MAX_TASKS = 100;
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
        ArrayList<Task> tasks = new ArrayList<>(MAX_TASKS);

        System.out.print(DIVIDER + "\n" + "\n");
        System.out.println(banner);
        System.out.println("Hello! I'm Caesar.\nWhat can I do for you?");
        System.out.println("You can enter the following commands: " + COMMANDS);
        System.out.println(DIVIDER);

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
                                ArrayList<Task> sortedTasks = new ArrayList<>(tasks);
                                sortedTasks.sort(Comparator.comparing(Task::isDone));
                                printTaskList(sortedTasks);
                            } else {
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
                            System.out.println("Bye. Hope to see you again soon!");
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

    private static void addTask(ArrayList<Task> tasks, Task task) throws CaesarException {
        if (tasks.size() == MAX_TASKS) {
            throw new CaesarException("You have too many tasks undone. Please finish some first before adding more");
        }

        tasks.add(task);
        System.out.println("Got it. I've added this task:\n" + task
                + "\nNow you have " + tasks.size() + " tasks in the list.");
        System.out.println(DIVIDER);
    }

    private static void deleteTask(ArrayList<Task> tasks, String details) throws CaesarException {
        int taskNumber = parseTaskNumber(details, "delete <task number>");
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new CaesarException("That task number does not exist.");
        }

        Task removedTask = tasks.remove(taskNumber - 1);
        System.out.println("Noted. I've removed this task:\n" + removedTask
                + "\nNow you have " + tasks.size() + " tasks in the list.");
        System.out.println(DIVIDER);
    }

    private static void printTaskList(ArrayList<Task> tasks) throws CaesarException {
        if (tasks.isEmpty()) {
            throw new CaesarException("Congrats! You have no todos now! Enjoy the day!");
        }

        System.out.println("Here are the tasks in your list:\n");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }

        boolean allDone = true;
        for (Task task : tasks) {
            if (!task.isDone()) {
                allDone = false;
                break;
            }
        }
        if (allDone) {
            System.out.println("\nCongrats! You have completed all your tasks!");
        }
        System.out.println(DIVIDER);
    }

    private static void updateTaskStatus(ArrayList<Task> tasks, CommandType action,
                                         String details) throws CaesarException {
        int taskNumber = parseTaskNumber(details, action.name().toLowerCase() + " <task number>");
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new CaesarException("That task number does not exist.");
        }

        Task task = tasks.get(taskNumber - 1);
        if (action == CommandType.MARK) {
            task.markAsDone();
            System.out.println("Nice! I've marked this task as done:\n" + task);
        } else {
            task.markAsNotDone();
            System.out.println("OK, I've marked this task as not done yet:\n" + task);
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
        if (description.isEmpty() || by.isEmpty()) {
            throw missingDetails("deadline <description> /by <date or time>");
        }
        return new Deadline(description, by);
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
        if (description.isEmpty() || start.isEmpty() || end.isEmpty()) {
            throw missingDetails("event <description> /from <start> /to <end>");
        }
        return new Event(description, start, end);
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
        return new CaesarException("May I know more details? Try enter in this format: " + format);
    }

    private static CaesarException unknownCommand() {
        return new CaesarException("I'm sorry, I'm afraid that I can't help you with this. "
                + "Try these commands: " + COMMANDS);
    }
}

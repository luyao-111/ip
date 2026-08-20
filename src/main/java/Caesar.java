import java.util.Scanner;

public class Caesar {
    private static final String DIVIDER = "____________________________________________________________";
    private static final int MAX_TASKS = 100;
    private static final String COMMANDS = "todo <description>, deadline <description> /by <date>, "
            + "event <description> /from <start> /to <end>, list, mark <number>, "
            + "unmark <number>, or bye";

    public static void main(String[] args) {
        String banner = "██████╗ █████╗ ███████╗███████╗ █████╗ ██████╗\n"
                + "██╔════╝██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗\n"
                + "██║     ███████║█████╗  ███████╗███████║██████╔╝\n"
                + "██║     ██╔══██║██╔══╝  ╚════██║██╔══██║██╔══██╗\n"
                + "╚██████╗██║  ██║███████╗███████║██║  ██║██║  ██║\n"
                + " ╚═════╝╚═╝  ╚═╝╚══════╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝\n";
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;
        int taskDone = 0;

        System.out.print(DIVIDER + "\n" + "\n");
        System.out.println(banner);
        System.out.println("Hello! I'm Caesar.\nWhat can I do for you?");
        System.out.println("You can enter the following commands: " + COMMANDS);
        System.out.println(DIVIDER);

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(DIVIDER);

                try {
                    String[] commandParts = command.trim().split("\\s+", 2);
                    String prefix = commandParts[0];
                    String details = commandParts.length > 1 ? commandParts[1] : null;

                    if ("bye".equals(command)) {
                        System.out.println("Bye. Hope to see you again soon!");
                        System.out.println(DIVIDER);
                        break;
                    } else if ("list".equals(prefix)) {
                        printTaskList(tasks, taskCount);
                    } else if ("mark".equals(prefix) || "unmark".equals(prefix)) {
                        updateTaskStatus(tasks, taskCount, taskDone, prefix, details);
                    } else if ("todo".equals(prefix)) {
                        taskCount = addTask(tasks, taskCount,
                                new ToDo(requireDetails(details, "todo <description>")));
                    } else if ("deadline".equals(prefix)) {
                        taskCount = addTask(tasks, taskCount, createDeadline(requireDetails(
                                details, "deadline <description> /by <date or time>")));
                    } else if ("event".equals(prefix)) {
                        taskCount = addTask(tasks, taskCount, createEvent(requireDetails(
                                details, "event <description> /from <start> /to <end>")));
                    } else {
                        throw new CaesarException("I'm sorry, I'm afraid that I can't help you with this.\n "
                                + "Try these commands: " + COMMANDS);
                    }
                } catch (CaesarException exception) {
                    System.out.println(exception.getMessage());
                    System.out.println(DIVIDER);
                }
            }
        }
    }

    private static int addTask(Task[] tasks, int taskCount, Task task) throws CaesarException {
        if (taskCount == tasks.length) {
            throw new CaesarException("You have too many tasks undone. Please finish some first before adding more");
        }

        tasks[taskCount] = task;
        taskCount++;
        System.out.println("Got it. I've added this task:\n" + task
                + "\nNow you have " + taskCount + " tasks in the list.");
        System.out.println(DIVIDER);
        return taskCount;
    }

    private static void printTaskList(Task[] tasks, int taskCount) throws CaesarException {
        if (taskCount == 0) {
            throw new CaesarException("Congrats! You have no todos now! Enjoy the day!");
        }

        System.out.println("Here are the tasks in your list:\n");
        for (int i = 0; i < taskCount; i++) {
            System.out.println((i + 1) + "." + tasks[i]);
        }
        System.out.println(DIVIDER);
    }

    private static void updateTaskStatus(Task[] tasks, int taskCount, int taskDone,
                                         String action, String details) throws CaesarException {
        if (details == null || details.trim().isEmpty()) {
            throw new CaesarException("May I know more details? Try enter in this format: "
                    + action + " <task number>");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(details.trim());
        } catch (NumberFormatException exception) {
            throw new CaesarException("Please provide a valid task number.");
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new CaesarException("That task number does not exist.");
        }

        Task task = tasks[taskNumber - 1];
        if ("mark".equals(action)) {
            task.markAsDone();
            taskDone++;
            System.out.println("Nice! I've marked this task as done:\n" + task);
            if (taskDone == taskCount) {
                System.out.println("Congrats! You have completed all your tasks!");
            }
        } else {
            task.markAsNotDone();
            taskDone--;
            System.out.println("OK, I've marked this task as not done yet:\n" + task);
        }
        System.out.println(DIVIDER);
    }

    private static Task createDeadline(String details) throws CaesarException {
        String[] commandParts = details.split("/by", 2);
        if (commandParts.length != 2) {
            throw new CaesarException("May I know more details? Try enter in this format: "
                    + "deadline <description> /by <date or time>");
        }

        String description = commandParts[0].trim();
        String by = commandParts[1].trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new CaesarException("May I know more details? Try enter in this format: "
                    + "deadline <description> /by <date or time>");
        }
        return new Deadline(description, by);
    }

    private static Task createEvent(String details) throws CaesarException {
        String[] commandParts = details.split("/from", 2);
        if (commandParts.length != 2) {
            throw new CaesarException("May I know more details? Try enter in this format: "
                    + "event <description> /from <start> /to <end>");
        }

        String[] timeParts = commandParts[1].split("/to", 2);
        if (timeParts.length != 2) {
            throw new CaesarException("May I know more details? Try enter in this format: "
                    + "event <description> /from <start> /to <end>");
        }

        String description = commandParts[0].trim();
        String start = timeParts[0].trim();
        String end = timeParts[1].trim();
        if (description.isEmpty() || start.isEmpty() || end.isEmpty()) {
            throw new CaesarException("May I know more details? Try enter in this format: "
                    + "event <description> /from <start> /to <end>");
        }
        return new Event(description, start, end);
    }

    private static String requireDetails(String details, String format) throws CaesarException {
        if (details == null || details.trim().isEmpty()) {
            throw new CaesarException("May I know more details? Try enter in this format: " + format);
        }
        return details.trim();
    }
}

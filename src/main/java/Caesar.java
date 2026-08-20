import java.util.Scanner;

public class Caesar {
    private static final String DIVIDER = "____________________________________________________________";
    private static final int MAX_TASKS = 100;

    public static void main(String[] args) {
        String banner = "██████╗ █████╗ ███████╗███████╗ █████╗ ██████╗\n"
                + "██╔════╝██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗\n"
                + "██║     ███████║█████╗  ███████╗███████║██████╔╝\n"
                + "██║     ██╔══██║██╔══╝  ╚════██║██╔══██║██╔══██╗\n"
                + "╚██████╗██║  ██║███████╗███████║██║  ██║██║  ██║\n"
                + " ╚═════╝╚═╝  ╚═╝╚══════╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝\n";
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        System.out.print(DIVIDER + "\n" + "\n");
        System.out.println(banner);
        System.out.println("Hello! I'm Caesar.\nWhat can I do for you?");
        System.out.println("GUIDES TO BE ADDED");
        System.out.println(DIVIDER);

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(DIVIDER);

                if ("bye".equals(command)) {
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(DIVIDER);
                    break;
                } else if ("list".equals(command)) {
                    printTaskList(tasks, taskCount);
                } else if (command.startsWith("mark ") || command.startsWith("unmark ")) {
                    updateTaskStatus(tasks, taskCount, command);
                } else if (command.startsWith("todo ")) {
                    taskCount = addTask(tasks, taskCount,
                            new ToDo(command.substring("todo ".length()).trim()));
                } else if (command.startsWith("deadline ")) {
                    Task deadline = createDeadline(command);
                    if (deadline != null) {
                        taskCount = addTask(tasks, taskCount, deadline);
                    } else {
                        printInvalidFormat("deadline <description> /by <date or time>");
                    }
                } else if (command.startsWith("event ")) {
                    Task event = createEvent(command);
                    if (event != null) {
                        taskCount = addTask(tasks, taskCount, event);
                    } else {
                        printInvalidFormat("event <description> /from <start> /to <end>");
                    }
                } else {
                    printInvalidFormat("Please enter a valid command: todo <description>, deadline <description> /by <date>, "
                            + "or event <description> /from <start> /to <end>");
                }
            }
        }
    }

    private static int addTask(Task[] tasks, int taskCount, Task task) {
        if (taskCount == tasks.length) {
            System.out.println("Your task list is full.");
            System.out.println(DIVIDER);
            return taskCount;
        }

        tasks[taskCount] = task;
        taskCount++;
        System.out.println("Got it. I've added this task:\n" + task
                + "\nNow you have " + taskCount + " tasks in the list.");
        System.out.println(DIVIDER);
        return taskCount;
    }

    private static void printTaskList(Task[] tasks, int taskCount) {
        System.out.println("Here are the tasks in your list:\n");
        for (int i = 0; i < taskCount; i++) {
            System.out.println((i + 1) + "." + tasks[i]);
        }
        System.out.println(DIVIDER);
    }

    private static void updateTaskStatus(Task[] tasks, int taskCount, String command) {
        String[] commandParts = command.split(" ", 2);
        try {
            int taskNumber = Integer.parseInt(commandParts[1]);
            if (taskNumber < 1 || taskNumber > taskCount) {
                System.out.println("That task number does not exist.");
            } else {
                Task task = tasks[taskNumber - 1];
                if (command.startsWith("mark ")) {
                    task.markAsDone();
                    System.out.println("Nice! I've marked this task as done:\n" + task);
                } else {
                    task.markAsNotDone();
                    System.out.println("OK, I've marked this task as not done yet:\n" + task);
                }
            }
        } catch (NumberFormatException exception) {
            System.out.println("Please provide a valid task number.");
        }
        System.out.println(DIVIDER);
    }

    private static Task createDeadline(String command) {
        String[] commandParts = command.substring("deadline ".length()).split("/by", 2);
        if (commandParts.length != 2) {
            return null;
        }

        String description = commandParts[0].trim();
        String by = commandParts[1].trim();
        if (description.isEmpty() || by.isEmpty()) {
            return null;
        }
        return new Deadline(description, by);
    }

    private static Task createEvent(String command) {
        String[] commandParts = command.substring("event ".length()).split("/from", 2);
        if (commandParts.length != 2) {
            return null;
        }

        String[] timeParts = commandParts[1].split("/to", 2);
        if (timeParts.length != 2) {
            return null;
        }

        String description = commandParts[0].trim();
        String start = timeParts[0].trim();
        String end = timeParts[1].trim();
        if (description.isEmpty() || start.isEmpty() || end.isEmpty()) {
            return null;
        }
        return new Event(description, start, end);
    }

    private static void printInvalidFormat(String expectedFormat) {
        System.out.println("Invalid command. Use: " + expectedFormat);
        System.out.println(DIVIDER);
    }
}

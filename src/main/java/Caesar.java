import java.util.Scanner;

public class Caesar {
    private static final String DIVIDER = "____________________________________________________________";
    private static final int MAX_TASKS = 100;

    private static class Task {
        private final String description;
        private boolean done;

        Task(String description) {
            this.description = description;
        }

        void markAsDone() {
            done = true;
            System.out.println("Nice! I've marked this task as done:\n" + this);
        }

        void markAsNotDone() {
            done = false;
            System.out.println("OK, I've marked this task as not done yet:\n" + this);
        }

        @Override
        public String toString() {
            return (done ? "[X] " : "[ ] ") + description;
        }
    }

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
                    System.out.println("Here are the tasks in your list:\n");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + ". " + tasks[i]);
                    }
                    System.out.println(DIVIDER);
                } else if (command.startsWith("mark " ) || command.startsWith("unmark ")) {
                    String[] commandParts = command.split(" ", 2);
                    try {
                        int taskNumber = Integer.parseInt(commandParts[1]);
                        if (taskNumber < 1 || taskNumber > taskCount) {
                            System.out.println("That task number does not exist.");
                        } else {
                            Task task = tasks[taskNumber - 1];
                            if (command.startsWith("mark ")) {
                                task.markAsDone();
                            } else {
                                task.markAsNotDone();
                            }
                        }
                    } catch (NumberFormatException exception) {
                        System.out.println("Please provide a valid task number.");
                    }
                    System.out.println(DIVIDER);
                } else if (taskCount < MAX_TASKS) {
                    tasks[taskCount] = new Task(command);
                    taskCount++;
                    System.out.println("added: " + command);
                    System.out.println(DIVIDER);
                } else {
                    System.out.println("Your task list is full.");
                    System.out.println(DIVIDER);
                }
            }
        }
    }
}

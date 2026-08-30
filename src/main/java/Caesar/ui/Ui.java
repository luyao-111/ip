package Caesar.ui;

import Caesar.exception.CaesarException;
import Caesar.parser.Parser;
import Caesar.task.Task;
import Caesar.task.TaskList;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Handles Caesar's console input and user-facing output.
 *
 * <p>Ui knows how to present information, but it does not decide which task
 * operation should happen. That decision remains in {@link Caesar}.</p>
 */
public class Ui implements AutoCloseable {
    private static final String DIVIDER = "____________________________________________________________";
    private final Scanner scanner;

    /** Creates a console user interface backed by standard input. */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /** Prints the greeting and available commands. */
    public void showWelcome() {
        String banner = "██████╗ █████╗ ███████╗███████╗ █████╗ ██████╗\n"
                + "██╔════╝██╔══██╗██╔════╝██╔════╝██╔══██╗██╔══██╗\n"
                + "██║     ███████║█████╗  ███████╗███████║██████╔╝\n"
                + "██║     ██╔══██║██╔══╝  ╚════██║██╔══██║██╔══██╗\n"
                + "╚██████╗██║  ██║███████╗███████║██║  ██║██║  ██║\n"
                + " ╚═════╝╚═╝  ╚═╝╚══════╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝\n";

        showDivider();
        System.out.println();
        System.out.println(banner);
        System.out.println("Hello! I'm Caesar.\nYou look even brighter than the last time we spoke.\nHow may I ease your day today?");
        System.out.println("\nYou can enter the following commands: "
                + Parser.getCommandInstructions());
        showDivider();
    }

    /** Returns whether another command is available from standard input. */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Reads the next command from standard input. */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Prints the standard visual divider. */
    public void showDivider() {
        System.out.println(DIVIDER);
    }

    /** Reports that the task file had to be created. */
    public void showFileCreated(Path filePath) {
        System.out.println("Task file not found. A new task file has been created at: " + filePath);
    }

    /** Reports a failure while loading tasks. */
    public void showLoadingError(CaesarException exception) {
        System.out.println("Error loading tasks from file: " + exception.getMessage());
    }

    /** Reports a command-processing error and prints a divider afterwards. */
    public void showError(CaesarException exception) {
        System.out.println(exception.getMessage());
        showDivider();
    }

    /** Prints the farewell message. */
    public void showGoodbye() {
        System.out.println("You handled today wonderfully. \nUntil next time—I'm always in your corner.");
        showDivider();
    }

    /** Prints feedback after adding a task and the dynamic task-count comment. */
    public void showTaskAdded(Task task, TaskList tasks) {
        System.out.println("Got it. I've safely recorded this for you:\n" + task);
        showDynamicComment(tasks);
    }

    /** Prints feedback after deleting a task. */
    public void showTaskDeleted(Task task, TaskList tasks) {
        System.out.println("Noted. I've removed this task:\n" + task
                + "\nNow you have " + tasks.size() + " tasks in the list.\nI'm glad that you got some of your own time");
        showDivider();
    }

    /** Prints feedback after marking a task complete. */
    public void showTaskMarked(Task task) {
        System.out.println("Well done, proud of your progress. I've marked this as complete:\n" + task);
        showDivider();
    }

    /** Prints feedback after returning a task to pending. */
    public void showTaskUnmarked(Task task) {
        System.out.println("No worries at all, no need to rush. I've set this back to pending:\n" + task);
        showDivider();
    }

    /** Prints tasks with one-based numbering and completion feedback. */
    public void showTaskList(Iterable<Task> tasks) throws CaesarException {
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

        if (taskItems.stream().allMatch(Task::isDone)) {
            System.out.println("\nCongrats! You have completed all your tasks!");
        }
        showDivider();
    }

    /** Prints encouragement tailored to the current number of tasks. */
    private void showDynamicComment(TaskList tasks) {
        if (tasks.size() < 3) {
            System.out.println("Now you have " + tasks.size() + " tasks in the list.\n"
                    + "Here is what we have lined up: \n" + tasks
                    + " \nA light and manageable day ahead—you've got this effortlessly.");
        } else if (tasks.size() < 7) {
            System.out.println("\nNow you have " + tasks.size() + " tasks in the list.\n"
                    + "Here is your schedule for today: \n" + tasks
                    + "\nSteady pace, one thing at a time—I'm right beside you:");
        } else {
            System.out.println("\nNow you have " + tasks.size() + " tasks in the list.\n"
                    + "You have a full plate today: \n" + tasks
                    + "\nRemember to take breaks and stay hydrated—let's tackle them together step by step!");
        }
        showDivider();
    }

    /** Closes the standard-input scanner. */
    @Override
    public void close() {
        scanner.close();
    }
}

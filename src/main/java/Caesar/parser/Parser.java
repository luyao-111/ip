package Caesar.parser;

import Caesar.Caesar;
import Caesar.command.AddCommand;
import Caesar.command.Command;
import Caesar.command.DeleteCommand;
import Caesar.command.ExitCommand;
import Caesar.command.ListCommand;
import Caesar.command.MarkCommand;
import Caesar.command.UnmarkCommand;
import Caesar.exception.CaesarException;
import Caesar.task.Deadline;
import Caesar.task.Event;
import Caesar.task.Task;
import Caesar.task.ToDo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Interprets user commands and creates tasks from command details.
 *
 * <p>Parser does not read from or write to the console. It converts raw input
 * into structured values so that {@link Caesar} can coordinate the user
 * interface and task operations.</p>
 */
public class Parser {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
    private static final String COMMANDS = "todo <description>, deadline <description> /by <date>, "
            + "event <description> /from <start> /to <end>, list, mark <number>, "
            + "unmark <number>, delete <number>, or bye";

    /** Returns the command instructions shown by the user interface. */
    public static String getCommandInstructions() {
        return COMMANDS;
    }

    /** Splits raw input into a command type and its optional details. */
    public ParsedCommand parse(String command) {
        String[] commandParts = command == null
                ? new String[]{""}
                : command.trim().split("\\s+", 2);
        String prefix = commandParts.length > 0 ? commandParts[0] : "";
        String details = commandParts.length > 1 ? commandParts[1] : null;
        return new ParsedCommand(Caesar.CommandType.fromString(prefix), details);
    }

    /** Parses raw input and creates the command that should execute it. */
    public Command parseCommand(String command) throws CaesarException {
        ParsedCommand parsedCommand = parse(command);
        String details = parsedCommand.getDetails();

        return switch (parsedCommand.getType()) {
            case TODO -> new AddCommand(new ToDo(requireDetails(details, "todo <description>")));
            case DEADLINE -> new AddCommand(createDeadline(requireDetails(
                    details, "deadline <description> /by <date or time>")));
            case EVENT -> new AddCommand(createEvent(requireDetails(
                    details, "event <description> /from <start> /to <end>")));
            case LIST -> new ListCommand("sorted".equals(details));
            case MARK -> new MarkCommand(parseTaskNumber(details, "mark <task number>"));
            case UNMARK -> new UnmarkCommand(parseTaskNumber(details, "unmark <task number>"));
            case DELETE -> new DeleteCommand(parseTaskNumber(details, "delete <task number>"));
            case BYE -> {
                if (details != null) {
                    throw unknownCommand();
                }
                yield new ExitCommand();
            }
            case UNKNOWN -> throw unknownCommand();
        };
    }

    /** Creates a deadline from details in the form {@code description /by date}. */
    public Task createDeadline(String details) throws CaesarException {
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

    /** Creates an event from details in the form {@code description /from start /to end}. */
    public Task createEvent(String details) throws CaesarException {
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

    /** Converts a date into the display format used by saved tasks. */
    private String convertTime(String time) throws CaesarException {
        String trimmed = time.trim();
        LocalDate date;

        try {
            date = LocalDate.parse(trimmed);
        } catch (DateTimeParseException firstException) {
            try {
                date = LocalDate.parse(trimmed, DISPLAY_FORMAT);
            } catch (DateTimeParseException secondException) {
                throw new CaesarException("Invalid date format. Please use YYYY-MM-DD or MMM d yyyy.");
            }
        }
        return date.format(DISPLAY_FORMAT);
    }

    /** Converts a command argument into a task number. */
    public int parseTaskNumber(String details, String format) throws CaesarException {
        if (details == null || details.trim().isEmpty()) {
            throw missingDetails(format);
        }

        try {
            return Integer.parseInt(details.trim());
        } catch (NumberFormatException exception) {
            throw new CaesarException("Please provide a valid task number.");
        }
    }

    /** Ensures a command has non-empty details and returns the trimmed value. */
    public String requireDetails(String details, String format) throws CaesarException {
        if (details == null || details.trim().isEmpty()) {
            throw missingDetails(format);
        }
        return details.trim();
    }

    /** Creates the standard invalid-command error. */
    public CaesarException unknownCommand() {
        return new CaesarException("I'm not quite sure I caught that command, but take your time. Let's try again. "
                + "Try these commands: " + COMMANDS);
    }

    /** Creates the standard error for a command that is missing required details. */
    private CaesarException missingDetails(String format) {
        return new CaesarException("I'd love to organize that for you, but I just need more details. Try enter in this format: " + format);
    }

    /** A parsed command containing its type and the text after the command keyword. */
    public static final class ParsedCommand {
        private final Caesar.CommandType type;
        private final String details;

        private ParsedCommand(Caesar.CommandType type, String details) {
            this.type = type;
            this.details = details;
        }

        /** Returns the command keyword classification. */
        public Caesar.CommandType getType() {
            return type;
        }

        /** Returns the optional text following the command keyword. */
        public String getDetails() {
            return details;
        }
    }
}

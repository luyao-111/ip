package Caesar.task;

/**
 * A task with a start and end time.
 */
public class Event extends Task {
    private final String start;
    private final String end;

    /** Creates an event task with its description, start, and end text. */
    public Event(String description, String start, String end) {
        super(description);
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the event start text used when saving the task.
     */
    public String getStart() {
        return start;
    }

    /**
     * Returns the event end text used when saving the task.
     */
    public String getEnd() {
        return end;
    }

    /** Returns the event in the task-list display format. */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + start + " to: " + end + ")";
    }
}

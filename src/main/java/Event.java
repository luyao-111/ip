/**
 * A task with a start and end time.
 */
public class Event extends Task {
    private final String start;
    private final String end;

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

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + start + " to: " + end + ")";
    }
}

public class Task {
    private final String description;
    private boolean done;

    public Task(String description) {
        this.description = description;
    }

    public void markAsDone() throws CaesarException {
        if (done) {
            throw new CaesarException("You have marked this task!");
        }
        done = true;
    }

    public void markAsNotDone() throws CaesarException {
        if (!done) {
            throw new CaesarException("You have unmarked this task!");
        }
        done = false;
    }

    @Override
    public String toString() {
        return (done ? "[X] " : "[ ] ") + description;
    }
}

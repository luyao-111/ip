package caesar.task;

import caesar.exception.CaesarException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskListTest {

    @Test
    public void testNewListIsEmpty() {
        TaskList tasks = new TaskList();

        assertTrue(tasks.isEmpty());
        assertEquals(0, tasks.size());
        assertTrue(tasks.areAllDone());
    }

    @Test
    public void testAddGetDeleteAndInsertUseOneBasedPositions() throws CaesarException {
        TaskList tasks = new TaskList();
        ToDo first = new ToDo("First");
        ToDo second = new ToDo("Second");
        ToDo inserted = new ToDo("Inserted");

        tasks.add(first);
        tasks.add(second);

        assertSame(first, tasks.get(1));
        assertSame(second, tasks.delete(2));

        tasks.insert(1, inserted);

        assertSame(inserted, tasks.get(1));
        assertSame(first, tasks.get(2));
        assertEquals(2, tasks.size());
    }

    @Test
    public void testInvalidTaskNumbersAreRejected() throws CaesarException {
        TaskList tasks = new TaskList();
        tasks.add(new ToDo("Task"));

        assertThrows(CaesarException.class, () -> tasks.get(0));
        assertThrows(CaesarException.class, () -> tasks.get(2));
        assertThrows(CaesarException.class, () -> tasks.delete(0));
        assertThrows(CaesarException.class, () -> tasks.insert(3, new ToDo("Task")));
    }

    @Test
    public void testConstructorCopiesInitialTasks() throws CaesarException {
        ArrayList<Task> initialTasks = new ArrayList<>();
        ToDo task = new ToDo("Original");
        initialTasks.add(task);

        TaskList tasks = new TaskList(initialTasks);
        initialTasks.clear();

        assertEquals(1, tasks.size());
        assertSame(task, tasks.get(1));
    }

    @Test
    public void testConstructorRejectsMoreThanMaximumTasks() {
        ArrayList<Task> initialTasks = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            initialTasks.add(new ToDo("Task " + i));
        }

        CaesarException exception = assertThrows(
                CaesarException.class,
                () -> new TaskList(initialTasks)
        );

        assertEquals("Task file contains more than 100 tasks.", exception.getMessage());
    }

    @Test
    public void testMarkAndUnmarkChangeTaskStatus() throws CaesarException {
        TaskList tasks = new TaskList();
        tasks.add(new ToDo("Task"));

        tasks.mark(1);
        assertTrue(tasks.get(1).isDone());
        assertTrue(tasks.areAllDone());

        tasks.unmark(1);
        assertFalse(tasks.get(1).isDone());
        assertFalse(tasks.areAllDone());
    }

    @Test
    public void testSortedByStatusDoesNotChangeOriginalOrder() throws CaesarException {
        TaskList tasks = new TaskList();
        ToDo completed = new ToDo("Completed");
        ToDo pending = new ToDo("Pending");
        completed.markAsDone();
        tasks.add(completed);
        tasks.add(pending);

        ArrayList<Task> sortedTasks = tasks.sortedByStatus();

        assertSame(pending, sortedTasks.get(0));
        assertSame(completed, sortedTasks.get(1));
        assertSame(completed, tasks.get(1));
        assertSame(pending, tasks.get(2));
    }
}

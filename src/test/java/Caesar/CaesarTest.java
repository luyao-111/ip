package caesar;

import caesar.gui.GuiUi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests Caesar command processing independently from the JavaFX window. */
public class CaesarTest {
    @TempDir
    private Path temporaryDirectory;

    /** Verifies that a command is executed and its response is sent to the GUI user interface. */
    @Test
    public void processCommandAddsTaskAndReturnsResponse() throws Exception {
        GuiUi guiUi = new GuiUi();
        Caesar caesar = createCaesar(guiUi);

        assertFalse(caesar.processCommand("todo Read a book"));

        assertTrue(guiUi.consumeResponse().contains("Read a book"));
    }

    /** Verifies that the list command returns the persisted task to the GUI user interface. */
    @Test
    public void processCommandListsTasks() throws Exception {
        GuiUi guiUi = new GuiUi();
        Caesar caesar = createCaesar(guiUi);
        caesar.processCommand("todo Read a book");
        guiUi.clearResponse();

        caesar.processCommand("list");

        assertTrue(guiUi.consumeResponse().contains("1. [T][ ] Read a book"));
    }

    /** Verifies that the bye command signals the JavaFX window to stop accepting input. */
    @Test
    public void processCommandRecognizesExit() throws Exception {
        GuiUi guiUi = new GuiUi();
        Caesar caesar = createCaesar(guiUi);

        assertTrue(caesar.processCommand("bye"));
        assertTrue(guiUi.consumeResponse().contains("Until next time"));
    }

    /** Creates a Caesar instance backed by an isolated temporary task file. */
    private Caesar createCaesar(GuiUi guiUi) {
        Path taskFile = temporaryDirectory.resolve("tasks.txt");
        return new Caesar(taskFile.toString(), guiUi);
    }
}

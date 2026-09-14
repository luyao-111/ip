package caesar;

import caesar.gui.GuiUi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    /** Verifies that the interactive loop executes commands through the shared command path. */
    @Test
    public void runProcessesCommandsAndStopsAtExit() {
        InputStream originalInput = System.in;
        System.setIn(new ByteArrayInputStream(
                "todo Read a book\nbye\n".getBytes(StandardCharsets.UTF_8)));

        try {
            GuiUi guiUi = new GuiUi();
            Caesar caesar = createCaesar(guiUi);
            caesar.run();

            String response = guiUi.consumeResponse();
            assertTrue(response.contains("Read a book"));
            assertTrue(response.contains("Until next time"));
        } finally {
            System.setIn(originalInput);
        }
    }

    /** Creates a Caesar instance backed by an isolated temporary task file. */
    private Caesar createCaesar(GuiUi guiUi) {
        Path taskFile = temporaryDirectory.resolve("tasks.txt");
        return new Caesar(taskFile.toString(), guiUi);
    }
}

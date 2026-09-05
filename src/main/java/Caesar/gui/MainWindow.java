package caesar.gui;

import caesar.Caesar;
import caesar.DialogBox;
import caesar.exception.CaesarException;
import caesar.parser.Parser;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/** Controls the main JavaFX window described by {@code MainWindow.fxml}. */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    /** Processes commands using the existing Caesar application logic. */
    private Caesar caesar;
    /** Captures command responses for display in the conversation. */
    private GuiUi guiUi;
    /** Displays the avatar for messages sent by the user. */
    private Image userImage;
    /** Displays the avatar for messages sent by Caesar. */
    private Image caesarImage;

    /** Initializes the injected controls after the FXML view has been loaded. */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        userImage = new Image(getClass().getResourceAsStream("/images/User.png"));
        caesarImage = new Image(getClass().getResourceAsStream("/images/Caesar.png"));
    }

    /** Injects the Caesar instance and response collector used by this window. */
    public void setCaesar(Caesar caesar, GuiUi guiUi) {
        this.caesar = caesar;
        this.guiUi = guiUi;
        addDialog("Hello! I'm Caesar.\n"
                + "You look even brighter than the last time we spoke.\n"
                + "How may I ease your day today?\n"
                + Parser.getCommandInstructions(), false);
    }

    /** Processes the command entered by the user and displays Caesar's response. */
    @FXML
    private void handleUserInput() {
        String command = userInput.getText().trim();
        if (command.isEmpty()) {
            return;
        }

        addDialog(command, true);
        try {
            guiUi.clearResponse();
            boolean shouldExit = caesar.processCommand(command);
            String response = guiUi.consumeResponse();
            if (!response.isBlank()) {
                addDialog(response, false);
            }
            if (shouldExit) {
                getScene().getWindow().hide();
            }
        } catch (CaesarException exception) {
            guiUi.clearResponse();
            addDialog(exception.getMessage(), false);
        }
        userInput.clear();
    }

    /** Adds a user or Caesar message to the conversation. */
    private void addDialog(String message, boolean fromUser) {
        DialogBox dialog = fromUser
                ? DialogBox.getUserDialog(message, userImage)
                : DialogBox.getCaesarDialog(message, caesarImage);
        dialog.getStyleClass().add(fromUser ? "user-dialog" : "caesar-dialog");
        dialog.setMaxWidth(370.0);
        VBox.setMargin(dialog, new Insets(2.0));
        dialogContainer.getChildren().add(dialog);
    }
}

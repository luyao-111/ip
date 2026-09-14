package caesar.gui;

import caesar.Caesar;
import caesar.DialogBox;
import caesar.exception.CaesarException;
import caesar.parser.Parser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    @FXML
    private Button helpButton;

    /** Processes commands using the existing Caesar application logic. */
    private Caesar caesar;
    /** Captures command responses for display in the conversation. */
    private GuiUi guiUi;
    /** Displays the avatar for messages sent by the user. */
    private Image userImage;
    /** Displays the avatar for messages sent by Caesar. */
    private Image caesarImage;
    /** Stores the help window so repeated clicks focus the same window. */
    private Stage helpStage;

    /** Initializes the injected controls after the FXML view has been loaded. */
    @FXML
    public void initialize() {
        dialogContainer.heightProperty().addListener((observable, oldHeight, newHeight) -> {
            scrollToLatestDialog();
        });
        userImage = new Image(getClass().getResourceAsStream("/images/User.png"));
        caesarImage = new Image(getClass().getResourceAsStream("/images/Caesar.png"));
    }

    /** Injects the Caesar instance and response collector used by this window. */
    public void setCaesar(Caesar caesar, GuiUi guiUi) {
        this.caesar = caesar;
        this.guiUi = guiUi;
        addDialog("Hello! I'm Caesar.\n"
                + "You look even brighter than the last time we spoke.\n"
                + "How may I ease your day today?\n\n"
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

    /** Opens a small window containing the supported command formats. */
    @FXML
    private void handleHelp() {
        if (helpStage != null) {
            helpStage.toFront();
            return;
        }

        VBox helpContent = new VBox(8.0);
        helpContent.setPadding(new Insets(16.0));
        helpContent.getStyleClass().add("help-content");

        Label title = new Label("Available commands");
        title.getStyleClass().add("help-title");
        helpContent.getChildren().add(title);

        for (String command : Parser.getHelpCommands()) {
            Label commandFormat = new Label("• " + command);
            commandFormat.setWrapText(true);
            commandFormat.getStyleClass().add("help-command");
            helpContent.getChildren().add(commandFormat);
        }

        helpStage = new Stage();
        helpStage.setTitle("Caesar Help");
        if (helpButton.getScene() != null) {
            helpStage.initOwner(helpButton.getScene().getWindow());
        }
        helpStage.initModality(Modality.WINDOW_MODAL);
        helpStage.setResizable(false);
        ScrollPane helpScrollPane = new ScrollPane(helpContent);
        helpScrollPane.setFitToWidth(true);
        helpScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        helpScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        helpScrollPane.getStyleClass().add("help-scroll");

        helpStage.setScene(new Scene(helpScrollPane, 400.0, 460.0));
        helpStage.getScene().getStylesheets().add(
                getClass().getResource("/view/caesar.css").toExternalForm());
        helpStage.setOnHidden(event -> helpStage = null);
        helpStage.show();
    }

    /** Adds a user or Caesar message to the conversation. */
    private void addDialog(String message, boolean fromUser) {
        DialogBox dialog = fromUser
                ? DialogBox.getUserDialog(message, userImage)
                : DialogBox.getCaesarDialog(message, caesarImage);
        dialog.getStyleClass().add(fromUser ? "user-dialog" : "caesar-dialog");
        dialog.setMaxWidth(370.0);
        VBox.setMargin(dialog, new Insets(3.0, 2.0, 3.0, 2.0));
        dialogContainer.getChildren().add(dialog);
        scrollToLatestDialog();
    }

    /** Scrolls the conversation to its newest message after JavaFX lays it out. */
    private void scrollToLatestDialog() {
        Platform.runLater(() -> {
            dialogContainer.applyCss();
            dialogContainer.layout();
            scrollPane.layout();
            scrollPane.setVvalue(scrollPane.getVmax());
        });
    }
}

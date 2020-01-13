import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This defines the functionality for the game Hangman.
 *
 * @author Pratik Gurung
 */
public class Hangman extends Application {

    private boolean showGame = false;
    private boolean showFooter = false;

    private boolean disableStartPlaying = false;

    private boolean gameInProgress = false;

    private StackPane[] lettersOfWordToGuess;
    private StackPane[] alphabet;

    private boolean[] guessedLetters;

    private static int remainingGuesses = 10;
    private int correctlyGuessed = 0;

    private String wordToGuess;

    private Boolean newGame;

    /**
     * Generates the screen
     *
     * @param primaryStage The screen on which the game is being displayed
     *
     * @throws Exception If there is a problem with the game
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        if(newGame == null) {
            newGame = true;
        }

        primaryStage.setTitle("Hangman");

        BorderPane borderPane = new BorderPane();
        borderPane.setBackground(new Background(new BackgroundFill(Color.grayRgb(210), CornerRadii.EMPTY, Insets.EMPTY)));

        Group hangmanImage = createHangmanImage();
        borderPane.getChildren().add(hangmanImage);

        VBox screen = new VBox();

        HBox toolbar = createToolbar(primaryStage);
        VBox game = new VBox();
        BorderPane gamePlay = new BorderPane();
        HBox title = createTitle();

        VBox rightSide = createRightSide();
        gamePlay.setRight(rightSide);
        game.getChildren().addAll(title, gamePlay);
        game.setVisible(showGame);

        screen.getChildren().addAll(toolbar, game);
        borderPane.setTop(screen);

        HBox footer = createFooter(primaryStage);
        borderPane.setBottom(footer);

        Scene scene = new Scene(borderPane, 1000, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the menu options at the top of the screen
     *
     * @param primaryStage The screen on which the game is being displayed
     *
     * @return The menu options
     */
    public HBox createToolbar(Stage primaryStage) {
        Button newGame = createToolbarButton("New");
        newGame.setOnMouseClicked(e -> createNewGame(primaryStage));

        Button load = createToolbarButton("Load");
        load.setOnMouseClicked(e -> load(primaryStage));

        Button save = createToolbarButton("Save");
        save.setOnMouseClicked(e -> saveAndReturn(primaryStage));
        //Save initially disabled because no game is in progress
        save.setDisable(true);

        Button exit = createToolbarButton("Exit");
        exit.setOnMouseClicked(e -> exit(primaryStage));

        HBox toolbar = new HBox();
        toolbar.getChildren().addAll(newGame, load, save, exit);
        toolbar.setBackground(new Background(new BackgroundFill(Color.grayRgb(50), CornerRadii.EMPTY, Insets.EMPTY)));
        toolbar.setPadding(new Insets(10, 0, 10, 5));

        return toolbar;
    }

    /**
     * Creates an individual menu button
     *
     * @param name The text displayed on the menu button
     *
     * @return The menu button
     */
    public Button createToolbarButton(String name) {
        Button button = new Button(name);
        File file = new File("src/main/resources/icons/" + name + ".png");
        Image icon = new Image(file.toURI().toString());
        button.setGraphic(new ImageView(icon));
        button.setBackground(new Background(new BackgroundFill(Color.grayRgb(50), CornerRadii.EMPTY, Insets.EMPTY)));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-border-color: grey");
        return button;
    }

    /**
     * Starts a new game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void createNewGame(Stage primaryStage) {
        if(gameInProgress) {
            loadNewGamePopup(primaryStage);
        } else {
            try {
                //Selecting a random word from words.txt
                BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/words/words.txt"));

                List<String> words = new ArrayList<String>();
                String word = reader.readLine();

                while (word != null) {
                    words.add(word);
                    word = reader.readLine();
                }

                int randomIndex = (int) (Math.random() * (double) (words.size()));
                wordToGuess = words.get(randomIndex).toLowerCase();

                //Initializing class variables
                showGame = true;
                showFooter = true;
                disableStartPlaying = false;
                gameInProgress = false;
                newGame = true;

                remainingGuesses = 10;
                correctlyGuessed = 0;

                HBox footer = (HBox) primaryStage.getScene().getRoot().getChildrenUnmodifiable().get(primaryStage.getScene().getRoot().getChildrenUnmodifiable().size() - 1);
                footer.setVisible(true);
                footer.getChildren().get(0).setDisable(false);

                start(primaryStage);
            } catch (URISyntaxException e) {
                System.out.println("Could not find \"words.txt\"");
            } catch(IOException e) {
                System.out.println("Could not read \"words.txt\"");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads a saved game to resume progress
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void load(Stage primaryStage) {
        if(gameInProgress) {
            loadLoadGamePopup(primaryStage);
        } else {
            //Allows the player to choose a saved game
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Saved Game");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Hangman Files", "*.hng"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(selectedFile));

                    //Generating the game from data saved on selected file
                    wordToGuess = reader.readLine();
                    remainingGuesses = Integer.valueOf(reader.readLine());
                    correctlyGuessed = Integer.valueOf(reader.readLine());

                    String guessedLettersString = reader.readLine();
                    guessedLettersString = guessedLettersString.substring(1, guessedLettersString.length() - 1);
                    String[] guessedLettersStringArray = guessedLettersString.split(",");

                    guessedLetters = new boolean[26];

                    for (int i = 0; i < guessedLettersStringArray.length; i++) {
                        guessedLetters[i] = Boolean.valueOf(guessedLettersStringArray[i].trim().toLowerCase());
                    }

                    showGame = true;
                    showFooter = true;
                    disableStartPlaying = false;
                    newGame = false;

                    HBox footer = (HBox) primaryStage.getScene().getRoot().getChildrenUnmodifiable().get(primaryStage.getScene().getRoot().getChildrenUnmodifiable().size() - 1);
                    footer.setVisible(true);
                    footer.getChildren().get(0).setDisable(false);

                    start(primaryStage);
                } catch (FileNotFoundException e) {
                    System.out.println("Could not find the selected file");
                } catch (IOException e) {
                    System.out.println("Could not read the selected file");
                } catch (Exception e) {
                    System.out.println("Could not load the saved game");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Saves the current game and exits the screen
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void saveAndExit(Stage primaryStage) {
        saveCurrentGame(primaryStage);
        primaryStage.close();
    }

    /**
     * Saves the current game and starts a new game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void saveAndCreateNewGame(Stage primaryStage) {
        saveCurrentGame(primaryStage);
        gameInProgress = false;
        createNewGame(primaryStage);
    }

    /**
     * Saves the current game and loads a previously saved game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void saveAndLoadGame(Stage primaryStage) {
        saveCurrentGame(primaryStage);
        gameInProgress = false;
        load(primaryStage);
    }

    /**
     * Saves the current game and returns to the current game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void saveAndReturn(Stage primaryStage) {
        saveCurrentGame(primaryStage);
        gameInProgress = false;
        disableStartPlaying = false;
        newGame = false;

        try {
            start(primaryStage);
        } catch (Exception e) {
            System.out.println("Could not return to game stage");
        }
    }

    /**
     * Saves the current game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void saveCurrentGame(Stage primaryStage) {
        //Generating data to be saved about the progress of the current game in the form of text
        String guessedLettersString = "[";

        for(int i = 0; i < guessedLetters.length; i++) {
            guessedLettersString += guessedLetters[i];
            if(i != guessedLetters.length - 1) {
                guessedLettersString += ", ";
            } else {
                guessedLettersString += "]";
            }
        }

        String hangmanFile = wordToGuess + "\n"
                + remainingGuesses + "\n"
                + correctlyGuessed + "\n"
                + guessedLettersString;

        //Allowing the user to choose where to save the current game data
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Hangman Game");
        File selectedFile = fileChooser.showSaveDialog(primaryStage);

        if (selectedFile != null) {
            String path = selectedFile.getAbsolutePath();
            int count = path.length() - path.replace(".", "").length();

            //Making sure that the saved game is followed by the .hng extension
            if(!path.endsWith(".hng") || count > 1) {
                Stage stage = new Stage();
                VBox screen = new VBox();
                Text text = new Text("Please save the game with a .hng extension only");

                Button close = new Button("CLOSE");
                close.setOnMouseClicked(e -> {
                    stage.close();
                    saveCurrentGame(primaryStage);
                });

                screen.getChildren().addAll(text, close);
                screen.setAlignment(Pos.CENTER);
                screen.setSpacing(15);
                Scene scene = new Scene(screen, 350, 200);
                stage.setScene(scene);

                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } else {
                try {
                    PrintWriter writer;
                    writer = new PrintWriter(selectedFile);
                    writer.println(hangmanFile);
                    writer.close();
                } catch (FileNotFoundException e) {
                    System.out.println("Could not save the Hangman file");
                } catch (Exception e) {
                    System.out.println("Could not load the game stage after saving");
                }
            }
        }
    }

    /**
     * Exits the current game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void exit(Stage primaryStage) {
        if(gameInProgress) {
            loadExitPopup(primaryStage);
        } else {
            primaryStage.close();
        }
    }

    /**
     * Generates popup to allow user to exit the game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void loadExitPopup(Stage primaryStage) {
        createThreeButtonPopup(primaryStage, 1);
    }

    /**
     * Generates popup to allow user to generate a new game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void loadNewGamePopup(Stage primaryStage) {
        createThreeButtonPopup(primaryStage, 2);
    }

    /**
     * Generates popup to allow user to resume a saved game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void loadLoadGamePopup(Stage primaryStage) {
        createThreeButtonPopup(primaryStage, 3);
    }

    /**
     * Generates a popup with three buttons
     *
     * @param primaryStage The screen on which the game is being displayed
     * @param code Specifies the functionality of the Yes and No buttons in the popup
     */
    public void createThreeButtonPopup(Stage primaryStage, int code) {
        Stage stage = new Stage();
        VBox screen = new VBox();
        Text text = new Text("Would you like to save the current game?");

        HBox buttons = createThreeButtonPopupButtons(primaryStage, stage, code);

        screen.getChildren().addAll(text, buttons);
        screen.setAlignment(Pos.CENTER);
        screen.setSpacing(15);
        Scene scene = new Scene(screen, 350, 200);

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    /**
     * Generates the three buttons in the three button popup
     *
     * @param primaryStage The screen on which the game is being displayed
     * @param stage The popup
     * @param code Specifies the functionality of the Yes and No buttons in the popup
     *
     * @return The three buttons
     */
    public HBox createThreeButtonPopupButtons(Stage primaryStage, Stage stage, int code) {
        HBox buttons = new HBox();

        Button yes = createYesButton(primaryStage, stage, code);
        Button no = createNoButton(primaryStage, stage, code);
        Button cancel = new Button("Cancel");

        cancel.setOnMouseClicked(e -> stage.close());

        buttons.getChildren().addAll(yes, no, cancel);
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(5);

        return buttons;
    }

    /**
     * Sets the functionality of the Yes button in the three button popup
     *
     * @param primaryStage The screen on which the game is being displayed
     * @param stage The popup
     * @param code Specifies the functionality of the Yes and No buttons in the popup
     *
     * @return The Yes button in the three button popup
     */
    public Button createYesButton(Stage primaryStage, Stage stage, int code) {
        Button yes = new Button("Yes");

        yes.setOnMouseClicked(e ->  {
            stage.close();

            switch(code) {
                case 1:
                    saveAndExit(primaryStage);
                    break;
                case 2:
                    saveAndCreateNewGame(primaryStage);
                    break;
                case 3:
                    saveAndLoadGame(primaryStage);
                    break;
            }
        });

        return yes;
    }

    /**
     * Sets the functionality of the No button in the three button popup
     *
     * @param primaryStage The screen on which the game is being displayed
     * @param stage The popup
     * @param code Specifies the functionality of the Yes and No buttons in the popup
     *
     * @return The No button in the three button popup
     */
    public Button createNoButton(Stage primaryStage, Stage stage, int code) {
        Button no = new Button("No");

        no.setOnMouseClicked(e -> {
            stage.close();

            switch(code) {
                case 1:
                    primaryStage.close();
                    break;
                case 2:
                    gameInProgress = false;
                    createNewGame(primaryStage);
                    break;
                case 3:
                    gameInProgress = false;
                    load(primaryStage);
                    break;
            }
        });

        return no;
    }

    /**
     * Generates the image of the hanging man
     *
     * @return The image of the hanging man
     */
    public Group createHangmanImage() {
        Group hangmanImage = new Group();

        Line base = new Line(40, 370, 290,370);
        base.setStrokeWidth(5);

        Line verticalLine = new Line(40, 370, 40, 130);
        verticalLine.setStrokeWidth(5);

        Line horizontalLine = new Line(40, 130, 210, 130);
        horizontalLine.setStrokeWidth(5);

        Line hangRope = new Line(210, 130, 210, 165);
        hangRope.setStrokeWidth(5);

        Circle head = new Circle(210, 190, 25);
        head.setFill(Color.TRANSPARENT);
        head.setStroke(Color.BLACK);
        head.setStrokeWidth(5);

        Line body = new Line(210, 215, 210, 285);
        body.setStrokeWidth(5);

        Line leftLeg = new Line(210, 285, 185, 310);
        leftLeg.setStrokeWidth(5);

        Line rightLeg = new Line(210, 285, 235, 310);
        rightLeg.setStrokeWidth(5);

        Line leftArm = new Line(210, 235, 190, 265);
        leftArm.setStrokeWidth(5);

        Line rightArm = new Line(210, 235, 230, 265);
        rightArm.setStrokeWidth(5);

        ObservableList<Node> children = hangmanImage.getChildren();

        children.addAll(base, verticalLine, horizontalLine, hangRope,
                head, body, leftLeg, rightLeg, leftArm, rightArm);

        for(int i = 10 - 1; i >= 10 - remainingGuesses; i--) {
            children.get(i).setVisible(false);
        }

        return hangmanImage;
    }

    /**
     * Generates the title at the top of the game screen
     *
     * @return The title at the top of the game screen
     */
    public HBox createTitle() {
        HBox title = new HBox();
        Text titleText = new Text("Hangman");
        titleText.setFont(Font.font(35));
        titleText.setFill(Color.WHITE);
        title.getChildren().add(titleText);
        title.setAlignment(Pos.CENTER);
        title.setPadding(new Insets(5, 0, 15, 0));
        return title;
    }

    /**
     * Generates the right side of the board
     *
     * @return The right side of the board
     */
    public VBox createRightSide() {
        VBox rightSide = new VBox();
        Text remainingGuessesText = new Text("Remaining Guesses: " + remainingGuesses);
        HBox wordBoxes = createWordBoxes(newGame);
        VBox letters = createLetters();
        rightSide.getChildren().addAll(remainingGuessesText, wordBoxes, letters);
        rightSide.setPadding(new Insets(0, 20, 0, 0));
        return rightSide;
    }

    /**
     * Generates the boxes for the letters of the word to be guessed
     *
     * @param newGame Boolean value specifying whether a new game is being generated or a saved game is being resumed
     *
     * @return The boxes for the letters of the word to be guessed
     */
    public HBox createWordBoxes(boolean newGame) {
        HBox wordBoxes = new HBox();

        if(showGame) {
            if(newGame) {
                guessedLetters = new boolean[26];
                Arrays.fill(guessedLetters, false);
            }

            lettersOfWordToGuess = new StackPane[wordToGuess.length()];

            for (int i = 0; i < wordToGuess.length(); i++) {
                StackPane stackPane = new StackPane();

                Rectangle rec = new Rectangle(20, 20);
                rec.setFill(Color.BLACK);

                Text letter = new Text(("" + wordToGuess.charAt(i)).toUpperCase());
                letter.setFill(Color.WHITE);

                int index = (int) wordToGuess.charAt(i);

                if(!guessedLetters[((int) wordToGuess.charAt(i)) - 97]) {
                    letter.setVisible(false);
                }

                stackPane.getChildren().addAll(rec, letter);
                stackPane.setPadding(new Insets(0, 1, 0, 1));

                lettersOfWordToGuess[i] = stackPane;
                wordBoxes.getChildren().add(stackPane);
            }
        }

        wordBoxes.setPadding(new Insets(20, 0 ,20, 0));

        return wordBoxes;
    }

    /**
     * Generates the whole English alphabet for the player to see
     *
     * @return The alphabet
     */
    public VBox createLetters() {
        VBox letters = new VBox();
        alphabet = new StackPane[26];

        if(showGame) {
            letters.getChildren().addAll(createAlphabet(0), createAlphabet(7),
                    createAlphabet(14), createAlphabet(21));
        }

        return letters;
    }

    /**
     * Generates a row  of letters in the alphabet
     *
     * @param start Specifies which letter in the alphabet to start the row from
     *
     * @return A row of letters from the alphabet
     */
    public HBox createAlphabet(int start) {
        HBox row = new HBox();

        int end = (start == 21) ? 26 : (start + 7);

        for(int i = start; i < end; i++) {
            StackPane stackPane = new StackPane();

            Rectangle rec = new Rectangle(41, 41);

            //The game could be resumed from a saved state so some letters could have been guessed already
            if(guessedLetters[i])
                rec.setFill(Color.DARKOLIVEGREEN);
            else
                rec.setFill(Color.GREEN);

            Text letter = new Text("" + (char) (i + 65));
            letter.setFill(Color.WHITE);

            stackPane.getChildren().addAll(rec, letter);
            stackPane.setStyle("-fx-border-color: white");

            alphabet[i] = stackPane;
            row.getChildren().add(stackPane);
        }

        return row;
    }

    /**
     * Generates the footer of the page
     *
     * @param primaryStage The screen on which the game is being displayed
     *
     * @return The footer of the page
     */
    public HBox createFooter(Stage primaryStage) {
        HBox footer = new HBox();
        footer.setBackground(new Background(new BackgroundFill(Color.grayRgb(240), CornerRadii.EMPTY, Insets.EMPTY)));
        Button startPlaying = new Button();
        startPlaying.setText("Start Playing");
        startPlaying.setOnMouseClicked(e -> startGame(primaryStage));
        startPlaying.setDisable(disableStartPlaying);
        footer.getChildren().add(startPlaying);
        footer.setPadding(new Insets(5, 0, 5, 0));
        footer.setAlignment(Pos.CENTER);
        footer.setVisible(showFooter);
        return footer;
    }

    /**
     * Starts a game
     *
     * @param primaryStage The screen on which the game is being displayed
     */
    public void startGame(Stage primaryStage) {
        showGame = true;

        Scene scene = primaryStage.getScene();
        scene.setOnKeyPressed(e -> handleKeyPress(e, primaryStage));

        ObservableList<Node> nodes = primaryStage.getScene().getRoot().getChildrenUnmodifiable();
        nodes.get(nodes.size() - 1).setDisable(true);
    }

    /**
     * Handles when a key is pressed by the player
     *
     * @param e The event
     * @param primaryStage The screen on which the game is being displayed
     */
    public void handleKeyPress(KeyEvent e, Stage primaryStage) {
        KeyCode code = e.getCode();
        int index = -1;

        if(code.isLetterKey()) {
            char typedLetter = e.getCode().getName().toLowerCase().charAt(0);

            for (int i = 0; i < alphabet.length; i++) {
                char letter = (char) (i + 97);

                if(typedLetter == letter) {
                    index = i;
                    break;
                }
            }

            if(!guessedLetters[index]) {
                //Indicating to the player that the letter has been guessed
                guessedLetters[index] = true;
                ((Rectangle) (alphabet[index].getChildren().get(0))).setFill(Color.DARKOLIVEGREEN);

                String letter = ((Text) (alphabet[index].getChildren().get(1))).getText().toLowerCase();
                boolean containsLetter = wordToGuess.contains("" + typedLetter);

                ObservableList<Node> screenElements = primaryStage.getScene().getRoot().getChildrenUnmodifiable();

                if(!gameInProgress) {
                    ObservableList<Node> buttons = ((HBox) ((VBox) screenElements.get(1)).getChildren().get(0)).getChildren();
                    buttons.get(2).setDisable(false);
                    buttons.get(3).setDisable(false);
                    gameInProgress = true;
                }

                if(containsLetter) {
                    for(int i = 0; i < wordToGuess.length(); i++) {
                        if(wordToGuess.charAt(i) == letter.charAt(0)) {
                            lettersOfWordToGuess[i].getChildren().get(1).setVisible(true);
                            correctlyGuessed++;
                        }
                    }

                    if(correctlyGuessed == wordToGuess.length()) {
                        createSingleButtonPopup(primaryStage, true);
                    }
                } else {
                    ((Group) screenElements.get(0)).getChildren().get(10 - remainingGuesses).setVisible(true);

                    remainingGuesses--;

                    ((Text) ((VBox) (((BorderPane) ((VBox) ((VBox) screenElements.get(1)).getChildren().get(1)).
                            getChildren().get(1)).getRight())).getChildren().get(0)).
                            setText("Remaining Guesses: " + remainingGuesses);

                    if(remainingGuesses == 0) {
                        createSingleButtonPopup(primaryStage, false);
                    }
                }
            }
        }
    }

    /**
     * Generates a popup with a single button on it for when a game is finished
     *
     * @param primaryStage The screen on which the game is being displayed
     * @param won Specifies whether the player won or lost
     */
    public void createSingleButtonPopup(Stage primaryStage, boolean won) {
        ObservableList<Node> screenElements = primaryStage.getScene().getRoot().getChildrenUnmodifiable();

        ((HBox) ((VBox) screenElements.get(1)).getChildren().get(0)).getChildren().get(2).setDisable(true);

        gameInProgress = false;

        Stage stage = new Stage();
        VBox screen = new VBox();
        Text text = new Text();

        if(won) {
            text.setText("You won!");
        } else {
            text.setText("You lost (the word was \""  + wordToGuess + "\")");
            for(int i = 0; i < lettersOfWordToGuess.length; i++) {
                StackPane letter = lettersOfWordToGuess[i];
                if(!letter.getChildren().get(1).isVisible()) {
                    ((Rectangle) letter.getChildren().get(0)).setFill(Color.grayRgb(100));
                    letter.getChildren().get(1).setVisible(true);
                }
            }
        }

        Button close = new Button("CLOSE");
        close.setOnMouseClicked(e -> {
            stage.close();
            primaryStage.getScene().setOnKeyPressed(event -> {});
        });

        screen.getChildren().addAll(text, close);
        screen.setAlignment(Pos.CENTER);
        screen.setSpacing(15);
        Scene scene = new Scene(screen, 350, 200);
        stage.setScene(scene);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    /**
     * Starts the program
     *
     * @param args Command Line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

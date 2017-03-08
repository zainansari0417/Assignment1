package sample;

/*
Group Members: Ammar Khan 100602925, Muhammmad Ansari (100586120)
Date: March 7th 2017
 */

//import packages
import java.io.*;
import java.util.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.text.DecimalFormat;

//main class
public class Main extends Application {

    //initializing variables
    static ArrayList<String> words = new ArrayList<>();
    static List<TestFile> results = new ArrayList<>();
    double accuracy = 0;
    double precision = 0;
    int correctGuesses = 0;
    int incorrectGuesses = 0;
    int correctSpamGuesses = 0;

    //creating hashmaps for frequency and probability
    static Map<String, Integer> trainHamFreq = new HashMap<>();
    static Map<String, Integer> trainSpamFreq = new HashMap<>();
    static Map<String, Float> hamProb = new HashMap<>();
    static Map<String, Float> spamWordProb = new HashMap<>();
    static Map<String, Float> probability = new HashMap<>();

    //GUI
    public void start(Stage primaryStage) throws Exception{

        //Prompting user to  select the directory where messages are stored
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        File mainDirectory = directoryChooser.showDialog(primaryStage);

        Group root = new Group();

        //creating stage
        primaryStage.setTitle("Spam Master 9000");
        primaryStage.setScene(new Scene(root, 800, 600));

        checkForSpam(mainDirectory);

        //formatting accuracy and precision values to be in decimal form
        DecimalFormat df = new DecimalFormat("0.000 00");
        String accuracyString, precisionString;
        accuracyString = df.format(accuracy);
        precisionString = df.format(precision);

        BorderPane layout = new BorderPane();
        root.getChildren().add(layout);

        //creating table
        TableView table = new TableView();
        layout.setCenter(table);
        table.setMinHeight(500);
        table.setItems(EmailArray.getAllFiles(results));

        //creating columns
        TableColumn<TestFile, String> filenameColumn = new TableColumn("File");
        TableColumn<TestFile, String> classColumn = new TableColumn("Actual Class");
        TableColumn<TestFile, Double> probColumn = new TableColumn("Spam Probability");

        filenameColumn.setMinWidth(350);
        classColumn.setMinWidth(100);
        probColumn.setMinWidth(350);

        filenameColumn.setCellValueFactory(new PropertyValueFactory<TestFile, String>("filename"));
        classColumn.setCellValueFactory(new PropertyValueFactory<TestFile, String>("actualClass"));
        probColumn.setCellValueFactory(new PropertyValueFactory<TestFile, Double>("spamProbability"));

        table.getColumns().addAll(filenameColumn, classColumn, probColumn);

        //adding separate pane for precision and accuracy
        GridPane bottom = new GridPane();
        bottom.setPadding(new Insets(20,10,10,10));
        bottom.setVgap(10); bottom.setHgap(10);
        layout.setBottom(bottom);

        Label accuracyLabel = new Label("Accuracy: ");
        bottom.add(accuracyLabel, 0,0);
        TextField accuracyField = new TextField(String.valueOf(accuracyString));
        bottom.add(accuracyField, 1, 0);

        Label precisionLabel = new Label("Precision: ");
        bottom.add(precisionLabel, 0, 1);
        TextField precisionField = new TextField(String.valueOf(precisionString));
        bottom.add(precisionField, 1, 1);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    //checking for spam
    public void checkForSpam(File mainDirectory) {
        File spamFolder = new File(mainDirectory + "/train/spam");
        File hamFolder = new File(mainDirectory +"/train/ham");
        File[] spamFiles = spamFolder.listFiles();
        File[] hamFiles = hamFolder.listFiles();

        //spam training
        for(int i=0; i<spamFiles.length; i++) {
            try {
                Scanner scanner = new Scanner(spamFiles[i]);
                while(scanner.hasNext()) {
                    String nextWord = scanner.next();
                    addToMap(nextWord, trainSpamFreq);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //ham training
        for(int i=0; i<hamFiles.length; i++) {
            try {
                Scanner scanner = new Scanner(hamFiles[i]);
                while(scanner.hasNext()) {
                    String nextWord = scanner.next();
                    addToMap(nextWord, trainHamFreq);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        Iterator it;
        int wordFreq;

        //spam probability
        it = trainSpamFreq.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            wordFreq = (Integer)pair.getValue();
            float prob = (float)wordFreq/spamFiles.length;
            spamWordProb.put((String)pair.getKey(), prob);
        }

        //ham probability
        it = trainHamFreq.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            wordFreq = (Integer)pair.getValue();
            float prob = (float)wordFreq/hamFiles.length;
            hamProb.put((String)pair.getKey(), prob);
        }

        //probability
        for(int i=0; i<words.size(); i++) {
            if(hamProb.containsKey(words.get(i)) &&
                    spamWordProb.containsKey(words.get(i))) {
                float prob = spamWordProb.get(words.get(i))/
                        (spamWordProb.get(words.get(i))+
                                hamProb.get(words.get(i)));
                probability.put(words.get(i), prob);
            }
        }

        spamDetect(mainDirectory);
    }

    public void spamDetect(File mainDirectory) {
        File spamFolder = new File(mainDirectory + "/test/spam");
        File hamFolder = new File(mainDirectory + "/test/ham");
        File[] spamFiles = spamFolder.listFiles();
        File[] hamFiles = hamFolder.listFiles();

        //ham
        for(int i=0; i<hamFiles.length; i++) {
            try {
                double n = 0;
                Scanner scanner = new Scanner(hamFiles[i]);
                while(scanner.hasNext()) {
                    String nextWord = scanner.next();
                    n += addToEta(nextWord);
                }

                //probability
                double spamProb = 1/(1+Math.exp(n));

                //correct or incorrect
                if(spamProb < 0.5) {
                    correctGuesses++;
                } else if(spamProb > 0.5) {
                    incorrectGuesses++;
                }

                TestFile nextFile;
                nextFile = new TestFile(hamFiles[i].getName(), spamProb, "Ham");
                results.add(nextFile);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //spam
        for(int i=0; i<spamFiles.length; i++) {
            try {
                double n = 0;
                Scanner scanner = new Scanner(spamFiles[i]);
                while(scanner.hasNext()) {
                    String nextWord = scanner.next();
                    n += addToEta(nextWord);
                }

                //probability
                double spamProb = 1/(1+Math.exp(n));

                //correct or incorrect
                if(spamProb > 0.5) {
                    correctGuesses++;
                    correctSpamGuesses++;
                }
                incorrectGuesses++;

                TestFile nextFile;
                nextFile = new TestFile(hamFiles[i].getName(), spamProb, "Spam");
                results.add(nextFile);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //calculating final values
        precision = (double)correctSpamGuesses / (double)incorrectGuesses;
        accuracy = (double)correctGuesses / ((double)hamFiles.length + (double)spamFiles.length);
    }

    public static void addToMap(String word, Map<String, Integer> map) {
        if(map.containsKey(word)) {
            int num = map.get(word);
            map.replace(word, num+1);
        } else {
            map.put(word, 1);
            words.add(word);
        }
    }

    public static double addToEta(String word) {
        if(probability.containsKey(word)) {
            double ln = Math.log(1-probability.get(word)) - Math.log(probability.get(word));
            return ln;
        } else {
            return 0;
        }
    }


}

package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class EmailArray {
    //code to get all files and add them to array through for loop
    public static ObservableList<TestFile> getAllFiles(List<TestFile> fileList) {
        ObservableList<TestFile> files =
                FXCollections.observableArrayList();

        for(int i=0; i<fileList.size(); i++) {
            files.add(fileList.get(i));
        }
    //return array of files to code to search through
        return files;
    }
}

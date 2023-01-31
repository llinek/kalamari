package cz.lukaslinek.bakalari;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {
    static File filesDir = null;
    public static void editFile(String filename) throws IOException {
        if (!new File(filesDir, filename).exists()) {
            File filesArray = new File(filesDir, Constants.FILESARRAYFILENAME);
            if (!filesArray.exists()) {
                filesArray.createNewFile();
            }
            FileWriter writer = new FileWriter(filesArray);
            writer.append(filename + "\n");
            writer.close();
            new File(filesDir, filename).createNewFile();
        }
    }
    public static boolean exists(String filename) {
        File filesArray = new File(filesDir, Constants.FILESARRAYFILENAME);
        if (filesArray.exists()) {
            try {
                BufferedReader input = new BufferedReader(new FileReader(filesArray));
                while (input.ready()) {
                    if (input.readLine().contains(filename)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}

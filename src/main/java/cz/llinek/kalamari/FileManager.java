package cz.llinek.kalamari;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {
    static File filesDir = null;

    public static File editFile(String filename) throws IOException {
        if (!new File(filesDir, filename).exists()) {
            File filesArray = new File(filesDir, Constants.FILES_ARRAY_FILENAME);
            if (!filesArray.exists()) {
                filesArray.createNewFile();
            }
            FileWriter writer = new FileWriter(filesArray);
            writer.append(filename + "\n");
            writer.close();
            new File(filesDir, filename).createNewFile();
        }
        return new File(filesDir, filename);
    }

    public static String readFile(String filename) {
        try {
            if (new File(filesDir, filename).exists()) {
                BufferedReader input = new BufferedReader(new FileReader(new File(filesDir, filename)));

                StringBuilder in = new StringBuilder();
                while (input.ready()) {
                    in.append(input.readLine());
                    in.append('\n');
                }
                return in.toString().substring(0, in.toString().length() - 1);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void fileWrite(String filename, String content) throws IOException {
        if (!new File(filesDir, filename).exists()) {
            File filesArray = new File(filesDir, Constants.FILES_ARRAY_FILENAME);
            if (!filesArray.exists()) {
                filesArray.createNewFile();
            }
            FileWriter writer = new FileWriter(filesArray);
            writer.append(filename + "\n");
            writer.close();
            new File(filesDir, filename).createNewFile();
        }
        new File(filesDir, filename).delete();
        new File(filesDir, filename).createNewFile();
        FileWriter output = new FileWriter(new File(filesDir, filename));
        output.write(content);
        output.close();
    }

    public static void fileAppend(String filename, String content) throws IOException {
        if (!new File(filesDir, filename).exists()) {
            File filesArray = new File(filesDir, Constants.FILES_ARRAY_FILENAME);
            if (!filesArray.exists()) {
                filesArray.createNewFile();
            }
            FileWriter writer = new FileWriter(filesArray);
            writer.append(filename + "\n");
            writer.close();
            new File(filesDir, filename).createNewFile();
        }
        FileWriter output = new FileWriter(new File(filesDir, filename));
        output.write(content);
        output.close();
    }

    public static void deleteFile(String filename) throws IOException {
        File filesArray = new File(filesDir, Constants.FILES_ARRAY_FILENAME);
        if (filesArray.exists()) {
            StringBuilder filesArrayContent = new StringBuilder();
            BufferedReader input = new BufferedReader(new FileReader(filesArray));
            while (true) {
                String temp = input.readLine();
                if (temp == null) {
                    break;
                }
                if (!temp.equals(filename)) {
                    filesArrayContent.append(temp);
                    filesArrayContent.append('\n');
                } else {
                    new File(filesDir, filename).delete();
                }
            }
            filesArray.delete();
            filesArray.createNewFile();
            FileWriter output = new FileWriter(filesArray);
            output.write(filesArrayContent.toString());
        }
    }

    public static boolean exists(String filename) {
        File filesArray = new File(filesDir, Constants.FILES_ARRAY_FILENAME);
        if (filesArray.exists()) {
            try {
                BufferedReader input = new BufferedReader(new FileReader(filesArray));
                while (true) {
                    String temp = input.readLine();
                    if (temp == null) {
                        break;
                    }
                    if (temp.contains(filename)) {
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

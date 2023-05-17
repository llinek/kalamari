package cz.llinek.kalamari;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {
    static File filesDir = null;

    private static void cleanDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                cleanDir(file);
            }
            file.delete();
        }
    }

    public static void deleteDirContents(String dir) {
        File file = new File(filesDir, dir);
        cleanDir(file);
    }

    public static File editFile(String filename) throws IOException {
        File file = new File(filesDir, filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static void deleteAllFiles() {
        cleanDir(filesDir);
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
                if (in.toString().length() > 1) {
                    return in.toString().substring(0, in.toString().length() - 1);
                }
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void mkDir(String dir) {
        new File(filesDir, dir).mkdir();
    }

    public static void fileWrite(String filename, String content) throws IOException {
        new File(filesDir, filename).delete();
        new File(filesDir, filename).createNewFile();
        FileWriter output = new FileWriter(new File(filesDir, filename));
        output.write(content);
        output.close();
    }

    public static void fileAppend(String filename, String content) throws IOException {
        if (!new File(filesDir, filename).exists()) {
            new File(filesDir, filename).createNewFile();
            FileWriter output = new FileWriter(new File(filesDir, filename));
            output.write(content);
            output.close();
            return;
        }
        String filePrefix = readFile(filename);
        FileWriter output = new FileWriter(new File(filesDir, filename));
        output.write(filePrefix + content);
        output.close();
    }

    public static void deleteFile(String filename) throws IOException {
        new File(filesDir, filename).delete();
    }

    public static boolean exists(String filename) {
        return new File(filesDir, filename).exists();
    }
}

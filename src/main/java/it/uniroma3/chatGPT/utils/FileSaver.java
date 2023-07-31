package it.uniroma3.chatGPT.utils;

import java.io.File;
import java.io.IOException;

public class FileSaver {
    public static void saveFile(String path, String filename, String content) throws IOException {
        File newFile = new File(path + "/" + filename);
        java.io.FileWriter myWriter = new java.io.FileWriter(newFile);
        myWriter.write(content);
        myWriter.close();
    }
}

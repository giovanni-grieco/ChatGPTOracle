package it.uniroma3.chatGPT.utils;

import java.io.File;

public class FileRetriever {
    public static File getFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("File not found at '" + path + "'");
        }
        return file;
    }
}

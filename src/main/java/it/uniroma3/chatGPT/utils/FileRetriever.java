package it.uniroma3.chatGPT.utils;

import java.io.File;
import java.io.IOException;

public class FileRetriever {
    public static File getFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File not found at '" + path + "'");
        }
        return file;
    }
}

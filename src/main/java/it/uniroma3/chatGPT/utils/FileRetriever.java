package it.uniroma3.chatGPT.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileRetriever {
    public static File getFile(String path) {
        return new File(path);
    }
}

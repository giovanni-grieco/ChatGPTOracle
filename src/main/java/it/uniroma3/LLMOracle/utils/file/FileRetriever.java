package it.uniroma3.LLMOracle.utils.file;

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

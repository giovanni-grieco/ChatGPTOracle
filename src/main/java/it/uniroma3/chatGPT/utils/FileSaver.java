package it.uniroma3.chatGPT.utils;

import java.io.File;
import java.io.IOException;

public class FileSaver {
    public static void saveFile(String path, String filename, String content) throws IOException {
        File directory = new File(path);
        if(!directory.exists()){
            boolean folderCreated = directory.mkdir();
            if(!folderCreated){
                System.err.println("Impossibile creare cartella");
            }else{
                System.out.println("Cartella creata "+path);
            }
        }
        File newFile = new File(path + "/" + filename);
        java.io.FileWriter myWriter = new java.io.FileWriter(newFile);
        myWriter.write(content);
        myWriter.close();
    }
}

package it.uniroma3.chatGPT.utils.file;

import java.io.File;
import java.io.IOException;

public class FileSaver {
    public static void saveFile(String path, String filename, String content){
        File directory = new File(path);
        if(!directory.exists()){
            boolean folderCreated = directory.mkdirs();
            if(!folderCreated){
                System.err.println("Impossibile creare cartella");
            }else{
                System.out.println("Cartella creata "+path);
            }
        }
        File newFile = new File(path + "/" + filename);
        try {
            java.io.FileWriter myWriter = new java.io.FileWriter(newFile);
            myWriter.write(content);
            //System.out.println("File creato "+path+"/"+filename);
            myWriter.close();
        }catch(IOException e){
            System.err.println("Impossibile salvare file");
            throw new RuntimeException(e);
        }
    }
}

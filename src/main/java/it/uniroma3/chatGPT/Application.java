package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.comando.ComandiFactory;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.extraction.AlaskaEntityExtractor;
import it.uniroma3.chatGPT.data.extraction.EntityExtractor;
import it.uniroma3.chatGPT.data.EntityType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;

public class Application {
    public static AppProperties appProperties = null;
    private final Set<Entity> entities;
    private int entityTypes;

    public Application(AppProperties appProperties) {
        Application.appProperties = appProperties;
        this.entities = new HashSet<>();
    }

    public void run() throws IOException {
        this.entityTypes = appProperties.getDatasetFolders().length;

        System.out.println("Extracting entities");
        for (EntityType type : EntityType.values()) {
            EntityExtractor extractor = new AlaskaEntityExtractor(type, Path.of(appProperties.getDatasetPath() + "/" + appProperties.getGroundTruthFileNames()[type.getTypeIndex()]));
            extractor.extractEntitiesFromGroundTruth(entities);
        }
        //Stampiamo le entità per controllare
        for (Entity e : entities) {
            System.out.println(e);
        }
        System.out.println("Entities extracted: " + entities.size());

        ComandiFactory comandiFactory = new ComandiFactory();
        String input = "";
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println("Inserisci il comando da eseguire: (comandi disponibili: " + comandiFactory.getComandi() +", quit" +")");
            input = scanner.nextLine();
            if (!input.equals("quit")) {
                try {
                    comandiFactory.makeComando(input).esegui(this);
                } catch (ClassNotFoundException|InvocationTargetException|InstantiationException|IllegalAccessException e) {
                    if(input.isEmpty() || input.isBlank()){
                        System.out.println("Inserisci un comando valido");
                    }else{
                        System.out.println("Il comando '" + input + "' non è stato riconosciuto");
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        } while (!input.equals("quit"));
        scanner.close();
    }
    public AppProperties getAppProperties() {
        return appProperties;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public int getEntityTypes() {
        return entityTypes;
    }

    public static void main(String[] args) throws Exception {
        AppProperties appProperties = AppProperties.getAppProperties();
        String APIKEY = appProperties.getAPIKey();
        String datasetPath = appProperties.getDatasetPath();
        String[] datasetFolders = appProperties.getDatasetFolders();
        String[] groundTruthFileNames = appProperties.getGroundTruthFileNames();

        System.out.println("API Key: " + APIKEY);
        System.out.println("Dataset Path: " + datasetPath);
        System.out.println("Dataset Folder: " + Arrays.toString(datasetFolders));
        System.out.println("Ground Truth File Name: " + Arrays.toString(groundTruthFileNames));

        Application app = new Application(appProperties);
        app.run();
    }

}

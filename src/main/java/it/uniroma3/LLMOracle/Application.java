package it.uniroma3.LLMOracle;

import it.uniroma3.LLMOracle.comando.ComandiFactory;
import it.uniroma3.LLMOracle.data.AlaskaDataset;
import it.uniroma3.LLMOracle.data.Dataset;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Scanner;

public class Application {
    public static AppProperties appProperties = null;

    private final Dataset dataset;
    private int entityTypes;

    public Application(AppProperties appProperties) throws IOException {
        Application.appProperties = appProperties;
        this.dataset = new AlaskaDataset();
    }

    public void run() {
        this.entityTypes = appProperties.getDatasetFolders().length;
        ComandiFactory comandiFactory = new ComandiFactory();
        String input = "";
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println("Inserisci il comando da eseguire: (comandi disponibili: " + comandiFactory.getComandi() + ", quit" + ")");
            input = scanner.nextLine();
            if (!input.equals("quit")) {
                try {
                    comandiFactory.makeComando(input).esegui(this);
                } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    if (input.isEmpty() || input.isBlank()) {
                        System.out.println("Inserisci un comando valido");
                    } else {
                        System.out.println("Il comando '" + input + "' non è stato riconosciuto");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (!input.equals("quit"));
        scanner.close();
    }

    public AppProperties getAppProperties() {
        return appProperties;
    }

    public int getEntityTypes() {
        return entityTypes;
    }

    public Dataset getDataset() {
        return dataset;
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

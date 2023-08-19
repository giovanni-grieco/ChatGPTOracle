package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.PromptBuilder;
import it.uniroma3.chatGPT.GPT.fineTuning.JSONLineGenerator;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityExtractor;
import it.uniroma3.chatGPT.utils.FileSaver;

import java.nio.file.Path;
import java.util.*;

public class Main {

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
        appProperties.validate(); //se la validazione fallisce, verranno sollevate eccezioni

        int entityTypes = datasetFolders.length;
        Set<Entity> entities = new HashSet<>();
        for (int type = 0; type < entityTypes; type++) {
            EntityExtractor extractor = new EntityExtractor(type, Path.of(datasetPath + "/" + groundTruthFileNames[type]));
            extractor.extractEntitiesFromGroundTruth(entities);
        }
        //Stampiamo le entità per controllare
        for (Entity e : entities) {
            System.out.println(e);
        }

        System.out.println("Entities extracted: " + entities.size());
        List<Entity> entityList = new ArrayList<>(entities);
        List<String> prompts = new ArrayList<>();
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.print("Inserisci il numero di prompt positivi: ");
        int matchingEntityPromptsAmount = keyboardScanner.nextInt();
        System.out.print("Inserisci il numero di prompt negativi: ");
        int nonMatchingEntityPromptsAmount = keyboardScanner.nextInt();
        keyboardScanner.close();
        System.out.println("Numero di prompt positivi: " + matchingEntityPromptsAmount);
        System.out.println("Numero di prompt negativi: " + nonMatchingEntityPromptsAmount);
        System.out.println("Creazione dei prompt...");
        //entità diverse fra loro

        PromptBuilder pb = new PromptBuilder(entityList, matchingEntityPromptsAmount, nonMatchingEntityPromptsAmount);

        pb.generateNonMatchingEntityPrompts(prompts);
        pb.generateMatchingEntityPrompts(prompts);

        System.out.println("Prompts size: " + prompts.size());
        List<String> filteredPrompts = new ArrayList<>();
        for (String prompt : prompts) {
            System.out.println(prompt);
            filteredPrompts.add(prompt.replaceAll("\"", "''"));
        }
        for (String prompt : filteredPrompts) {
            System.out.println(prompt);
        }
        prompts = filteredPrompts;

        String JSONLines = JSONLineGenerator.generateJSONLines(prompts);
        System.out.println(JSONLines);
        FileSaver.saveFile("C:/Users/giovi/Desktop/", "prompts.jsonl", JSONLines);

    }

    private static int funzioneDiRapporto(int n) {
        return Math.min(n, 10);
    }
}

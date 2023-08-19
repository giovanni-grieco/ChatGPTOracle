package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.GPT.PromptBuilder;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityExtractor;
import it.uniroma3.chatGPT.data.Score;
import it.uniroma3.chatGPT.data.ScoreCalculator;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;
import it.uniroma3.chatGPT.utils.FileSaver;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            AppProperties appProperties = AppProperties.getAppProperties();
            String APIKEY = appProperties.getAPIKey();
            String datasetPath = appProperties.getDatasetPath();
            String datasetFolder = appProperties.getDatasetFolder();
            String groundTruthFileName = appProperties.getGroundTruthFileName();
            System.out.println("API Key: " + APIKEY);
            System.out.println("Dataset Path: " + datasetPath);
            System.out.println("Dataset Folder: " + datasetFolder);
            System.out.println("Ground Truth File Name: " + groundTruthFileName);
            appProperties.validate();

            EntityExtractor extractor = new EntityExtractor(Path.of(datasetPath + "/" + groundTruthFileName));
            Set<Entity> entities = extractor.extractEntitiesFromGroundTruth();
            System.out.println("amount of entities extracted: " + entities.size());
            List<Entity> entityList = entities.stream().toList();

            List<String> prompts = new ArrayList<>();
            Scanner keyboardScanner = new Scanner(System.in);
            System.out.print("Inserisci il numero di prompt positivi: ");
            int matchingEntityPromptsAmount = keyboardScanner.nextInt();
            int nonMatchingEntityPromptsAmount = funzioneDiRapporto(entityList.size())*matchingEntityPromptsAmount;
            System.out.println("Numero di prompt positivi: " + matchingEntityPromptsAmount);
            System.out.println("Numero di prompt negativi: " + nonMatchingEntityPromptsAmount);
            System.out.println("Creazione dei prompt...");
            //entità diverse fra loro

            PromptBuilder.generateNonMatchingEntityPrompts(entityList,nonMatchingEntityPromptsAmount,prompts);

            PromptBuilder.generateMatchingEntityPrompts(entityList, matchingEntityPromptsAmount, prompts);

            System.out.println("Prompts size: " + prompts.size());

            ChatGPT gpt = new ChatGPT(APIKEY);
            List<GPTQuery> answers = gpt.processPrompts(prompts, "davinci:ft-personal-2023-08-18-17-03-50", 500);

            Score score = ScoreCalculator.calculateScore(answers, nonMatchingEntityPromptsAmount);

            String results = score.toString();
            System.out.println(results);

            LocalDate now = LocalDate.now();
            LocalTime nowTime = LocalTime.now();
            String fileName = appProperties.getDatasetFolder()+"-"+now + "_" + nowTime.getHour() + "-" + nowTime.getMinute() + "-" + nowTime.getSecond();
            FileSaver.saveFile("./results/", fileName + ".txt", results);
            System.out.println("File saved as ./results/" + fileName + ".txt");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static int funzioneDiRapporto(int n){
        return Math.min(n, 10);
    }
}

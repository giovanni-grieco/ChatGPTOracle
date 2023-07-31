package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityExtractor;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;
import it.uniroma3.chatGPT.utils.FileSaver;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        try {
            AppProperties appProperties = AppProperties.getAppProperties();
            System.out.println("API Key: " + appProperties.getAPIKey());
            System.out.println("Dataset Path: " + appProperties.getDatasetPath());
            System.out.println("Dataset Folder: " + appProperties.getDatasetFolder());
            System.out.println("Ground Truth File Name: " + appProperties.getGroundTruthFileName());

            EntityExtractor extractor = new EntityExtractor();
            Set<Entity> entities = extractor.extractEntitiesFromGroundTruth();
            for (Entity e : entities) {
                System.out.println(e.toString());
                List<File> htmls = e.dataLocationsToFiles();
                System.out.println("Htmls: " + htmls.toString());
            }

            List<String> prompts = (List<String>) ChatGPT.PromptBuilder.generatePrompts(entities);
            System.out.println("Entities size: " + entities.size());
            System.out.println("Prompts size: " + prompts.size());

            List<String> promptsRandom = new ArrayList<String>();
            for (int i = 0; i < 40; i++) {
                Random random = new Random();
                int randomNumber = random.nextInt();
                randomNumber = Math.abs(randomNumber);
                randomNumber = randomNumber % prompts.size();
                promptsRandom.add(prompts.get(randomNumber));
            }

            ChatGPT gpt = new ChatGPT();
            List<GPTQuery> answers = gpt.processPrompts(promptsRandom, "text-davinci-003", 20000);
            for (GPTQuery answer : answers) {
                System.out.println(answer.getRisposta());
            }

            int yes = 0;
            for (GPTQuery query : answers) {
                if (query.isYes()) {
                    yes++;
                }
            }
            String results = "Positive results: " + yes + "\n" +
                    "Negative results: " + (answers.size() - yes) + "\n" +
                    "Total results: " + answers.size() + "\n" +
                    "Accuracy: " + (double) yes / answers.size() + "\n";
            System.out.println(results);
            LocalDate now = LocalDate.now();
            LocalTime nowTime = LocalTime.now();
            String fileName = now.toString() + "_" + nowTime.getHour() + "-" + nowTime.getMinute() + "-" + nowTime.getSecond();
            FileSaver.saveFile("C:/Users/giovi/Desktop", fileName + ".txt", results);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static int firstNnumbers(int n) {
        return n * (n - 1) / 2;
    }
}

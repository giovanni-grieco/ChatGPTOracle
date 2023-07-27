package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.GPT.PromptBuilder;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args){
        try {
            AppProperties appProperties = AppProperties.getAppProperties();
            System.out.println("API Key: "+appProperties.getAPIKey());
            System.out.println("Dataset Path: "+appProperties.getDatasetPath());
            System.out.println("Dataset Folder: "+appProperties.getDatasetFolder());
            System.out.println("Ground Truth File Name: "+appProperties.getGroundTruthFileName());
            EntityExtractor extractor = new EntityExtractor(appProperties);
            Set<Entity> entities = extractor.extractEntitiesFromGroundTruth();
            for(Entity e:entities){
                System.out.println(e.toString());
                List<File> htmls = e.dataLocationsToFiles();
                System.out.println("Htmls: "+htmls.toString());

            }
            Entity firstEntity=(Entity) entities.toArray()[0];
            List<File> htmls = firstEntity.dataLocationsToFiles();
            ChatGPT gpt = new ChatGPT(appProperties);
            GPTQuery risposta = gpt.processPrompt(PromptBuilder.twoWebPagesTalkingAboutTheSameEntity(Files.readString(htmls.get(0).toPath()), Files.readString(htmls.get(1).toPath())), "text-davinci-003");
            System.out.println(risposta.getRisposta());
            /*ChatGPT gpt = new ChatGPT(appProperties);
            List<GPTQuery> risposte;
            risposte = gpt.processPrompts(List.of("Is 5+5 equals to 10? answer with yes or no", "Can a squirrel fly? Answer with yes or no"), "text-davinci-003", 1000);
            System.out.println("\n\nPrinting answers...");
            System.out.println("----");
            for (GPTQuery risposta : risposte) {
                System.out.println(risposta.toString()+"\n----");
            }*/
        }catch (IOException e){
            System.out.println(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

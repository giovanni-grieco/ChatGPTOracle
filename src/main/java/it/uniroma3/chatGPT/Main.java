package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityExtractor;

import java.io.IOException;
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
        Set<Entity> entities = extractor.extractEntities();
        for(Entity e:entities){
            System.out.println(e.toString());
        }
        System.out.println(entities.contains(new Entity("entity_id")));
        }catch (IOException e){
            System.out.println(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

   /* public static void main(String[] args){
        try {
            AppProperties appProperties = AppProperties.getAppProperties();
            System.out.println("API Key: "+appProperties.getAPIKey());
            System.out.println("Dataset Path: "+appProperties.getDatasetPath());
            ChatGPT gpt = new ChatGPT(appProperties);
            List<GPTQuery> risposte;
            risposte = gpt.processPrompts(List.of("Is 5+5 equals to 10? answer with yes or no", "Can a squirrel fly? Answer with yes or no"), "text-davinci-003", 1000);
            System.out.println("\n\nPrinting answers...");
            System.out.println("----");
            for (GPTQuery risposta : risposte) {
                System.out.println(risposta.toString()+"\n----");
            }
        }catch (IOException e){
            System.out.println(e.getMessage()+"\nAssicurarsi di avere un file chiamato app.properties nella root folder!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/
}

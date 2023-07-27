package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.GPT.PromptBuilder;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityExtractor;
import it.uniroma3.chatGPT.utils.HTMLFilter;

import java.io.File;
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
            String htmlNonFiltrato = Files.readString(htmls.get(0).toPath());
            System.out.println("Caratteri prima del filtraggio: "+htmlNonFiltrato.length());
            String pagina_filtrata=HTMLFilter.filter(Files.readString(htmls.get(0).toPath()),List.of("style","script","head","meta","img","link"));
            System.out.println("Caratteri dopo il filtraggio: "+pagina_filtrata.length());
            System.out.println("Differenza: "+(htmlNonFiltrato.length()-pagina_filtrata.length()));
            String pagina_filtrata1=HTMLFilter.filter(Files.readString(htmls.get(1).toPath()),List.of("style","script","head","meta","img","link"));
            ChatGPT gpt = new ChatGPT(appProperties);
            GPTQuery risposta = gpt.processPrompt(PromptBuilder.twoWebPagesTalkingAboutTheSameEntity(pagina_filtrata,pagina_filtrata1), "text-davinci-003");
            System.out.println(risposta.getRisposta());
            List<GPTQuery> risposte;
            risposte = gpt.processPrompts(List.of("Is 5+5 equals to 10? answer with yes or no", "Can a squirrel fly? Answer with yes or no"), "text-davinci-003", 1000);
            System.out.println("\n\nPrinting answers...");
            System.out.println("----");
            for (GPTQuery answer : risposte) {
                System.out.println(answer.toString()+"\n----");
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}

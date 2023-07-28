package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.GPT.PromptBuilder;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityExtractor;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        try {
            AppProperties appProperties = AppProperties.getAppProperties();
            System.out.println("API Key: " + appProperties.getAPIKey());
            System.out.println("Dataset Path: " + appProperties.getDatasetPath());
            System.out.println("Dataset Folder: " + appProperties.getDatasetFolder());
            System.out.println("Ground Truth File Name: " + appProperties.getGroundTruthFileName());

            EntityExtractor extractor = new EntityExtractor(appProperties);
            Set<Entity> entities = extractor.extractEntitiesFromGroundTruth();
            for (Entity e : entities) {
                System.out.println(e.toString());
                List<File> htmls = e.dataLocationsToFiles();
                System.out.println("Htmls: " + htmls.toString());
            }

            Entity firstEntity = (Entity) entities.toArray()[0];
            System.out.println("First Entity->" + firstEntity.toString());
            List<String> prompts = new ArrayList<>();
            System.out.println(firstEntity.getDataLocations().size());

            for (int i = 0; i < firstEntity.getDataLocations().size(); i++) {
                //l'unico di cui ho stabilito un template è ebay
                if (firstEntity.getDataLocations().get(i).getDomain().equals("www.ebay.com")) {
                    String html = Files.readString(firstEntity.getDataLocations().get(i).toFullPath());
                    String htmlFiltratoA = HTMLFilter.filter(html, HTMLFilter.DEFAULT_TAGS, firstEntity.getDataLocations().get(i).getDomain());


                    for (int j = i + 1; j < firstEntity.getDataLocations().size(); j++) {
                        if (firstEntity.getDataLocations().get(j).getDomain().equals("www.ebay.com")) {
                            String html1 = Files.readString(firstEntity.getDataLocations().get(j).toFullPath());
                            String htmlFiltratoB = HTMLFilter.filter(html1, HTMLFilter.DEFAULT_TAGS, firstEntity.getDataLocations().get(j).getDomain());
                            prompts.add(PromptBuilder.twoWebPagesTalkingAboutTheSameEntity(htmlFiltratoA, htmlFiltratoB));
                        }
                    }
                }
            }
            System.out.println(firstNnumbers(firstEntity.getDataLocations().size() - 1));
            System.out.println(prompts.size());

            ChatGPT gpt = new ChatGPT();
            List<GPTQuery> answers = gpt.processPrompts(prompts, "text-davinci-003", 20000);
            for (GPTQuery answer : answers) {
                System.out.println(answer);
            }

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

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
import java.util.ArrayList;
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
            /*List<File> htmls = firstEntity.dataLocationsToFiles();
            String htmlNonFiltrato = Files.readString(htmls.get(0).toPath());
            String pagina_filtrata=HTMLFilter.filter(Files.readString(htmls.get(0).toPath()),List.of("style","script","head","meta","img","link"));

            String htmlNonFiltrato1 = Files.readString(htmls.get(1).toPath());
            String pagina_filtrata1=HTMLFilter.filter(Files.readString(htmls.get(1).toPath()),List.of("style","script","head","meta","img","link"));

            String pagina_filtrata_contentRich=HTMLFilter.filter(Files.readString(htmls.get(0).toPath()),List.of("style","script","head","meta","img","link"), firstEntity.getDataLocations().get(0).getDomain());
            String pagina_filtrata1_contentRich=HTMLFilter.filter(Files.readString(htmls.get(1).toPath()),List.of("style","script","head","meta","img","link"), firstEntity.getDataLocations().get(1).getDomain());*/


            // FINIRE IL TEST IN MEDIA SCALA DOPO. HO BISOGNO DI MANGIARE!!
            List<String> prompts = new ArrayList<>();
            for(int i =0; i<firstEntity.getDataLocations().size(); i++){
                //l'unico di cui ho stabilito un template è ebay
                if(firstEntity.getDataLocations().get(i).getDomain().equals("www.ebay.com")){
                    String html = Files.readString(firstEntity.getDataLocations().get(i).toPath());
                    String htmlFiltrato = HTMLFilter.filter(html, List.of("style","script","head","meta","img","link"), firstEntity.getDataLocations().get(i).getDomain());
                }
            }

        }catch (IOException e){
            System.out.println(e.getMessage());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}

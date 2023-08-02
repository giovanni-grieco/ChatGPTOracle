package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityExtractor;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;
import it.uniroma3.chatGPT.utils.FileSaver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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
            Set<Entity>ebayEntities = new HashSet<>();
            for (Entity e : entities) {
                List<Data> ebayData = new ArrayList<>();
                for(Data d : e.getData()){
                    if(d.getDomain().equals("www.ebay.com")){
                        ebayData.add(d);
                    }
                }
                e.setData(ebayData);
                if(ebayData.size()>1){
                    ebayEntities.add(e);
                }
            }

            System.out.println("Computing prompts...");
            List<Entity> entityList = new ArrayList(ebayEntities);
            List<String> prompts = new ArrayList<>();

            //entità diverse fra loro
            for(int i=0;i<25;i++){
                Random random = new Random();
                int randomNumber = random.nextInt();
                randomNumber = Math.abs(randomNumber);
                randomNumber = randomNumber % entityList.size();
                Entity e1 = (Entity) entityList.get(randomNumber);
                int anotherRandomNumber = random.nextInt()%entityList.size();
                anotherRandomNumber = Math.abs(anotherRandomNumber);
                while(anotherRandomNumber == randomNumber){
                    anotherRandomNumber = random.nextInt()%entityList.size();
                    anotherRandomNumber = Math.abs(anotherRandomNumber);
                }
                Entity e2 = (Entity) entityList.get(anotherRandomNumber);
                //estraiamo 2 informazioni a caso dalle entità
                int randomDataNumber1 = random.nextInt()%e1.getData().size();
                randomDataNumber1 = Math.abs(randomDataNumber1);
                int randomDataNumber2 = random.nextInt()%e2.getData().size();
                randomDataNumber2 = Math.abs(randomDataNumber2);
                String dataE1 = Files.readString(e1.getData().get(randomDataNumber1).toFullPath());
                String dataE2 = Files.readString(e2.getData().get(randomDataNumber2).toFullPath());
                //Filtriamo le informazioni
                String pureDataE1 = HTMLFilter.filterTemplate(dataE1, HTMLFilter.DEFAULT_TAGS, e1.getData().get(randomDataNumber1).getDomain());
                String pureDataE2 = HTMLFilter.filterTemplate(dataE2, HTMLFilter.DEFAULT_TAGS, e2.getData().get(randomDataNumber2).getDomain());
                //Creiamo il prompt
                prompts.add(ChatGPT.PromptBuilder.buildPromptTwoSnippets(pureDataE1, pureDataE2));
            }

            //entità uguali fra loro
            for(int i=0;i<25; i++){
                Random random = new Random();
                int randomNumber = random.nextInt();
                randomNumber = randomNumber % entityList.size();
                randomNumber = Math.abs(randomNumber);
                Entity e1 = (Entity) entityList.get(randomNumber);
                int randomDataNumber1 = random.nextInt()%e1.getData().size();
                int randomDataNumber2 = random.nextInt()%e1.getData().size();
                randomDataNumber1 = Math.abs(randomDataNumber1);
                randomDataNumber2 = Math.abs(randomDataNumber2);
                while(randomDataNumber1 == randomDataNumber2){
                    randomDataNumber2 = random.nextInt()%e1.getData().size();
                    randomDataNumber2 = Math.abs(randomDataNumber2);
                }
                String data1 = Files.readString(e1.getData().get(randomDataNumber1).toFullPath());
                String data2 = Files.readString(e1.getData().get(randomDataNumber2).toFullPath());
                //Filtriamo le informazioni
                String pureDataE1 = HTMLFilter.filterTemplate(data1, HTMLFilter.DEFAULT_TAGS, e1.getData().get(randomDataNumber1).getDomain());
                String pureDataE2 = HTMLFilter.filterTemplate(data2, HTMLFilter.DEFAULT_TAGS, e1.getData().get(randomDataNumber2).getDomain());
                prompts.add(ChatGPT.PromptBuilder.buildPromptTwoSnippets(pureDataE1, pureDataE2));
            }

            System.out.println("Entities size: " + entities.size());
            System.out.println("Prompts size: " + prompts.size());

            ChatGPT gpt = new ChatGPT();
            List<GPTQuery> answers = gpt.processPrompts(prompts, "text-davinci-003", 20000);
            for (GPTQuery answer : answers) {
                System.out.println(answer.getRisposta());
            }

            //la prima metà dovrebbero essere tutti no
            //la seconda metà dovrebbero essere tutti si
            int truePositive = 0;
            int trueNegative = 0;
            int falsePositive = 0;
            int falseNegative = 0;

            for(int i = 0;i< answers.size();i++){
                if(i<answers.size()/2){
                    if(!answers.get(i).isYes()){
                        trueNegative++;
                    }else{
                        falsePositive++;
                    }
                }else{
                    if(answers.get(i).isYes()){
                        truePositive++;
                    }else{
                        falseNegative++;
                    }
                }
            }

            double precision = (double)truePositive/(truePositive+falsePositive);
            double recall = (double)truePositive/(truePositive+falseNegative);
            double Fscore = 2*((precision*recall)/(precision+recall));

            String results = "True positive: " + truePositive + "\n" +
                    "True negative: " + trueNegative+ "\n" +
                    "False positive: " + falsePositive+ "\n" +
                    "False negative: " + falseNegative+ "\n" +
                    "Total answers: " + answers.size() + "\n" +
                    "Precision: "+ (double)truePositive/(truePositive+falsePositive) + "\n" +
                    "Recall: "+ (double)truePositive/(truePositive+falseNegative) + "\n" +
                    "Fscore: "+ Fscore + "\n";
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

package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityExtractor;
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

            EntityExtractor extractor = new EntityExtractor(Path.of(datasetPath + "/" + groundTruthFileName));
            Set<Entity> entities = extractor.extractEntitiesFromGroundTruth();
            System.out.println("Entities extracted: " + entities.size());
           /* Set<Entity> ebayEntities = new HashSet<>();
            for (Entity e : entities) {
                List<Data> ebayData = new ArrayList<>();
                for (Data d : e.getData()) {
                    if (d.getDomain().equals("www.ebay.com")) {
                        ebayData.add(d);
                    }
                }
                e.setData(ebayData);
                if (ebayData.size() > 1) {
                    ebayEntities.add(e);
                }
            }*/
            System.out.println("Entities extracted: " + entities.size());
            System.out.println("Computing prompts...");
            List<Entity> entityList = new ArrayList<>(entities);
            List<String> prompts = new ArrayList<>();
            int entitaDiverseFraLoro = 40;
            int entitaUgualiFraLoro = 10;

            //entità diverse fra loro
            for (int i = 0; i < entitaDiverseFraLoro; i++) {
                Random random = new Random();
                int randomNumber = random.nextInt();
                randomNumber = Math.abs(randomNumber);
                randomNumber = randomNumber % entityList.size();
                Entity e1 = entityList.get(randomNumber);
                int anotherRandomNumber = random.nextInt() % entityList.size();
                anotherRandomNumber = Math.abs(anotherRandomNumber);
                while (anotherRandomNumber == randomNumber) {
                    anotherRandomNumber = random.nextInt() % entityList.size();
                    anotherRandomNumber = Math.abs(anotherRandomNumber);
                }
                Entity e2 = entityList.get(anotherRandomNumber);
                //estraiamo 2 informazioni a caso dalle entità
                int randomDataNumber1 = random.nextInt() % e1.getData().size();
                randomDataNumber1 = Math.abs(randomDataNumber1);
                int randomDataNumber2 = random.nextInt() % e2.getData().size();
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
            for (int i = 0; i < entitaUgualiFraLoro; i++) {
                Random random = new Random();
                int randomNumber = random.nextInt();
                randomNumber = randomNumber % entityList.size();
                randomNumber = Math.abs(randomNumber);
                Entity e1 = entityList.get(randomNumber);
                int randomDataNumber1 = random.nextInt() % e1.getData().size();
                int randomDataNumber2 = random.nextInt() % e1.getData().size();
                randomDataNumber1 = Math.abs(randomDataNumber1);
                randomDataNumber2 = Math.abs(randomDataNumber2);
                while (randomDataNumber1 == randomDataNumber2) {
                    randomDataNumber2 = random.nextInt() % e1.getData().size();
                    randomDataNumber2 = Math.abs(randomDataNumber2);
                }
                String data1 = Files.readString(e1.getData().get(randomDataNumber1).toFullPath());
                String data2 = Files.readString(e1.getData().get(randomDataNumber2).toFullPath());
                //Filtriamo le informazioni
                String pureDataE1 = HTMLFilter.filterTemplate(data1, HTMLFilter.DEFAULT_TAGS, e1.getData().get(randomDataNumber1).getDomain());
                String pureDataE2 = HTMLFilter.filterTemplate(data2, HTMLFilter.DEFAULT_TAGS, e1.getData().get(randomDataNumber2).getDomain());
                prompts.add(ChatGPT.PromptBuilder.buildPromptTwoSnippets(pureDataE1, pureDataE2));
            }

            System.out.println("Prompts size: " + prompts.size());

            ChatGPT gpt = new ChatGPT(APIKEY);
            List<GPTQuery> answers = gpt.processPrompts(prompts, "text-davinci-003", 20000);

            //la prima metà dovrebbero essere tutti no
            //la seconda metà dovrebbero essere tutti si
            int truePositive = 0;
            int trueNegative = 0;
            int falsePositive = 0;
            int falseNegative = 0;

            for (int i = 0; i < answers.size(); i++) {
                if (i < entitaDiverseFraLoro) {
                    if (!answers.get(i).isYes()) {
                        trueNegative++;
                    } else {
                        falsePositive++;
                    }
                } else {
                    if (answers.get(i).isYes()) {
                        truePositive++;
                    } else {
                        falseNegative++;
                    }
                }
            }


            double precision = (double) truePositive / (truePositive + falsePositive);
            double recall = (double) truePositive / (truePositive + falseNegative);
            double fscore = 2 * ((precision * recall) / (precision + recall));

            String results = "True positive: " + truePositive + "\n" +
                    "True negative: " + trueNegative + "\n" +
                    "False positive: " + falsePositive + "\n" +
                    "False negative: " + falseNegative + "\n" +
                    "Total answers: " + answers.size() + "\n" +
                    "Precision: " + (double) truePositive / (truePositive + falsePositive) + "\n" +
                    "Recall: " + (double) truePositive / (truePositive + falseNegative) + "\n" +
                    "fscore: " + fscore + "\n";
            System.out.println(results);
            LocalDate now = LocalDate.now();
            LocalTime nowTime = LocalTime.now();
            String fileName = now + "_" + nowTime.getHour() + "-" + nowTime.getMinute() + "-" + nowTime.getSecond();
            FileSaver.saveFile("C:/Users/giovi/Desktop", fileName + ".txt", results);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

}

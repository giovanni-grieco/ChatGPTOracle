package it.uniroma3.chatGPT;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
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

        int entityTypes=datasetFolders.length;
        Set<Entity> entities = new HashSet<>();
        for(int type = 0; type<entityTypes; type++){
            EntityExtractor extractor = new EntityExtractor(type, Path.of(datasetPath+"/"+groundTruthFileNames[type]));
            extractor.extractEntitiesFromGroundTruth(entities);
        }
        //Stampiamo le entità per controllare
        for(Entity e:entities){
            System.out.println(e);
        }

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

            PromptBuilder pb = new PromptBuilder(entityList, matchingEntityPromptsAmount, nonMatchingEntityPromptsAmount);

            pb.generateNonMatchingEntityPrompts(prompts);
            pb.generateMatchingEntityPrompts(prompts);

        System.out.println("Prompts size: " + prompts.size());
        List<String>filteredPrompts = new ArrayList<>();
        for(String prompt : prompts){
            System.out.println(prompt);
            filteredPrompts.add(prompt.replaceAll("\"","''"));
        }
        for(String prompt : filteredPrompts){
            System.out.println(prompt);
        }
        prompts = filteredPrompts;

        System.out.println(prompts.get(prompts.size()/2));
        System.out.println(prompts.get((prompts.size()/2)+1));
        StringBuilder sb = new StringBuilder();
        int iterator = 0;
        for(String p:prompts){
            sb.append("{");
            sb.append("\"prompt\":\""+p+"\",");
            if(iterator<=prompts.size()/2) {
                sb.append(" \"completion\":\"no\"");
            }else{
                sb.append("\"completion\":\" yes\"");
            }
            sb.append("}\n");
            iterator++;
        }
        System.out.println(sb.toString());
        FileSaver.saveFile("C:/Users/giovi/Desktop/", "prompts.jsonl", sb.toString());
        /*ChatGPT gpt = new ChatGPT(APIKEY);
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
        FileSaver.saveFile("./results/", fileName + ".txt", results);
        System.out.println("File saved as ./results/" + fileName + ".txt");*/
    }

    private static int funzioneDiRapporto(int n) {
        return Math.min(n, 10);
    }
}

package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.*;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Prompt;
import it.uniroma3.chatGPT.utils.FileRetriever;
import it.uniroma3.chatGPT.utils.FileSaver;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DatasetRoger implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException {

        String pathToRogerDataset = "E:/Tirocinio Dataset/oracle_ext_camera0_15.csv";
        try {
            File datasetFile = FileRetriever.getFile(pathToRogerDataset);
            String dataset = Files.readString(datasetFile.toPath());
            String[] lines = dataset.split("\n");
            List<Prompt> prompts = new ArrayList<>();
            for (String line : lines) {
                String[] splitLine = line.split(";");
                String entityString1 = splitLine[0];
                String entityString2 = splitLine[1];
                String expectedAnswer = splitLine[2];
                //TODO creare prompt e darli in pasto a GPT
                String promptString = "first: " + entityString1 + ". second: " + entityString2;
                Prompt p = new Prompt (promptString, Boolean.parseBoolean(expectedAnswer));
                prompts.add(p);
            }
            String modello = "gpt-3.5-turbo";
            System.out.println(prompts);
            LLM llm = new AzureGPT("You will be given 2 snippets of texts. You will have to answer whether the 2 texts are talking about the same entity, object or subject. Answer only with yes or no.");
            List<GPTQuery> answers = llm.processPrompts(prompts, modello, 1);
            String results = ScoreCalculator.calculateScore(answers).toString();
            System.out.println(results);

            LocalDate now = LocalDate.now();
            LocalTime nowTime = LocalTime.now();
            String fileName = "datasetRoger" + "-" + now + "_" + nowTime.getHour() + "-" + nowTime.getMinute() + "-" + nowTime.getSecond();
            FileSaver.saveFile("./results/", fileName + ".txt", results+"\n\n"+modello);
            System.out.println("File saved as ./results/" + fileName + ".txt");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

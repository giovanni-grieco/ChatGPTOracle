package it.uniroma3.LLMOracle.comando.comandi;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.GPT.GPTException;
import it.uniroma3.LLMOracle.GPT.GPTQuery;
import it.uniroma3.LLMOracle.GPT.LLM;
import it.uniroma3.LLMOracle.GPT.LLMFactory;
import it.uniroma3.LLMOracle.GPT.prompt.ClassificationPrompt;
import it.uniroma3.LLMOracle.GPT.prompt.Prompt;
import it.uniroma3.LLMOracle.GPT.prompt.PromptBuilder;
import it.uniroma3.LLMOracle.GPT.score.Score;
import it.uniroma3.LLMOracle.GPT.score.ScoreCalculator;
import it.uniroma3.LLMOracle.comando.Comando;
import it.uniroma3.LLMOracle.data.Data;
import it.uniroma3.LLMOracle.data.Dataset;
import it.uniroma3.LLMOracle.data.Entity;
import it.uniroma3.LLMOracle.utils.Sampler;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class AllDomains implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException, IOException, GPTException {
        Map<Integer,Score> percentuale2Score = new HashMap<>();
        List<Entity> randomizedEntity = new ArrayList<>(application.getDataset().getEntities());
        //Dataset alaska = application.getDataset();
        System.out.println("Genero casi positivi");
        int percentualeCasiPositivi = 5;
        int casiTotali = 2000;
        while (percentualeCasiPositivi <= 100) {
            Collections.shuffle(randomizedEntity);
            List<Prompt> positivePromptList = new ArrayList<>();
            List<Prompt> negativePromptList = new ArrayList<>();
            int casiPositivi = (int) ((casiTotali * percentualeCasiPositivi) / 100);
            int casiNegativi = casiTotali - casiPositivi;
            System.out.println("Percentuale casi positivi: " + percentualeCasiPositivi + "%");
            System.out.println("Casi positivi: " + casiPositivi);
            System.out.println("Casi negativi: " + casiNegativi);
            while (positivePromptList.size() < casiPositivi) {
                System.out.print("Prompt positivi generati: " + positivePromptList.size() + "\r");
                Entity e = randomizedEntity.get(0);
                List<Data> entityData = new Sampler<Data>(2, e.getData()).sampleCollection();
                String t1 = entityData.get(0).getTitleQuick();
                String t2 = entityData.get(1).getTitleQuick();
                if (t1.isEmpty() || t1.isBlank() || t2.isEmpty() || t2.isBlank()) {
                    Collections.shuffle(randomizedEntity);
                    System.out.println("Riproviamo...");
                    continue;
                }
                ClassificationPrompt prompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(t1, t2, true);
                positivePromptList.add(prompt);
                Collections.shuffle(randomizedEntity);
            }
            System.out.println("Prompt positivi generati: " + positivePromptList.size());
            System.out.println("Alcuni prompt presi d'esempio:\n" + new Sampler<Prompt>(4, positivePromptList).sampleCollection());
            System.out.println("Genero casi negativi");
            while (negativePromptList.size() < casiNegativi) {
                System.out.print("Prompt negativi generati: " + negativePromptList.size() + "\r");
                Entity e1 = randomizedEntity.get(0);
                Entity e2 = randomizedEntity.get(1);
                Data d1 = new Sampler<Data>(1, e1.getData()).sampleCollection().get(0);
                Data d2 = new Sampler<Data>(1, e2.getData()).sampleCollection().get(0);
                String t1 = d1.getTitleQuick();
                String t2 = d2.getTitleQuick();
                if (t1.isEmpty() || t1.isBlank() || t2.isEmpty() || t2.isBlank()) {
                    Collections.shuffle(randomizedEntity);
                    System.out.println("Riproviamo...");
                    continue;
                }
                ClassificationPrompt negativePrompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(t1, t2, false);
                negativePromptList.add(negativePrompt);
                Collections.shuffle(randomizedEntity);
            }
            System.out.println("Prompt negativi generati: " + negativePromptList.size());
            System.out.println("Alcuni prompt presi d'esempio:\n" + new Sampler<Prompt>(4, negativePromptList).sampleCollection());
            LLM gpt = LLMFactory.createLLMAllDefault();
            List<GPTQuery> rispostePositive = gpt.processPrompts(positivePromptList, "gpt-3.5-turbo", 0);
            List<GPTQuery> risposteNegative = gpt.processPrompts(negativePromptList, "gpt-3.5-turbo", 0);
            List<GPTQuery> risposte = new ArrayList<>();
            risposte.addAll(rispostePositive);
            risposte.addAll(risposteNegative);
            Score score = ScoreCalculator.calculateScore(risposte);
            System.out.println("Score:\n" + score);
            percentuale2Score.put(percentualeCasiPositivi, score);
            percentualeCasiPositivi+= 5;
        }
        Workbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Score");
        XSSFRow row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Percentuale casi positivi");
        row0.createCell(1).setCellValue("TP");
        row0.createCell(2).setCellValue("TN");
        row0.createCell(3).setCellValue("FP");
        row0.createCell(4).setCellValue("FN");
        row0.createCell(5).setCellValue("Precision");
        row0.createCell(6).setCellValue("Recall");
        row0.createCell(7).setCellValue("F1");
        row0.createCell(8).setCellValue("MCC");
        row0.createCell(9).setCellValue("Total cases");
        for(Integer percentuale : percentuale2Score.keySet()){
            Score score = percentuale2Score.get(percentuale);
            XSSFRow row = sheet.createRow(sheet.getLastRowNum()+1);
            row.createCell(0).setCellValue(percentuale);
            row.createCell(1).setCellValue(score.getTP());
            row.createCell(2).setCellValue(score.getTN());
            row.createCell(3).setCellValue(score.getFP());
            row.createCell(4).setCellValue(score.getFN());
            row.createCell(5).setCellValue(score.getPrecision());
            row.createCell(6).setCellValue(score.getRecall());
            row.createCell(7).setCellValue(score.getFScore());
            row.createCell(8).setCellValue(score.getMCC());
            row.createCell(9).setCellValue(score.getTP()+score.getTN()+score.getFP()+score.getFN());
        }
        //salviamo il file
        FileOutputStream fileOut = new FileOutputStream("./spreadsheets/stats/allDomains.xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}

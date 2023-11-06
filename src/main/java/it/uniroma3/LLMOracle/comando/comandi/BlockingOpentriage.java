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
import it.uniroma3.LLMOracle.data.*;
import it.uniroma3.LLMOracle.data.extraction.BlockDataExtractor;
import it.uniroma3.LLMOracle.utils.Sampler;
import it.uniroma3.LLMOracle.utils.textDistance.CosineSimilarityText;
import it.uniroma3.LLMOracle.utils.textDistance.LevenshteinDistance;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockingOpentriage implements Comando {


    //Si, sembra soffrire di mappite però non posso assegnare questa responsabilità a una specifica classe esistente
    private final Map<Blocco, List<Prompt>> blockPromptMap;
    private final Map<Blocco, List<GPTQuery>> blockQueryMap;
    private final Map<Blocco, Score> blockScoreMap;
    private final Map<Prompt, Double> promptSimilarityMap;

    private final Map<Prompt, Integer> promptLevenshteinDistanceMap;

    private final Map<Blocco, Double> blockAverageTextCosineSimilarityMap;

    private final Map<Blocco, Double> blockLevenshteinDistanceMap;

    public BlockingOpentriage(){
        this.blockPromptMap = new HashMap<>();
        this.blockQueryMap = new HashMap<>();
        this.blockScoreMap = new HashMap<>();
        this.promptSimilarityMap = new HashMap<>();
        this.promptLevenshteinDistanceMap = new HashMap<>();
        this.blockAverageTextCosineSimilarityMap = new HashMap<>();
        this.blockLevenshteinDistanceMap = new HashMap<>();
    }

    @Override
    public void esegui(Application application) throws InterruptedException, IOException, GPTException {
        String datasetFolderPath = application.getAppProperties().getDatasetPath();
        String datasetPath = datasetFolderPath + "/" + "blockpages_camera0_15.csv";
        BlockDataExtractor blockEE = new BlockDataExtractor(datasetPath, EntityType.CAMERA);
        Dataset dataset = application.getDataset();
        List<BlockData> blockData;
        System.out.println("Carico i blocchi...");
        while (blockEE.hasNextBlock()) {
            List<Prompt> prompts = new ArrayList<>();
            Blocco blocco = blockEE.nextBlock();
            blockData = blocco.makeDataList();
            for (int i = 0; i < blockData.size(); i++) {
                if(blockData.get(i).getTitle().isEmpty() || blockData.get(i).getTitle().isBlank()){
                    continue;
                }
                Entity e1 = dataset.getEntityByData(blockData.get(i));
                for (int j = i; j < blockData.size(); j++) {
                    if(blockData.get(j).getTitle().isEmpty() || blockData.get(j).getTitle().isBlank()){
                        continue;
                    }
                    Entity e2 = dataset.getEntityByData(blockData.get(j));
                    //System.out.println(e2);
                    if (e1 != null && e2 != null) {
                        Prompt prompt = null;
                        String title1 = blockData.get(i).getTitle();
                        String title2 = blockData.get(j).getTitle();
                        Double titleTextDistance = CosineSimilarityText.apply(title1, title2);
                        Integer titleLevenshteinDistance = LevenshteinDistance.calculate(title1, title2);
                        if (e1.equals(e2)) {
                            prompt = PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(title1, title2, true);
                        } else {
                            prompt = PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(title1, title2, false);
                        }
                        prompts.add(prompt);
                        this.promptSimilarityMap.put(prompt, titleTextDistance);
                        this.promptLevenshteinDistanceMap.put(prompt, titleLevenshteinDistance);
                    }
                }
            }
            List<Prompt> sampledPrompts = new Sampler<>(5, prompts).sampleCollection();
            this.blockPromptMap.put(blocco, sampledPrompts);
        }
        //calcolo le distanze e similarità medie
        for(Blocco b: this.blockPromptMap.keySet()){
            List<Prompt> prompts = this.blockPromptMap.get(b);
            int levenshteinSum = 0;
            double sum = 0;
            for(Prompt p: prompts){
                sum += this.promptSimilarityMap.get(p);
                levenshteinSum += this.promptLevenshteinDistanceMap.get(p);
            }
            double levenshteinAverage = (double) levenshteinSum /prompts.size();
            double average = sum/prompts.size();
            this.blockAverageTextCosineSimilarityMap.put(b, average);
            this.blockLevenshteinDistanceMap.put(b, levenshteinAverage);
        }
        System.out.println("Inizio interrogazione...");
        Workbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("OpenTriage blocking");
        XSSFRow row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Blocco");
        row0.createCell(1).setCellValue("TP");
        row0.createCell(2).setCellValue("TN");
        row0.createCell(3).setCellValue("FP");
        row0.createCell(4).setCellValue("FN");
        row0.createCell(5).setCellValue("average text cosine similarity");
        row0.createCell(6).setCellValue("Levenshtein distance");
        for(Blocco b: this.blockPromptMap.keySet()){
            XSSFRow nextRow = sheet.createRow(sheet.getLastRowNum()+1);
            System.out.print(b+": ");
            System.out.println(this.blockPromptMap.get(b).size());
            //iniziamo l'interrogazione
            LLM llm = LLMFactory.createLLMAllDefault();
            List<GPTQuery> answers = llm.processPrompts(this.blockPromptMap.get(b), "gpt-35-turbo", 0);
            this.blockQueryMap.put(b, answers);
            XSSFSheet promptSheet = (XSSFSheet) workbook.createSheet("Blocco "+b.getId());
            XSSFRow row0Prompt = promptSheet.createRow(0);
            row0Prompt.createCell(0).setCellValue("Prompt");
            row0Prompt.createCell(1).setCellValue("Risposta");
            row0Prompt.createCell(2).setCellValue("Ground Truth Linkage");
            for(GPTQuery query : answers){
                XSSFRow promptRow = promptSheet.createRow(promptSheet.getLastRowNum()+1);
                promptRow.createCell(0).setCellValue(query.getPrompt().getTextPrompt());
                promptRow.createCell(1).setCellValue(query.getRisposta());
                promptRow.createCell(2).setCellValue(((ClassificationPrompt)query.getPrompt()).isPositive());
            }
            Score score = ScoreCalculator.calculateScore(answers);
            this.blockScoreMap.put(b, score);
            nextRow.createCell(0).setCellValue(b.getId());
            nextRow.createCell(1).setCellValue(score.getTP());
            nextRow.createCell(2).setCellValue(score.getTN());
            nextRow.createCell(3).setCellValue(score.getFP());
            nextRow.createCell(4).setCellValue(score.getFN());
            nextRow.createCell(5).setCellValue(this.blockAverageTextCosineSimilarityMap.get(b));
            nextRow.createCell(6).setCellValue(this.blockLevenshteinDistanceMap.get(b));
            System.out.println(score);
        }
        System.out.println("Interrogazione finita.");
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        String dateAndTime = date +"_"+ time.getHour()+ "_"+ time.getMinute();
        FileOutputStream fileOut = new FileOutputStream("./spreadsheets/blocking/"+"OpenTriageBlocking"+"-"+dateAndTime+".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}

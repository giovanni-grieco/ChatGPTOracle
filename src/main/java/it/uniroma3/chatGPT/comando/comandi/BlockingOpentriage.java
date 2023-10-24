package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.GPT.LLM;
import it.uniroma3.chatGPT.GPT.chatCompletion.AzureGPT;
import it.uniroma3.chatGPT.GPT.prompt.Prompt;
import it.uniroma3.chatGPT.GPT.prompt.PromptBuilder;
import it.uniroma3.chatGPT.GPT.score.Score;
import it.uniroma3.chatGPT.GPT.score.ScoreCalculator;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.*;
import it.uniroma3.chatGPT.data.extraction.BlockDataExtractor;
import it.uniroma3.chatGPT.utils.Sampler;
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

    private Map<Blocco, List<Prompt>> blockPromptMap;
    private Map<Blocco, List<GPTQuery>> blockQueryMap;
    private Map<Blocco, Score> blockScoreMap;

    public BlockingOpentriage(){
        this.blockPromptMap = new HashMap<>();
        this.blockQueryMap = new HashMap<>();
        this.blockScoreMap = new HashMap<>();
    }

    @Override
    public void esegui(Application application) throws InterruptedException, IOException {

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
            //System.out.println(blockData);
            for (int i = 0; i < blockData.size(); i++) {
                if(blockData.get(i).getTitle().isEmpty() || blockData.get(i).getTitle().isBlank()){
                    continue;
                }
                Entity e1 = dataset.getEntityByData(blockData.get(i));
                //System.out.println(e1);
                for (int j = i; j < blockData.size(); j++) {
                    if(blockData.get(j).getTitle().isEmpty() || blockData.get(j).getTitle().isBlank()){
                        continue;
                    }
                    Entity e2 = dataset.getEntityByData(blockData.get(j));
                    //System.out.println(e2);
                    if (e1 != null && e2 != null) {
                        if (e1.equals(e2)) {
                            prompts.add(PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(blockData.get(i).getTitle(), blockData.get(j).getTitle(), true));
                        } else {
                            prompts.add(PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(blockData.get(i).getTitle(), blockData.get(j).getTitle(), false));
                        }
                    }
                }
            }
            List<Prompt> sampledPrompts = new Sampler<Prompt>(1000, prompts).sampleCollection();
            this.blockPromptMap.put(blocco, sampledPrompts);
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
        for(Blocco b: this.blockPromptMap.keySet()){
            XSSFRow nextRow = sheet.createRow(sheet.getLastRowNum()+1);
            System.out.print(b+": ");
            System.out.println(this.blockPromptMap.get(b).size());
            //iniziamo l'interrogazione
            LLM llm = new AzureGPT(LLM.STANDARD_INITIALIZATION_PROMPT);
            List<GPTQuery> answers = llm.processPrompts(this.blockPromptMap.get(b), "gpt-35-turbo", 0);
            this.blockQueryMap.put(b, answers);
            Score score = ScoreCalculator.calculateScore(answers);
            this.blockScoreMap.put(b, score);
            nextRow.createCell(0).setCellValue(b.getId());
            nextRow.createCell(1).setCellValue(score.getTP());
            nextRow.createCell(2).setCellValue(score.getTN());
            nextRow.createCell(3).setCellValue(score.getFP());
            nextRow.createCell(4).setCellValue(score.getFN());
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
        /*LLM llm = new AzureGPT(LLM.STANDARD_INITIALIZATION_PROMPT);
        var answers = llm.processPrompts(prompts, "gpt-35-turbo", 0);
        Score score = ScoreCalculator.calculateScore(answers);
        System.out.println(score);*/
    }
}

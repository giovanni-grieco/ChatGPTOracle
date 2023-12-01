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
import it.uniroma3.LLMOracle.data.Blocco;
import it.uniroma3.LLMOracle.utils.AddToMapList;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class BaselineTrainingPrompt implements Comando {

    private final Map<Prompt, String[]> promptToTextArrayMap;
    private final Map<Blocco, List<Prompt>> blockPromptMap;
    private final Map<Blocco, List<GPTQuery>> blockQueryMap;
    private final Map<Blocco, Score> blockScoreMap;

    public BaselineTrainingPrompt(){
        this.blockPromptMap = new HashMap<>();
        this.blockQueryMap = new HashMap<>();
        this.blockScoreMap = new HashMap<>();
        this.promptToTextArrayMap = new HashMap<>();
    }

    @Override
    public void esegui(Application application) throws InterruptedException, IOException, GPTException {
        String datasetFolderPath = application.getAppProperties().getDatasetPath();
        String datasetPath = datasetFolderPath + "/nuovo/camera/train_ext_camera0_15.csv";
        BufferedReader datasetReader = new BufferedReader(new FileReader(datasetPath));
        populatePromptMaps(datasetReader, this.blockPromptMap,60);
        datasetReader.close();
        Set<Blocco> blocks = new HashSet<>(this.blockPromptMap.keySet());
        /*for(Blocco b: this.blockPromptMap.keySet()){
            System.out.println(blockPromptMap.get(b));
        }*/
        //this.makeExcelFile(this.iterateOnBlocks(blocks));
        this.makeExcelFile(this.iterateOnSingleBlockVariableCharacter((Blocco)blocks.toArray()[0]));
    }

    private Set<Blocco> iterateOnSingleBlockVariableCharacter(Blocco blocco) throws GPTException, InterruptedException {
        //simuliamo la creazione dei blocchi ma in realtà tutti i prompt sono in un unico blocco
        //li rinominiamo usando l'id basato sul numero di caratteri
        Set<Blocco> blocchiFarlocchi = new HashSet<>();
        List<Prompt> promptsOfBlock = this.blockPromptMap.get(blocco);
        int charsAmountPerDescription = 10;
        while(charsAmountPerDescription <= 200){
            for(Prompt p : promptsOfBlock){
                String[] textArray = this.promptToTextArrayMap.get(p);
                String textA = textArray[0];
                String textB = textArray[1];
                String cutTextA = textA.substring(0, Math.min(textA.length(), charsAmountPerDescription));
                String cutTextB = textB.substring(0, Math.min(textB.length(), charsAmountPerDescription));
                ClassificationPrompt prompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(cutTextA, cutTextB, ((ClassificationPrompt) p).isPositive());
                Blocco newBlocco = new Blocco(String.valueOf(charsAmountPerDescription));
                AddToMapList.addToMapList(newBlocco, prompt, this.blockPromptMap);
                blocchiFarlocchi.add(newBlocco);
            }
            charsAmountPerDescription += 10;
        }
        for(Blocco farlocco : blocchiFarlocchi){
            List<Prompt> corti = this.blockPromptMap.get(farlocco);
            LLM gpt = LLMFactory.createLLMAllDefault();
            List<GPTQuery> queries = gpt.processPrompts(corti, "gpt-3.5-turbo", 0);
            this.blockQueryMap.put(farlocco, queries);
            Score scorePerBlock = ScoreCalculator.calculateScore(queries);
            this.blockScoreMap.put(farlocco, scorePerBlock);
        }
        return blocchiFarlocchi;
    }

    private Set<Blocco> iterateOnBlocks(Set<Blocco> blocks) throws GPTException, InterruptedException {
        for(Blocco b: this.blockPromptMap.keySet()){
            blocks.remove(b);
            System.out.println("Block selected: "+b.getId()+"\nBlocks remaining: "+ blocks.size());
            List<Prompt> promptsOfBlock = this.blockPromptMap.get(b);
            LLM gpt = LLMFactory.createLLMAllDefault();
            List<GPTQuery> queries = gpt.processPrompts(promptsOfBlock, "gpt-3.5-turbo", 0);
            this.blockQueryMap.put(b, queries);
            Score scorePerBlock = ScoreCalculator.calculateScore(queries);
            this.blockScoreMap.put(b, scorePerBlock);
        }
        return blocks;
    }

    private void populatePromptMaps(BufferedReader reader, Map<Blocco, List<Prompt>> blockPromptMap) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(";");
            Blocco b = new Blocco(columns[0]);
            String textA = columns[1].toLowerCase().replaceAll("\"", "\\\"");
            String textB = columns[2].toLowerCase().replaceAll("\"", "\\\"");
            ClassificationPrompt prompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(textA, textB, Boolean.parseBoolean(columns[3]));
            AddToMapList.addToMapList(b, prompt, blockPromptMap);
        }
    }

    private void populatePromptMaps(BufferedReader reader, Map<Blocco, List<Prompt>> blockPromptMap, int charsAmountPerDescription) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(";");
            Blocco b = new Blocco(columns[0]);
            String textA = columns[1].toLowerCase().replaceAll("\"", "\\\"");
            String textB = columns[2].toLowerCase().replaceAll("\"", "\\\"");
            String cutTextA = textA.substring(0, Math.min(textA.length(), charsAmountPerDescription));
            String cutTextB = textB.substring(0, Math.min(textB.length(), charsAmountPerDescription));
            ClassificationPrompt prompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(cutTextA, cutTextB, Boolean.parseBoolean(columns[3]));
            this.promptToTextArrayMap.put(prompt, new String[]{textA, textB});
            AddToMapList.addToMapList(b, prompt, blockPromptMap);
        }
    }

    private void makeExcelFile(Set<Blocco> involvedBlocks) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("OpenTriage blocking");
        XSSFRow row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Blocco");
        row0.createCell(1).setCellValue("TP");
        row0.createCell(2).setCellValue("TN");
        row0.createCell(3).setCellValue("FP");
        row0.createCell(4).setCellValue("FN");
        row0.createCell(5).setCellValue("F1");
        row0.createCell(6).setCellValue("MCC");
        for (Blocco blocco : involvedBlocks) {
            XSSFSheet blockSheet = (XSSFSheet) workbook.createSheet(blocco.getId());
            XSSFRow blockSheetHeaderRow = blockSheet.createRow(0);
            blockSheetHeaderRow.createCell(0).setCellValue("Prompt");
            blockSheetHeaderRow.createCell(1).setCellValue("Risposta");
            blockSheetHeaderRow.createCell(2).setCellValue("Risposta attesa");
            //Fixare per few shots per blocco. Dato che se iteriamo su tutti i blocchi, non è detto che troviamo le query
            //per tutti i blocchi dato che in few shots per blocco iteriamo sui blocchi di test.
            for (GPTQuery query : this.blockQueryMap.get(blocco)) {
                XSSFRow blockSheetRow = blockSheet.createRow(blockSheet.getLastRowNum() + 1);
                blockSheetRow.createCell(0).setCellValue(query.getPrompt().getTextPrompt());
                blockSheetRow.createCell(1).setCellValue(query.getRisposta());
                blockSheetRow.createCell(2).setCellValue(((ClassificationPrompt) query.getPrompt()).isPositive() ? "yes" : "no");
            }
            //Salviamo i prompt effettuati

            XSSFRow blockRow = sheet.createRow(sheet.getLastRowNum() + 1);
            Score blockScore = this.blockScoreMap.get(blocco);
            blockRow.createCell(0).setCellValue(blocco.getId());
            blockRow.createCell(1).setCellValue(blockScore.getTP());
            blockRow.createCell(2).setCellValue(blockScore.getTN());
            blockRow.createCell(3).setCellValue(blockScore.getFP());
            blockRow.createCell(4).setCellValue(blockScore.getFN());
            blockRow.createCell(5).setCellValue(blockScore.getFScore());
            blockRow.createCell(6).setCellValue(blockScore.getMCC());
        }

        //Aggiungiamo la data al nome
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        String dateAndTime = date + "_" + time.getHour() + "_" + time.getMinute();
        FileOutputStream fileOut = new FileOutputStream("./spreadsheets/blocking/" + "baseline-"+"variableLength-"+ dateAndTime + ".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}

package it.uniroma3.LLMOracle.comando.comandi;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.GPT.GPTQuery;
import it.uniroma3.LLMOracle.GPT.LLM;
import it.uniroma3.LLMOracle.GPT.chatCompletion.AzureGPT;
import it.uniroma3.LLMOracle.GPT.chatCompletion.Chat;
import it.uniroma3.LLMOracle.GPT.prompt.ClassificationPrompt;
import it.uniroma3.LLMOracle.GPT.prompt.Prompt;
import it.uniroma3.LLMOracle.GPT.prompt.PromptBuilder;
import it.uniroma3.LLMOracle.GPT.score.Score;
import it.uniroma3.LLMOracle.GPT.score.ScoreCalculator;
import it.uniroma3.LLMOracle.comando.Comando;
import it.uniroma3.LLMOracle.data.*;
import it.uniroma3.LLMOracle.utils.AddToMapList;
import it.uniroma3.LLMOracle.utils.Sampler;
import it.uniroma3.LLMOracle.utils.textDistance.CosineSimilarityText;
import it.uniroma3.LLMOracle.utils.textDistance.LevenshteinDistance;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class FewShotsBlocking implements Comando {

    //Si, sembra soffrire di mappite però non posso assegnare questa responsabilità a una specifica classe esistente
    private final Map<Blocco, List<Prompt>> blockPromptMap;

    private final Map<Blocco, List<Prompt>> blockTrainPromptMap;

    //contiene le risposte
    private final Map<Blocco, List<GPTQuery>> blockQueryMap;
    private final Map<Blocco, Score> blockScoreMap;
    private final Map<Prompt, Double> promptSimilarityMap;

    private final Map<Prompt, Integer> promptLevenshteinDistanceMap;

    private final Map<Blocco, Double> blockToSampledPromptCosineSimilarityMap;

    private final Map<Blocco, Double> blockToSampledPromptLevenshteinDistanceMap;

    public FewShotsBlocking() {
        this.blockPromptMap = new HashMap<>();
        this.blockTrainPromptMap = new HashMap<>();
        this.blockQueryMap = new HashMap<>();
        this.blockScoreMap = new HashMap<>();
        this.promptSimilarityMap = new HashMap<>();
        this.promptLevenshteinDistanceMap = new HashMap<>();
        this.blockToSampledPromptCosineSimilarityMap = new HashMap<>();
        this.blockToSampledPromptLevenshteinDistanceMap = new HashMap<>();
    }

    @Override
    public void esegui(Application application) throws IOException, InterruptedException {
        String datasetFolderPath = application.getAppProperties().getDatasetPath();
        String datasetPath = datasetFolderPath + "/nuovo/camera/oracle_ext_camera0_15.csv";
        String trainsetPath = datasetFolderPath + "/nuovo/camera/train_ext_camera0_15.csv";
        BufferedReader datasetReader = new BufferedReader(new FileReader(datasetPath));
        BufferedReader trainsetReader = new BufferedReader(new FileReader(trainsetPath));
        this.populatePromptMaps(datasetReader, this.blockPromptMap);
        this.populatePromptMaps(trainsetReader, this.blockTrainPromptMap);
        datasetReader.close();
        trainsetReader.close();
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.println("Vuoi fare few shot learning su tutto il dominio (0) o per blocco(1)?");
        int choice = keyboardScanner.nextInt();
        while (choice != 0 && choice != 1) {
            System.out.println("Inserisci un valore valido");
            System.out.println("Vuoi fare few shot learning su tutto il dominio (0) o per blocco(1)?");
            choice = keyboardScanner.nextInt();
        }
        if (choice == 0) {
            this.domainFewShotPrompting();
            this.makeExcelFile(choice, this.blockPromptMap.keySet());
        } else {
            this.blockFewShotPrompting();
            this.makeExcelFile(choice, this.blockTrainPromptMap.keySet());
        }

    }

    private void blockFewShotPrompting() throws InterruptedException {
        //Iteriamo solo sui blocchi contenuti nel file di training
        for(Blocco blocco : this.blockTrainPromptMap.keySet()){
            List<Prompt> trainingPromptList = this.blockTrainPromptMap.get(blocco);
            List<Prompt> sampledTrainingPromptList = new Sampler<Prompt>(3, trainingPromptList).sampleCollection();
            Chat fewShotsPromptingChat = new Chat();
            for(Prompt prompt : sampledTrainingPromptList){
                ClassificationPrompt classificationPrompt = (ClassificationPrompt) prompt;
                fewShotsPromptingChat.addUserChatMessage(classificationPrompt.getTextPrompt())
                        .addSystemChatAnswer(classificationPrompt.isPositive() ? "yes" : "no");
            }
            System.out.println(fewShotsPromptingChat);
            LLM gpt = new AzureGPT(LLM.STANDARD_INITIALIZATION_PROMPT, fewShotsPromptingChat);
            List<Prompt> promptList = this.blockPromptMap.get(blocco);
            List<Prompt> sampledPromptList = new Sampler<Prompt>(1000, promptList).sampleCollection();
            List<GPTQuery> answers = gpt.processPrompts(sampledPromptList, "gpt-35-turbo", 0);
            this.blockScoreMap.put(blocco, ScoreCalculator.calculateScore(answers));
            this.blockQueryMap.put(blocco, answers);
            double sumofsimilarity=0f;
            int sumoflevenshtein=0;
            for(Prompt sampled : sampledPromptList){
                sumoflevenshtein += this.promptLevenshteinDistanceMap.get(sampled);
                sumofsimilarity += this.promptSimilarityMap.get(sampled);
            }
            double averageSimilarity = sumofsimilarity/sampledPromptList.size();
            double averageLevenshteinDistance = (double) sumoflevenshtein / sampledPromptList.size();
            this.blockToSampledPromptCosineSimilarityMap.put(blocco,averageSimilarity);
            this.blockToSampledPromptLevenshteinDistanceMap.put(blocco, averageLevenshteinDistance);
        }
    }

    private void domainFewShotPrompting() throws InterruptedException {
        List<Prompt> learningPromptList = new ArrayList<>();
        //Estraiamo a caso 5 blocchi e da questi 5 blocchi estraiamo a caso 1 prompt per blocco
        Random random = new Random();
        List<Blocco> blockList = new ArrayList<>(this.blockTrainPromptMap.keySet());
        for (int i = 0; i < 3; i++) {
            int randomNumber = random.nextInt();
            randomNumber = Math.abs(randomNumber);
            randomNumber = randomNumber % this.blockTrainPromptMap.size();
            Blocco b = blockList.get(randomNumber);
            List<Prompt> promptList = this.blockTrainPromptMap.get(b);
            int anotherRandomNumber = random.nextInt();
            anotherRandomNumber = Math.abs(anotherRandomNumber);
            anotherRandomNumber = anotherRandomNumber % promptList.size();
            Prompt p = promptList.get(anotherRandomNumber);
            learningPromptList.add(p);
        }
        Chat fewShotsPromptingChat = new Chat();
        for(Prompt prompt : learningPromptList){
            ClassificationPrompt classificationPrompt = (ClassificationPrompt) prompt;
            fewShotsPromptingChat.addUserChatMessage(classificationPrompt.getTextPrompt())
                    .addSystemChatAnswer(classificationPrompt.isPositive() ? "yes" : "no");
        }
        System.out.println(fewShotsPromptingChat);
        String assistantContent = LLM.STANDARD_INITIALIZATION_PROMPT;
        LLM gpt = new AzureGPT(assistantContent, fewShotsPromptingChat);
        for(Blocco b : this.blockPromptMap.keySet()){
            Sampler<Prompt> promptSampler = new Sampler<>(1000,this.blockPromptMap.get(b));
            List<Prompt> sampledPrompt = promptSampler.sampleCollection();
            List<GPTQuery> answers = gpt.processPrompts(sampledPrompt, "gpt-35-turbo", 0);
            this.blockQueryMap.put(b,answers);
            this.blockScoreMap.put(b, ScoreCalculator.calculateScore(answers));
            double sumofsimilarity=0f;
            int sumoflevenshtein=0;
            for(Prompt sampled : sampledPrompt){
                sumoflevenshtein += this.promptLevenshteinDistanceMap.get(sampled);
                sumofsimilarity += this.promptSimilarityMap.get(sampled);
            }
            double averageSimilarity = sumofsimilarity/sampledPrompt.size();
            double averageLevenshteinDistance = (double) sumoflevenshtein / sampledPrompt.size();
            this.blockToSampledPromptCosineSimilarityMap.put(b,averageSimilarity);
            this.blockToSampledPromptLevenshteinDistanceMap.put(b, averageLevenshteinDistance);
        }
    }

    private void populatePromptMaps(BufferedReader reader, Map<Blocco, List<Prompt>> blockPromptMap) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(";");
            Blocco b = new Blocco(columns[0]);
            String textA = columns[1].toLowerCase();
            String textB = columns[2].toLowerCase();
            ClassificationPrompt prompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(textA, textB, Boolean.parseBoolean(columns[3]));
            this.promptLevenshteinDistanceMap.put(prompt, LevenshteinDistance.calculate(textA, textB));
            this.promptSimilarityMap.put(prompt, CosineSimilarityText.apply(textA, textB));
            AddToMapList.addToMapList(b, prompt, blockPromptMap);
        }
    }

    private void makeExcelFile(int choice, Set<Blocco> involvedBlocks) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("OpenTriage blocking");
        XSSFRow row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Blocco");
        row0.createCell(1).setCellValue("TP");
        row0.createCell(2).setCellValue("TN");
        row0.createCell(3).setCellValue("FP");
        row0.createCell(4).setCellValue("FN");
        row0.createCell(5).setCellValue("avg cos similarity");
        row0.createCell(6).setCellValue("avg levenshtein");
        row0.createCell(7).setCellValue("F1");
        row0.createCell(8).setCellValue("MCC");
        for(Blocco blocco : involvedBlocks){
            XSSFSheet blockSheet = (XSSFSheet) workbook.createSheet(blocco.getId());
            XSSFRow blockSheetHeaderRow= blockSheet.createRow(0);
            blockSheetHeaderRow.createCell(0).setCellValue("Prompt");
            blockSheetHeaderRow.createCell(1).setCellValue("Risposta");
            blockSheetHeaderRow.createCell(2).setCellValue("Risposta attesa");
            //Fixare per few shots per blocco. Dato che se iteriamo su tutti i blocchi, non è detto che troviamo le query
            //per tutti i blocchi dato che in few shots per blocco iteriamo sui blocchi di test.
            for(GPTQuery query : this.blockQueryMap.get(blocco)){
                XSSFRow blockSheetRow = blockSheet.createRow(blockSheet.getLastRowNum()+1);
                blockSheetRow.createCell(0).setCellValue(query.getPrompt().getTextPrompt());
                blockSheetRow.createCell(1).setCellValue(query.getRisposta());
                blockSheetRow.createCell(2).setCellValue(((ClassificationPrompt)query.getPrompt()).isPositive() ? "yes" : "no");
            }
            //Salviamo i prompt effettuati

            XSSFRow blockRow = sheet.createRow(sheet.getLastRowNum()+1);
            Score blockScore = this.blockScoreMap.get(blocco);
            blockRow.createCell(0).setCellValue(blocco.getId());
            blockRow.createCell(1).setCellValue(blockScore.getTP());
            blockRow.createCell(2).setCellValue(blockScore.getTN());
            blockRow.createCell(3).setCellValue(blockScore.getFP());
            blockRow.createCell(4).setCellValue(blockScore.getFN());
            blockRow.createCell(5).setCellValue(this.blockToSampledPromptCosineSimilarityMap.get(blocco));
            blockRow.createCell(6).setCellValue(this.blockToSampledPromptLevenshteinDistanceMap.get(blocco));
            blockRow.createCell(7).setCellValue(blockScore.getFScore());
            blockRow.createCell(8).setCellValue(blockScore.getMCC());
        }

        //Aggiungiamo la data al nome
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        String dateAndTime = date +"_"+ time.getHour()+ "_"+ time.getMinute();
        String type = choice == 0 ? "domain" : "block";
        FileOutputStream fileOut = new FileOutputStream("./spreadsheets/blocking/"+"fewshotsblocking-"+type+"-"+dateAndTime+".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}

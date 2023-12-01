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
import it.uniroma3.LLMOracle.GPT.segmentazione.Segmenter;
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

    private int choice;

    private int cutoffChoice;

    private int charsPerDescription;
    private int trainingPromptsAmount;

    public FewShotsBlocking() {
        this.blockPromptMap = new HashMap<>();
        this.blockTrainPromptMap = new HashMap<>();
        this.blockQueryMap = new HashMap<>();
        this.blockScoreMap = new HashMap<>();
        this.promptSimilarityMap = new HashMap<>();
        this.promptLevenshteinDistanceMap = new HashMap<>();
        this.blockToSampledPromptCosineSimilarityMap = new HashMap<>();
        this.blockToSampledPromptLevenshteinDistanceMap = new HashMap<>();
        this.trainingPromptsAmount = 0;
    }

    @Override
    public void esegui(Application application) throws IOException, InterruptedException {
        String datasetFolderPath = application.getAppProperties().getDatasetPath();
        /*String datasetPath = datasetFolderPath + "/nuovo/camera/oracle_ext_camera0_15.csv";
        String trainsetPath = datasetFolderPath + "/nuovo/camera/train_ext_camera0_15.csv";*/
        String datasetPath = datasetFolderPath + "/nuovo/camera/oracle_topAttr_camera0_15.csv";
        String trainsetPath = datasetFolderPath + "/nuovo/camera/train_topAttr_camera0_15.csv";
        BufferedReader datasetReader = new BufferedReader(new FileReader(datasetPath));
        BufferedReader trainsetReader = new BufferedReader(new FileReader(trainsetPath));
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.print("Vuoi eseguire un cutoff ai testi dei prompt? (0 no, 1 si): ");
        this.cutoffChoice = keyboardScanner.nextInt();
        while (cutoffChoice < 0 || cutoffChoice > 1) {
            System.out.println("Inserisci un valore valido");
            System.out.print("Vuoi eseguire un cutoff ai testi dei prompt? (0 no, 1 si): ");
            cutoffChoice = keyboardScanner.nextInt();
        }

        if (cutoffChoice == 1) {
            System.out.print("Inserisci il numero di caratteri massimi per prompt: ");
            this.charsPerDescription = keyboardScanner.nextInt();
            if(this.charsPerDescription < 1){
                System.out.println("Inserisci un valore valido");
                System.out.print("Inserisci il numero di token per prompt: ");
                this.charsPerDescription = keyboardScanner.nextInt();
            }
            this.populatePromptMaps(datasetReader, this.blockPromptMap, this.charsPerDescription);
            this.populatePromptMaps(trainsetReader, this.blockTrainPromptMap, this.charsPerDescription);
        } else {
            this.populatePromptMaps(datasetReader, this.blockPromptMap);
            this.populatePromptMaps(trainsetReader, this.blockTrainPromptMap);
        }
        System.out.print("Scegli la quantità di k-shots learning prompts: ");
        trainingPromptsAmount = keyboardScanner.nextInt();
        while (trainingPromptsAmount < 0) {
            System.out.println("Inserisci un valore valido");
            System.out.print("Scegli la quantità di k-shots learning prompts: ");
            trainingPromptsAmount = keyboardScanner.nextInt();
        }
        datasetReader.close();
        trainsetReader.close();

        if(trainingPromptsAmount == 0){
            String choiceString = "Vuoi fare train (0) o oracle (1)?: ";
            System.out.print(choiceString);
            this.choice = keyboardScanner.nextInt();
            while (choice < 0 || choice > 1) {
                System.out.println("Inserisci un valore valido");
                System.out.print(choiceString);
                choice = keyboardScanner.nextInt();
            }
            if (choice == 0) {
                //usiamo train per fare training
                this.domainFewShotPrompting(this.blockTrainPromptMap.keySet(), this.blockTrainPromptMap, this.blockPromptMap);
                this.makeExcelFile(this.blockPromptMap.keySet());
            } else {
                //usiamo oracle per fare training
                this.domainFewShotPrompting(this.blockPromptMap.keySet(), this.blockPromptMap, this.blockPromptMap);
                this.makeExcelFile(this.blockPromptMap.keySet());
            }
        }else{
            String choiceString = "Vuoi fare train-oracle domain(0) oracle-oracle domain(1) train-oracle block(2) oracle-oracle block(3)?: ";
            System.out.print(choiceString);
            this.choice = keyboardScanner.nextInt();
            while (choice < 0 || choice > 3) {
                System.out.println("Inserisci un valore valido");
                System.out.print(choiceString);
                choice = keyboardScanner.nextInt();
            }
            if (choice == 0) {
                //usiamo train per fare training
                this.domainFewShotPrompting(this.blockTrainPromptMap.keySet(), this.blockTrainPromptMap, this.blockPromptMap);
                this.makeExcelFile(this.blockPromptMap.keySet());
            } else if (choice == 1) {
                //usiamo oracle per fare training
                this.domainFewShotPrompting(this.blockPromptMap.keySet(), this.blockPromptMap, this.blockPromptMap);
                this.makeExcelFile(this.blockPromptMap.keySet());
            } else if (choice == 2) {
                //Usiamo i train per fare few shot learning e interroghiamo su tutti i blocchi
                this.blockFewShotPrompting(this.blockTrainPromptMap.keySet(), this.blockTrainPromptMap, this.blockPromptMap, trainingPromptsAmount);
                this.makeExcelFile(this.blockTrainPromptMap.keySet());
            } else {
                //usiamo oracle per fare training
                this.blockFewShotPrompting(this.blockPromptMap.keySet(), this.blockPromptMap, this.blockPromptMap, trainingPromptsAmount);
                this.makeExcelFile(this.blockPromptMap.keySet());
            }
        }
    }


    private void blockFewShotPrompting(Set<Blocco> trainingBlocksSet, Map<Blocco, List<Prompt>> block2PromptTrainingMap, Map<Blocco, List<Prompt>> block2PromptTestMap, int trainingPromptAmount) throws InterruptedException {
        //Iteriamo solo sui blocchi contenuti nel file di training
        for (Blocco blocco : trainingBlocksSet) {
            List<Prompt> trainingPromptList = block2PromptTrainingMap.get(blocco);
            System.out.println("Training prompt list size: " + trainingPromptList.size());
            int promptPositivi = trainingPromptAmount / 2;
            int promptNegativi = (trainingPromptAmount / 2) + (trainingPromptAmount % 2);
            int promptPositiviCreati = 0;
            int promptNegativiCreati = 0;
            List<Prompt> sampledTrainingPromptList = new ArrayList<>();
            if(trainingPromptList.size() < trainingPromptAmount){
                sampledTrainingPromptList.addAll(trainingPromptList);
            }else {
                List<Prompt> temp = new ArrayList<>(trainingPromptList);
                Collections.shuffle(temp);
                while ((promptPositiviCreati != promptPositivi || promptNegativiCreati != promptNegativi) && !temp.isEmpty()) {
                    //estraiamo i primi n prompt
                    ClassificationPrompt promptEstratto = (ClassificationPrompt) temp.remove(0);
                    if ((promptEstratto).isPositive() && promptPositiviCreati != promptPositivi) {
                        sampledTrainingPromptList.add(promptEstratto);
                        promptPositiviCreati++;
                    } else if (!( promptEstratto).isPositive() && promptNegativiCreati != promptNegativi) {
                        sampledTrainingPromptList.add(promptEstratto);
                        promptNegativiCreati++;
                    }
                }
            }
            Chat fewShotsPromptingChat = new Chat();
            for (Prompt prompt : sampledTrainingPromptList) {
                ClassificationPrompt classificationPrompt = (ClassificationPrompt) prompt;
                fewShotsPromptingChat.addUserChatMessage(classificationPrompt.getTextPrompt())
                        .addSystemChatAnswer(classificationPrompt.isPositive() ? "yes" : "no");
            }
            System.out.println(fewShotsPromptingChat);
            System.out.println(fewShotsPromptingChat.toJson());
            //LLM gpt = new AzureGPT(LLM.STANDARD_INITIALIZATION_PROMPT, fewShotsPromptingChat);
            LLM gpt = new AzureGPT(LLM.STANDARD_INITIALIZATION_PROMPT);
            List<Prompt> promptList = block2PromptTestMap.get(blocco);
            List<Prompt> sampledPromptList = new Sampler<>(1000, promptList).sampleCollection();
            List<GPTQuery> answers = gpt.processPrompts(sampledPromptList, "gpt-35-turbo", 500);
            this.blockScoreMap.put(blocco, ScoreCalculator.calculateScore(answers));
            this.blockQueryMap.put(blocco, answers);
            double sumofsimilarity = 0f;
            int sumoflevenshtein = 0;
            for (Prompt sampled : sampledPromptList) {
                sumoflevenshtein += this.promptLevenshteinDistanceMap.get(sampled);
                sumofsimilarity += this.promptSimilarityMap.get(sampled);
            }
            double averageSimilarity = sumofsimilarity / sampledPromptList.size();
            double averageLevenshteinDistance = (double) sumoflevenshtein / sampledPromptList.size();
            this.blockToSampledPromptCosineSimilarityMap.put(blocco, averageSimilarity);
            this.blockToSampledPromptLevenshteinDistanceMap.put(blocco, averageLevenshteinDistance);
        }
    }

    private void domainFewShotPrompting(Set<Blocco> trainingBlockSet, Map<Blocco, List<Prompt>> block2PromptTrainingMap, Map<Blocco, List<Prompt>> block2PromptTestMap) throws InterruptedException {
        List<Prompt> learningPromptList = new ArrayList<>();
        int promptPositivi = this.trainingPromptsAmount / 2;
        int promptNegativi = (trainingPromptsAmount / 2) + (trainingPromptsAmount % 2);
        int promptPositiviCreati = 0;
        int promptNegativiCreati = 0;
        List<Prompt> tempList = new ArrayList<>();
        //Metto tutti i prompt provenienti da tutti i blocchi temporanea in una lista
        for(Blocco b: trainingBlockSet){
            tempList.addAll(block2PromptTrainingMap.get(b));
        }
        //shuffle tempList
        Collections.shuffle(tempList);
        while ((promptPositiviCreati != promptPositivi || promptNegativiCreati != promptNegativi) && !tempList.isEmpty()) {
            //estraggo a caso un prompt
            ClassificationPrompt promptEstratto = (ClassificationPrompt) tempList.remove(0);
            if((promptEstratto).isPositive() && promptPositiviCreati != promptPositivi) {
                learningPromptList.add(promptEstratto);
                promptPositiviCreati++;
            }else if(!(promptEstratto).isPositive() && promptNegativiCreati != promptNegativi){
                learningPromptList.add(promptEstratto);
                promptNegativiCreati++;
            }
        }

        if(tempList.isEmpty()){
            System.out.println("Attenzione, i prompt estratti per fare few shots potrebbero essere sbilanciati");
        }

        Chat fewShotsPromptingChat = new Chat();
        for (Prompt prompt : learningPromptList) {
            ClassificationPrompt classificationPrompt = (ClassificationPrompt) prompt;
            fewShotsPromptingChat.addUserChatMessage(classificationPrompt.getTextPrompt())
                    .addSystemChatAnswer(classificationPrompt.isPositive() ? "yes" : "no");
        }
        System.out.println(fewShotsPromptingChat);
        System.out.println(fewShotsPromptingChat.toJson());
        String assistantContent = LLM.STANDARD_INITIALIZATION_PROMPT;
        LLM gpt = new AzureGPT(assistantContent, fewShotsPromptingChat);
        //LLM gpt = new AzureGPT(assistantContent);
        for (Blocco b : block2PromptTestMap.keySet()) {
            Sampler<Prompt> promptSampler = new Sampler<>(1000, block2PromptTestMap.get(b));
            List<Prompt> sampledPrompt = promptSampler.sampleCollection();
            List<GPTQuery> answers = gpt.processPrompts(sampledPrompt, "gpt-35-turbo", 0);
            this.blockQueryMap.put(b, answers);
            this.blockScoreMap.put(b, ScoreCalculator.calculateScore(answers));
            double sumofsimilarity = 0f;
            int sumoflevenshtein = 0;
            for (Prompt sampled : sampledPrompt) {
                sumoflevenshtein += this.promptLevenshteinDistanceMap.get(sampled);
                sumofsimilarity += this.promptSimilarityMap.get(sampled);
            }
            double averageSimilarity = sumofsimilarity / sampledPrompt.size();
            double averageLevenshteinDistance = (double) sumoflevenshtein / sampledPrompt.size();
            this.blockToSampledPromptCosineSimilarityMap.put(b, averageSimilarity);
            this.blockToSampledPromptLevenshteinDistanceMap.put(b, averageLevenshteinDistance);
        }
    }

    private void populatePromptMaps(BufferedReader reader, Map<Blocco, List<Prompt>> blockPromptMap) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(";");
            Blocco b = new Blocco(columns[0]);
            String textA = columns[1].toLowerCase().replaceAll("\"", "\\\"");
            String textB = columns[2].toLowerCase().replaceAll("\"", "\\\"");
            ClassificationPrompt prompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(textA, textB, Boolean.parseBoolean(columns[3]));
            this.promptLevenshteinDistanceMap.put(prompt, LevenshteinDistance.calculate(textA, textB));
            this.promptSimilarityMap.put(prompt, CosineSimilarityText.apply(textA, textB));
            AddToMapList.addToMapList(b, prompt, blockPromptMap);
        }
    }

    private void populatePromptMaps(BufferedReader reader, Map<Blocco, List<Prompt>> blockPromptMap, int tokerPerDescription) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(";");
            Blocco b = new Blocco(columns[0]);
            String textA = columns[1].toLowerCase().replaceAll("\"", "\\\"");
            String textB = columns[2].toLowerCase().replaceAll("\"", "\\\"");
            String cutTextA = textA.substring(0, Math.min(textA.length(), tokerPerDescription));
            String cutTextB = textB.substring(0, Math.min(textB.length(), tokerPerDescription));
            ClassificationPrompt prompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(cutTextA, cutTextB, Boolean.parseBoolean(columns[3]));
            this.promptLevenshteinDistanceMap.put(prompt, LevenshteinDistance.calculate(cutTextA, cutTextB));
            this.promptSimilarityMap.put(prompt, CosineSimilarityText.apply(cutTextA, cutTextB));
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
        row0.createCell(5).setCellValue("avg cos similarity");
        row0.createCell(6).setCellValue("avg levenshtein");
        row0.createCell(7).setCellValue("F1");
        row0.createCell(8).setCellValue("MCC");
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
            blockRow.createCell(5).setCellValue(this.blockToSampledPromptCosineSimilarityMap.get(blocco));
            blockRow.createCell(6).setCellValue(this.blockToSampledPromptLevenshteinDistanceMap.get(blocco));
            blockRow.createCell(7).setCellValue(blockScore.getFScore());
            blockRow.createCell(8).setCellValue(blockScore.getMCC());
        }

        //Aggiungiamo la data al nome
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        String dateAndTime = date + "_" + time.getHour() + "_" + time.getMinute();
        String type = "";
        if (choice == 0) {
            type = "train-oracle-domain";
        } else if (choice == 1) {
            type = "oracle-oracle-domain";
        } else if (choice == 2) {
            type = "train-oracle-block";
        } else {
            type = "oracle-oracle-block";
        }
        String cutoff = "";
        String cutoffAt = "";
        if (cutoffChoice == 1) {
            cutoff = "cutoff";
            cutoffAt += this.charsPerDescription;
        } else {
            cutoff = "nocutoff";
        }
        FileOutputStream fileOut = new FileOutputStream("./spreadsheets/blocking/" + "fewshotsblocking-" + type+"-"+ cutoff+"at-"+ cutoffAt + "-"+"-"+this.trainingPromptsAmount+"shots-" + dateAndTime + ".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }



}

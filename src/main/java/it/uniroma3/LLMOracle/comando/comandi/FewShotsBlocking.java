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
import it.uniroma3.LLMOracle.GPT.tokenizer.Tokenizer;
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

    private int tokensPerDescription;

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
        Scanner keyboardScanner = new Scanner(System.in);
        System.out.println("Vuoi eseguire un cutoff ai testi dei prompt? (0 no, 1 si)");
        this.cutoffChoice = keyboardScanner.nextInt();
        while (cutoffChoice < 0 || cutoffChoice > 1) {
            System.out.println("Inserisci un valore valido");
            System.out.println("Vuoi eseguire un cutoff ai testi dei prompt? (0 no, 1 si)");
            cutoffChoice = keyboardScanner.nextInt();
        }
        int trainingPromptsAmount;
        if (cutoffChoice == 1) {
            System.out.println("Inserisci il numero di token massimi per prompt");
            this.tokensPerDescription = keyboardScanner.nextInt();
            if (this.tokensPerDescription < 1) {
                System.out.println("Inserisci un valore valido");
                System.out.println("Inserisci il numero di token massimi per prompt");
                this.tokensPerDescription = keyboardScanner.nextInt();
            }
            trainingPromptsAmount = 10;
        }else{
            trainingPromptsAmount = 3;
        }
        this.populatePromptMaps(datasetReader, this.blockPromptMap);
        this.populatePromptMaps(trainsetReader, this.blockTrainPromptMap);
        datasetReader.close();
        trainsetReader.close();
        String choiceString = "Vuoi fare train-oracle domain(0) oracle-oracle domain(1) train-oracle block(2) oracle-oracle block(3)?";
        System.out.println(choiceString);
        this.choice = keyboardScanner.nextInt();
        while (choice < 0 || choice > 3) {
            System.out.println("Inserisci un valore valido");
            System.out.println(choiceString);
            choice = keyboardScanner.nextInt();
        }
        if (choice == 0) {
            //usiamo train per fare training
            this.domainFewShotPrompting(this.blockTrainPromptMap.keySet(), this.blockTrainPromptMap, this.blockPromptMap, trainingPromptsAmount);
            this.makeExcelFile(this.blockPromptMap.keySet());
        } else if (choice == 1) {
            //usiamo oracle per fare training
            this.domainFewShotPrompting(this.blockPromptMap.keySet(), this.blockPromptMap, this.blockPromptMap, trainingPromptsAmount);
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


    public void blockFewShotPrompting(Set<Blocco> trainingBlocksSet, Map<Blocco, List<Prompt>> block2PromptTrainingMap, Map<Blocco, List<Prompt>> block2PromptTestMap, int trainingPromptAmount) throws InterruptedException {
        //Iteriamo solo sui blocchi contenuti nel file di training
        for (Blocco blocco : trainingBlocksSet) {
            List<Prompt> trainingPromptList = block2PromptTrainingMap.get(blocco);
            int promptPositivi = trainingPromptAmount / 2;
            int promptNegativi = (trainingPromptAmount / 2) + (trainingPromptAmount % 2);
            int promptPositiviCreati = 0;
            int promptNegativiCreati = 0;
            int maxRetries = 100;

            List<Prompt> sampledTrainingPromptList = new ArrayList<>();
            while ((promptPositiviCreati != promptPositivi || promptNegativiCreati != promptNegativi) && maxRetries > 0) {
                /*System.out.println("test");
                System.out.println("maxRetries: "+maxRetries);*/
                Prompt promptEstratto = new Sampler<Prompt>(1, trainingPromptList).sampleCollection().get(0);
                boolean trovato = false;
                if (((ClassificationPrompt) promptEstratto).isPositive() && promptPositiviCreati != promptPositivi) {
                    sampledTrainingPromptList.add(promptEstratto);
                    promptPositiviCreati++;
                    trovato = true;
                }
                if (!((ClassificationPrompt) promptEstratto).isPositive() && promptNegativiCreati != promptNegativi) {
                    sampledTrainingPromptList.add(promptEstratto);
                    promptNegativiCreati++;
                    trovato = true;
                }
                if (!trovato) {
                    maxRetries--;
                }
            }
            Chat fewShotsPromptingChat = new Chat();
            for (Prompt prompt : sampledTrainingPromptList) {
                /*String textPrompt = String.copyValueOf(prompt.getTextPrompt().toCharArray());
                if (this.cutoffChoice == 1) {
                    Tokenizer tokenizer = new Tokenizer(textPrompt);
                    textPrompt = tokenizer.getNextNTokens(this.tokensPerDescription);
                }*/
                if(this.cutoffChoice == 1){
                    prompt = this.shortenPrompt((ClassificationPrompt) prompt, this.tokensPerDescription);

                }
                ClassificationPrompt classificationPrompt = (ClassificationPrompt) prompt;
                fewShotsPromptingChat.addUserChatMessage(classificationPrompt.getTextPrompt())
                        .addSystemChatAnswer(classificationPrompt.isPositive() ? "yes" : "no");
            }
            System.out.println(fewShotsPromptingChat);
            LLM gpt = new AzureGPT(LLM.STANDARD_INITIALIZATION_PROMPT, fewShotsPromptingChat);
            List<Prompt> promptList = block2PromptTestMap.get(blocco);
            List<Prompt> sampledPromptList = new Sampler<>(1000, promptList).sampleCollection();

            if(this.cutoffChoice == 1){
                List<Prompt> shortenedPromptList = new ArrayList<>();
                for(Prompt p : sampledPromptList){
                    shortenedPromptList.add(this.shortenPrompt((ClassificationPrompt) p, this.tokensPerDescription));
                }
                sampledPromptList = shortenedPromptList;
            }

            List<GPTQuery> answers = gpt.processPrompts(sampledPromptList, "gpt-35-turbo", 0);
            this.blockScoreMap.put(blocco, ScoreCalculator.calculateScore(answers));
            this.blockQueryMap.put(blocco, answers);
            double sumofsimilarity = 0f;
            int sumoflevenshtein = 0;
            try {
                for (Prompt sampled : sampledPromptList) {
                    sumoflevenshtein += this.promptLevenshteinDistanceMap.get(sampled);
                    sumofsimilarity += this.promptSimilarityMap.get(sampled);
                }
            }catch(Exception e){
                System.err.println("Exception while calculating similarity and levenshtein distance");
            }
            double averageSimilarity = sumofsimilarity / sampledPromptList.size();
            double averageLevenshteinDistance = (double) sumoflevenshtein / sampledPromptList.size();
            this.blockToSampledPromptCosineSimilarityMap.put(blocco, averageSimilarity);
            this.blockToSampledPromptLevenshteinDistanceMap.put(blocco, averageLevenshteinDistance);
        }
    }

    public void domainFewShotPrompting(Set<Blocco> trainingBlockSet, Map<Blocco, List<Prompt>> block2PromptTrainingMap, Map<Blocco, List<Prompt>> block2PromptTestMap, int trainingPromptAmount) throws InterruptedException {
        List<Prompt> learningPromptList = new ArrayList<>();
        int promptPositivi = trainingPromptAmount / 2;
        int promptNegativi = (trainingPromptAmount / 2) + (trainingPromptAmount % 2);
        int promptPositiviCreati = 0;
        int promptNegativiCreati = 0;
        int maxRetries = 100;
        while ((promptPositiviCreati != promptPositivi || promptNegativiCreati != promptNegativi) && maxRetries > 0) {
            Blocco bloccoEstratto = new Sampler<Blocco>(1, trainingBlockSet).sampleCollection().get(0);
            List<Prompt> promptList = block2PromptTrainingMap.get(bloccoEstratto);
            Prompt promptEstratto = new Sampler<Prompt>(1, promptList).sampleCollection().get(0);
            boolean trovato = false;
            if (((ClassificationPrompt) promptEstratto).isPositive() && promptPositiviCreati != promptPositivi) {
                learningPromptList.add(promptEstratto);
                promptPositiviCreati++;
                trovato = true;
            }
            if (!((ClassificationPrompt) promptEstratto).isPositive() && promptNegativiCreati != promptNegativi) {
                learningPromptList.add(promptEstratto);
                promptNegativiCreati++;
                trovato = true;
            }
            if (!trovato) {
                maxRetries--;
            }
        }
        Chat fewShotsPromptingChat = new Chat();
        for (Prompt prompt : learningPromptList) {
            String textPrompt = String.copyValueOf(prompt.getTextPrompt().toCharArray());
            if(this.cutoffChoice == 1){
                prompt = this.shortenPrompt((ClassificationPrompt) prompt, this.tokensPerDescription);
            }
            ClassificationPrompt classificationPrompt = (ClassificationPrompt) prompt;
            fewShotsPromptingChat.addUserChatMessage(classificationPrompt.getTextPrompt())
                    .addSystemChatAnswer(classificationPrompt.isPositive() ? "yes" : "no");
        }
        System.out.println(fewShotsPromptingChat);
        String assistantContent = LLM.STANDARD_INITIALIZATION_PROMPT;
        LLM gpt = new AzureGPT(assistantContent, fewShotsPromptingChat);
        for (Blocco b : block2PromptTestMap.keySet()) {
            Sampler<Prompt> promptSampler = new Sampler<>(1000, block2PromptTestMap.get(b));
            List<Prompt> sampledPrompt = promptSampler.sampleCollection();
            if(this.cutoffChoice == 1){
                List<Prompt> shortenedPromptList = new ArrayList<>();
                for(Prompt p : sampledPrompt){
                    ClassificationPrompt pr = this.shortenPrompt((ClassificationPrompt) p, this.tokensPerDescription);
                    shortenedPromptList.add(pr);
                }
                sampledPrompt = shortenedPromptList;
            }
            List<GPTQuery> answers = gpt.processPrompts(sampledPrompt, "gpt-35-turbo", 0);
            this.blockQueryMap.put(b, answers);
            this.blockScoreMap.put(b, ScoreCalculator.calculateScore(answers));
            double sumofsimilarity = 0f;
            int sumoflevenshtein = 0;
            try {
                for (Prompt sampled : sampledPrompt) {
                    sumoflevenshtein += this.promptLevenshteinDistanceMap.get(sampled);
                    sumofsimilarity += this.promptSimilarityMap.get(sampled);
                }
            }catch(Exception e){
                System.err.println("Exception while calculating similarity and levenshtein distance");
            }
            double averageSimilarity = sumofsimilarity / sampledPrompt.size();
            double averageLevenshteinDistance = (double) sumoflevenshtein / sampledPrompt.size();
            this.blockToSampledPromptCosineSimilarityMap.put(b, averageSimilarity);
            this.blockToSampledPromptLevenshteinDistanceMap.put(b, averageLevenshteinDistance);
        }
    }

    public ClassificationPrompt shortenPrompt(ClassificationPrompt prompt, int tokensPerDescription) {
        String textPrompt = String.copyValueOf(prompt.getTextPrompt().toCharArray());
        String[] firstSecond = textPrompt.split("second: ");
        String first = firstSecond[0].replace("first: ", "");
        String second = firstSecond[1];
        Tokenizer t1 = new Tokenizer(first);
        Tokenizer t2 = new Tokenizer(second);
        String firstShortened = t1.getNextNTokens(tokensPerDescription);
        String secondShortened = t2.getNextNTokens(tokensPerDescription);
        this.promptLevenshteinDistanceMap.put(prompt, LevenshteinDistance.calculate(firstShortened, secondShortened));
        this.promptSimilarityMap.put(prompt, CosineSimilarityText.apply(firstShortened, secondShortened));
        return (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(firstShortened, secondShortened, prompt.isPositive());
    }

    public void populatePromptMaps(BufferedReader reader, Map<Blocco, List<Prompt>> blockPromptMap) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(";");
            Blocco b = new Blocco(columns[0]);
            String textA = columns[1].toLowerCase().replace("\"", "\\\"");
            String textB = columns[2].toLowerCase().replace("\"", "\\\"");
            ClassificationPrompt prompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(textA, textB, Boolean.parseBoolean(columns[3]));
            this.promptLevenshteinDistanceMap.put(prompt, LevenshteinDistance.calculate(textA, textB));
            this.promptSimilarityMap.put(prompt, CosineSimilarityText.apply(textA, textB));
            AddToMapList.addToMapList(b, prompt, blockPromptMap);
        }
    }

    public void populatePromptMaps(BufferedReader reader, Map<Blocco, List<Prompt>> blockPromptMap, int tokerPerDescription) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(";");
            Blocco b = new Blocco(columns[0]);
            String textA = columns[1].toLowerCase();
            String textB = columns[2].toLowerCase();
            String cutTextA = textA.substring(0, Math.min(textA.length(), tokerPerDescription));
            String cutTextB = textB.substring(0, Math.min(textB.length(), tokerPerDescription));
            ClassificationPrompt prompt = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(cutTextA, cutTextB, Boolean.parseBoolean(columns[3]));
            this.promptLevenshteinDistanceMap.put(prompt, LevenshteinDistance.calculate(cutTextA, cutTextB));
            this.promptSimilarityMap.put(prompt, CosineSimilarityText.apply(cutTextA, cutTextB));
            AddToMapList.addToMapList(b, prompt, blockPromptMap);
        }
    }

    public void makeExcelFile(Set<Blocco> involvedBlocks) throws IOException {
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
        // addestramentoSu-InterrogazioneSu-addestramentoDaTuttoDominio/PerBlocco
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
            cutoffAt += this.tokensPerDescription;
        } else {
            cutoff = "nocutoff";
        }
        FileOutputStream fileOut = new FileOutputStream("./spreadsheets/blocking/" + "fewshotsblocking-" + type + "-" + cutoff + "at-" + cutoffAt + "-" + dateAndTime + ".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}

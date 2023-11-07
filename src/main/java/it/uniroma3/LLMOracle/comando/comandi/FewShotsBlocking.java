package it.uniroma3.LLMOracle.comando.comandi;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.GPT.GPTQuery;
import it.uniroma3.LLMOracle.GPT.prompt.Prompt;
import it.uniroma3.LLMOracle.GPT.prompt.PromptBuilder;
import it.uniroma3.LLMOracle.GPT.score.Score;
import it.uniroma3.LLMOracle.comando.Comando;
import it.uniroma3.LLMOracle.data.*;
import it.uniroma3.LLMOracle.utils.AddToMapList;
import it.uniroma3.LLMOracle.utils.textDistance.CosineSimilarityText;
import it.uniroma3.LLMOracle.utils.textDistance.LevenshteinDistance;

import java.io.*;
import java.util.*;

public class FewShotsBlocking implements Comando {

    //Si, sembra soffrire di mappite però non posso assegnare questa responsabilità a una specifica classe esistente
    private final Map<Blocco, List<Prompt>> blockPromptMap;

    private final Map<Blocco, List<Prompt>> blockTrainPromptMap;

    private final Map<Blocco, List<GPTQuery>> blockQueryMap;
    private final Map<Blocco, Score> blockScoreMap;
    private final Map<Prompt, Double> promptSimilarityMap;

    private final Map<Prompt, Integer> promptLevenshteinDistanceMap;

    private final Map<Blocco, Double> blockAverageTextCosineSimilarityMap;

    private final Map<Blocco, Double> blockLevenshteinDistanceMap;

    public FewShotsBlocking() {
        this.blockPromptMap = new HashMap<>();
        this.blockTrainPromptMap = new HashMap<>();
        this.blockQueryMap = new HashMap<>();
        this.blockScoreMap = new HashMap<>();
        this.promptSimilarityMap = new HashMap<>();
        this.promptLevenshteinDistanceMap = new HashMap<>();
        this.blockAverageTextCosineSimilarityMap = new HashMap<>();
        this.blockLevenshteinDistanceMap = new HashMap<>();
    }

    @Override
    public void esegui(Application application) throws IOException {
        String datasetFolderPath = application.getAppProperties().getDatasetPath();
        String datasetPath = datasetFolderPath + "/nuovo/camera/oracle_ext_camera0_15.csv";
        String trainsetPath = datasetFolderPath + "/nuovo/camera/train_ext_camera0_15.csv";
        BufferedReader datasetReader = new BufferedReader(new FileReader(datasetPath));
        BufferedReader trainsetReader = new BufferedReader(new FileReader(trainsetPath));
        this.populatePromptMaps(datasetReader, this.blockPromptMap);
        this.populatePromptMaps(trainsetReader, this.blockTrainPromptMap);
        datasetReader.close();
        trainsetReader.close();
        //System.out.println(blockPromptMap);
        //System.out.println(blockTrainPromptMap);
        //Chiediamo all'utente se vuole fare few shot learning su tutto il dominio o per blocco
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
        } else {
            this.blockFewShotPrompting();
        }

    }

    private void domainFewShotPrompting() {
        List<Prompt> learningPromptList = new ArrayList<>();
        //Estraiamo a caso 5 blocchi e da questi 5 blocchi estraiamo a caso 1 prompt per blocco
        Random random = new Random();
        List<Blocco> blockList = new ArrayList<>(this.blockPromptMap.keySet());
        for (int i = 0; i < 5; i++) {
            int randomNumber = random.nextInt();
            randomNumber = Math.abs(randomNumber);
            randomNumber = randomNumber % this.blockPromptMap.size();
            Blocco b = blockList.get(randomNumber);
            List<Prompt> promptList = this.blockPromptMap.get(b);
            int anotherRandomNumber = random.nextInt();
            anotherRandomNumber = Math.abs(anotherRandomNumber);
            anotherRandomNumber = anotherRandomNumber % promptList.size();
            Prompt p = promptList.get(anotherRandomNumber);
            learningPromptList.add(p);
        }
        System.out.println(learningPromptList);
    }

    private void blockFewShotPrompting() {

        for(Blocco block : this.blockPromptMap.keySet()){
            //Calcoliamo la media della similarità tra i testi dei prompt del blocco
            double averageTextCosineSimilarity = this.calculateAverageBlockCosineSimilarity(block);
            this.blockAverageTextCosineSimilarityMap.put(block, averageTextCosineSimilarity);
            //Calcoliamo la media della similarità tra i testi dei prompt del blocco
            double averageLevenshteinDistance = this.calculateAverageBlockLevenshteinDistance(block);
            this.blockLevenshteinDistanceMap.put(block, averageLevenshteinDistance);
        }

    }

    private void populatePromptMaps(BufferedReader reader, Map<Blocco, List<Prompt>> blockPromptMap) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(";");
            Blocco b = new Blocco(columns[0]);
            String textA = columns[1];
            String textB = columns[2];
            Prompt prompt = PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(textA, textB, Boolean.parseBoolean(columns[3]));
            this.promptLevenshteinDistanceMap.put(prompt, LevenshteinDistance.calculate(textA, textB));
            this.promptSimilarityMap.put(prompt, CosineSimilarityText.apply(textA, textB));
            AddToMapList.addToMapList(b, prompt, blockPromptMap);
        }
    }

    private double calculateAverageBlockLevenshteinDistance(Blocco b) {
        double average = 0;
        for (Prompt p : this.blockPromptMap.get(b)) {
            average += this.promptLevenshteinDistanceMap.get(p);
        }
        average /= this.blockPromptMap.get(b).size();
        return average;
    }

    private double calculateAverageBlockCosineSimilarity(Blocco b) {
        double average = 0;
        for (Prompt p : this.blockPromptMap.get(b)) {
            average += this.promptSimilarityMap.get(p);
        }
        average /= this.blockPromptMap.get(b).size();
        return average;
    }
}

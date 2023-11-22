package it.uniroma3.LLMOracle.comando.comandi;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.GPT.GPTException;
import it.uniroma3.LLMOracle.GPT.chatCompletion.Chat;
import it.uniroma3.LLMOracle.GPT.tokenizer.Tokenizer;
import it.uniroma3.LLMOracle.comando.Comando;
import it.uniroma3.LLMOracle.data.Blocco;
import it.uniroma3.LLMOracle.utils.AddToMapList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test implements Comando {

    private final Map<Blocco, List<String>> blockPromptMap;

    private final Map<Blocco, List<String>> blockTrainPromptMap;

    public Test(){
        this.blockPromptMap = new HashMap<>();
        this.blockTrainPromptMap = new HashMap<>();
    }

    @Override
    public void esegui(Application application) throws InterruptedException, IOException, GPTException {
        String datasetFolderPath = application.getAppProperties().getDatasetPath();
        String datasetPath = datasetFolderPath + "/nuovo/camera/oracle_ext_camera0_15.csv";
        String trainsetPath = datasetFolderPath + "/nuovo/camera/train_ext_camera0_15.csv";
        BufferedReader datasetReader = new BufferedReader(new FileReader(datasetPath));
        BufferedReader trainsetReader = new BufferedReader(new FileReader(trainsetPath));
        this.populatePromptMaps(datasetReader, this.blockPromptMap);
        this.populatePromptMaps(trainsetReader, this.blockTrainPromptMap);
        int tokensPerDescription = 100;
        Chat initChat = new Chat();
        List<String> tokenisedPrompts = new ArrayList<>();
        for(Blocco b : this.blockTrainPromptMap.keySet()){
            System.out.println(b);
            List<String> promptList = this.blockTrainPromptMap.get(b);
            for(String p : promptList){
                //System.out.println(p);
                Tokenizer tokenizer = new Tokenizer(p);
                String cutPrompt = tokenizer.getNextNTokens(tokensPerDescription);
                tokenisedPrompts.add(cutPrompt);
            }
        }
        System.out.println("Tokenised prompts: "+tokenisedPrompts.size());
        for(String s : tokenisedPrompts){
            System.out.println(s);
        }
    }

    private void populatePromptMaps(BufferedReader reader, Map<Blocco, List<String>> blockPromptMap) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(";");
            Blocco b = new Blocco(columns[0]);
            String textA = columns[1].toLowerCase();
            String textB = columns[2].toLowerCase();
            /*this.promptLevenshteinDistanceMap.put(prompt, LevenshteinDistance.calculate(textA, textB));
            this.promptSimilarityMap.put(prompt, CosineSimilarityText.apply(textA, textB));*/
            AddToMapList.addToMapList(b, textA, blockPromptMap);
            AddToMapList.addToMapList(b, textB, blockPromptMap);
        }
    }
}

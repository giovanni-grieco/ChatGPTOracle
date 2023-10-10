package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.Prompt;
import it.uniroma3.chatGPT.GPT.PromptBuilder;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;
import it.uniroma3.chatGPT.utils.CosineSimilarityText;
import it.uniroma3.chatGPT.utils.LevenshteinDistance;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blocking implements Comando {

    Map<Integer, List<Prompt>> promptInBlocks;

    Map<Entity, List<String>> entity2Titles;

    public Blocking(){
        promptInBlocks = new HashMap<>();
        entity2Titles = new HashMap<>();
    }

    private void addToBlock(int block, Prompt prompt){
        if(promptInBlocks.containsKey(block)){
            promptInBlocks.get(block).add(prompt);
        }else{
            List<Prompt> promptList = new ArrayList<>();
            promptList.add(prompt);
            promptInBlocks.put(block, promptList);
        }
    }

    private void addToTitles(Entity entity, String title){
        if(entity2Titles.containsKey(entity)){
            entity2Titles.get(entity).add(title);
        }else{
            List<String> titleList = new ArrayList<>();
            titleList.add(title);
            entity2Titles.put(entity, titleList);
        }
    }

    @Override
    public void esegui(Application application) throws InterruptedException, IOException {

        //itero su tutte le entità
        //prendo il loro compo title e di fatto faccio un n*(n-1)/2 confronti in cui faccio blocking di coppie di title.
        //li posizioni in una hashmap che chiave un integer che da la classe al blocco e come value una lista di coppie di title su cui fare l'interrogazione a gpt

        List<Entity> entityList = new ArrayList<>(application.getEntities());
        for(Entity e : entityList){
            List<Data> entityData = e.getData();
            for(Data d: entityData){
                String htmlTextData = d.getTextData();
                addToTitles(e, HTMLFilter.getTitle(htmlTextData));
            }
        }
        //faccio prima i casi di matching
        for(Entity e : entity2Titles.keySet()){
            List<String>entityTitles = entity2Titles.get(e);
            List<Pair<String, String>> titlePairs = pairTitles(entityTitles);
            for(Pair<String, String> pairOfTitles: titlePairs){
                String title1 = pairOfTitles.getLeft();
                String title2 = pairOfTitles.getRight();
                if(title1.isEmpty() || title2.isEmpty() || title1.isBlank() || title2.isBlank()){
                    continue;
                }
                double distanceBetweenTitles = CosineSimilarityText.apply(title1.toLowerCase(), title2.toLowerCase());
                addToBlock((int)Math.floor(distanceBetweenTitles*10), PromptBuilder.buildPromptTwoSnippetsStandardChatGPT(title1, title2, true));
            }
        }

        //faccio i casi di non matching

        System.out.println(promptInBlocks.keySet());
        for(int n : promptInBlocks.keySet()){
            System.out.println("Block: " + n);
            System.out.println("Prompt in block: " + promptInBlocks.get(n).size());
        }

    }

    public List<Pair<String, String>> pairTitles(List<String> titles){
        List<Pair<String, String>> prompts = new ArrayList<>();
        for(int i=0; i<titles.size(); i++ ){
            String title1 = titles.get(i);
            for(int j=i+1; j<titles.size(); j++){
                String title2 = titles.get(j);
                prompts.add(Pair.of(title1, title2));
            }
        }
        return prompts;
    }
}

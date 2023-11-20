package it.uniroma3.LLMOracle.comando.comandi;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.GPT.GPTQuery;
import it.uniroma3.LLMOracle.GPT.LLM;
import it.uniroma3.LLMOracle.GPT.chatCompletion.AzureGPT;
import it.uniroma3.LLMOracle.GPT.prompt.Prompt;
import it.uniroma3.LLMOracle.GPT.segmentazione.Segmenter;
import it.uniroma3.LLMOracle.comando.Comando;
import it.uniroma3.LLMOracle.data.Entity;
import it.uniroma3.LLMOracle.data.extraction.HTMLFilter;
import java.util.ArrayList;
import java.util.List;

public class Segmentazione implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException {
        try {
            List<Entity> entityList = new ArrayList<>(application.getDataset().getEntities());
            List<Prompt> prompts = new ArrayList<>();
            Entity e = entityList.get(1);
            String initialPrompt = "You are an entity extractor. You are given a text and you have to extract the entity from it. Try a find a specific person, object, place, in general entity.";
            LLM gpt = new AzureGPT(initialPrompt);
            gpt.answerQuestionCompletion("hello", "gpt-35-turbo");
            String html = e.getData().get(0).getTextData();
            String testoFiltrato = HTMLFilter.filterText(html, HTMLFilter.DEFAULT_TO_REMOVE_TAGS).replaceAll("\"","inches");


            int maxTokensPerPrompt = 3000;

            List<String> segments = new ArrayList<>();
            Segmenter segmenter = new Segmenter(testoFiltrato);
            while(segmenter.hasNextToken()){
                String segment = segmenter.getNextNTokens(maxTokensPerPrompt);
                segments.add(segment);
            }
            System.out.println("Segments: "+segments.size());
            for(String segment : segments){
                prompts.add(new Prompt(segment));
            }
            var queries = gpt.processPrompts(prompts, "gpt-35-turbo",0);
            for(GPTQuery query : queries){
                System.out.println(query);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

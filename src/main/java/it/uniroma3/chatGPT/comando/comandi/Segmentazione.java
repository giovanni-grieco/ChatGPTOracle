package it.uniroma3.chatGPT.comando.comandi;

import com.didalgo.gpt3.Encoding;
import com.didalgo.gpt3.GPT3Tokenizer;
import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.AzureGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.GPT.LLM;
import it.uniroma3.chatGPT.GPT.segmentazione.Segmenter;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.GPT.Prompt;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;

import javax.swing.text.html.HTML;
import java.util.ArrayList;
import java.util.List;

public class Segmentazione implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException {
        try {
            List<Entity> entityList = new ArrayList<>(application.getEntities());
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
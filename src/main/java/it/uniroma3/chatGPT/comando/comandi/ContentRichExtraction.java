package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.AzureGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.GPT.LLM;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.GPT.Prompt;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;

import java.util.ArrayList;
import java.util.List;

public class ContentRichExtraction implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException {
        try {
            List<Entity> entityList = new ArrayList<>(application.getEntities());
            List<Prompt> prompts = new ArrayList<>();
            Entity e = entityList.get(0);
            String initialPrompt = "You're a helpful assistant that will have to read an HTML document talking about a certain entity. You are required to extract the relevant information and discard all of the HTML tags and metadata. You will say what the page is talking about regarding the main subject of the entity disregarding all of the useless contextual information, focusing mainly on what you recognise to be the subject. Answer only with \"Entity: <name descriptive of the entity>\".";
            LLM gpt = new AzureGPT(initialPrompt);
            String html = e.getData().get(0).getTextData();
            html = HTMLFilter.filter(html, HTMLFilter.DEFAULT_TO_REMOVE_TAGS).replaceAll("\"","''").replaceAll("\n","");
            System.out.println("HTML filtered size: "+html.length());
            //Segmentiamo
            Prompt p = new Prompt(html);
            GPTQuery query = gpt.processPrompt(p,"gpt-3.5-turbo");
            System.out.println(query.getRisposta());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

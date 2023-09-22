package it.uniroma3.chatGPT.GPT;

import java.util.List;

/**
 * Questa classe è l'implementazione dell'interfaccia LLM che permette di processare una lista di prompt usando l'API di ChatGPT di OpenAI.
 */
public class ChatGPT implements LLM{
    @Override
    public List<GPTQuery> processPrompts(List<String> prompts, String modelName, int millisDelay) throws InterruptedException {
        return null;
    }
}

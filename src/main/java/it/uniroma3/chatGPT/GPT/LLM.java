package it.uniroma3.chatGPT.GPT;

import java.util.List;

public interface LLM {
    public List<GPTQuery> processPrompts(List<String> prompts, String modelName, int millisDelay) throws InterruptedException;
}

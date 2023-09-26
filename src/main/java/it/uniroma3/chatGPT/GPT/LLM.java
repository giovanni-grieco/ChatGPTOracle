package it.uniroma3.chatGPT.GPT;

import java.util.List;

public abstract class LLM {
    protected static String[] models ={"text-davinci-003","curie:ft-personal-2023-08-22-20-39-20","curie:ft-personal-2023-08-22-20-10-51" ,"curie:ft-personal-2023-08-22-19-32-20", "curie:ft-personal-2023-08-19-19-36-38"};
    protected final static String COMPLETION_URL_API = "https://api.openai.com/v1/completions";

    protected final static String CHAT_URL_API = "https://api.openai.com/v1/chat/completions";

    protected final static String URL_AVAILABLE_MODELS = "https://api.openai.com/v1/models";
    //private static String model= "text-babbage-001";
    protected static String privateKey = null;

    public LLM(String apiKey) {
        LLM.privateKey = apiKey;
    }


    public abstract List<GPTQuery> processPrompts(List<String> prompts, String modelName, int millisDelay) throws InterruptedException;
}

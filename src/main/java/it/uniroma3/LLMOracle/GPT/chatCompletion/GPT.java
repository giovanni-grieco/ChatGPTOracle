package it.uniroma3.LLMOracle.GPT.chatCompletion;

import it.uniroma3.LLMOracle.GPT.LLM;
import java.io.IOException;

public abstract class GPT extends LLM {
    protected final String assistantContent;

    protected GPT(String apiKey, String assistantContent) {
        super(apiKey);
        this.assistantContent = assistantContent;
    }

    public abstract String answerQuestionCompletion(String prompt, String modelName) throws IOException;

    protected abstract String extractMessageFromJSONResponse(String response);
}

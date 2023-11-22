package it.uniroma3.LLMOracle.comando.comandi;

import it.uniroma3.LLMOracle.GPT.prompt.ClassificationPrompt;
import it.uniroma3.LLMOracle.GPT.prompt.PromptBuilder;
import it.uniroma3.LLMOracle.comando.Comando;
import org.junit.Test;

import static org.junit.Assert.*;

public class FewShotsBlockingTest {

    @Test
    public void shortenPrompt() {
        FewShotsBlocking c = new FewShotsBlocking();
        ClassificationPrompt p = (ClassificationPrompt) PromptBuilder.buildPromptTwoSnippetsStandardChatGPT("nikon camera 123 15.6\\\"", "canon eos 2000d 18-55mm", false);
        ClassificationPrompt shortenedPrompt = c.shortenPrompt(p, 4);
        assertEquals("first: nikon camera . second: canon eos . ", shortenedPrompt.getTextPrompt());
    }
}
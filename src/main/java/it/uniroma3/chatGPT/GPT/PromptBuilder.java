package it.uniroma3.chatGPT.GPT;

public class PromptBuilder {
    public static String twoWebPagesTalkingAboutTheSameEntity(String webPageA, String webPageB) {
        String prompt = "These are 2 snippets of text.\n";
        prompt += "First: " + webPageA + ".\n";
        prompt += "Second: " + webPageB + ".\n";
        prompt += "Are the 2 snippets talking about the same entity, object or attribute? Answer with 'yes' or 'no'.\n";
        return prompt;
    }
}

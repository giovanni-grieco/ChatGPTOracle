package it.uniroma3.chatGPT.GPT;

public class PromptBuilder {
    public static String twoWebPagesTalkingAboutTheSameEntity(String webPageA, String webPageB){
        String prompt = "I have extracted information from two web pages.\n";
        prompt += "This is first web page: "+webPageA+".\n";
        prompt += "This is second web page: "+webPageB+".\n";
        prompt += "Are these 2 web pages talking about the same entity or attribute? Answer only with 'yes' or 'no'\n";
        return prompt;
    }
}

package it.uniroma3.LLMOracle.GPT;

import it.uniroma3.LLMOracle.GPT.prompt.Prompt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class LLM {

    public static final String STANDARD_INITIALIZATION_PROMPT = "You will be given 2 snippets of texts. You will have to answer whether the 2 texts are talking about the same entity, object or subject. Answer only with yes or no.";
    protected final static String COMPLETION_URL_API = "https://api.openai.com/v1/completions";

    protected final static String CHAT_URL_API = "https://api.openai.com/v1/chat/completions";

    protected final static String URL_AVAILABLE_MODELS = "https://api.openai.com/v1/models";
    //private static String model= "text-babbage-001";
    protected static String privateKey = null;

    private final List<Integer> timeList = new ArrayList<>();

    private final List<Character> loadingChars = List.of('|', '/', '-', '\\');

    int loadingCharIndex = 0;


    protected LLM(String apiKey) {
        LLM.privateKey = apiKey;
    }

    public abstract String answerQuestionCompletion(String prompt, String modelName) throws GPTException, IOException;

    public List<GPTQuery> processPrompts(List<Prompt> prompts, String modelName, int millisDelay) throws InterruptedException{
        List<GPTQuery> outputs = new ArrayList<>();
        int processedPrompts=0;
        int totalPrompts = prompts.size();
        boolean firstPrompt = true;
        for (Prompt prompt : prompts) {
            //System.out.println("Asking: " + prompt.toString());
            int querytime = 0;
            try {
                GPTQuery answer = this.processPrompt(prompt, modelName);
                outputs.add(answer);
                processedPrompts++;
                //System.out.println("Answer: " + answer.getRisposta());
                querytime = answer.getTempoDiRisposta();
                this.addTime(querytime);
            } catch (GPTException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Skipping to next prompt...");
            }
            String estimatedTime = String.format("%.2f", ((this.getAverageTime()*(prompts.size()-processedPrompts)))/1000);
            String output = "Processed prompts: "+processedPrompts+"/"+totalPrompts+" "+"estimated time remaining "+estimatedTime+"s "+this.getNextLoadingChar();
            int outputLength = output.length();
            if(!firstPrompt){
                for(int i = 0; i<outputLength; i++){
                    System.out.print("\b");
                }
            }
            firstPrompt = false;
            System.out.print(output);
        }
        System.out.println();
        return outputs;
    }

    public GPTQuery processPrompt(Prompt prompt, String modelName) throws GPTException {
        try {
            String answer;
            //System.out.println("Answering...");
            int initTime = (int) System.currentTimeMillis();
            answer = answerQuestionCompletion(prompt.getTextPrompt(), modelName);
            int endTime = (int) System.currentTimeMillis();
            return new GPTQuery(answer, modelName, prompt, endTime - initTime);
        } catch (Exception e) {
            throw new GPTException(e.getMessage(), e.getCause());
        }
    }

    private char getNextLoadingChar(){
        if(loadingCharIndex>=this.loadingChars.size()){
            loadingCharIndex = 0;
        }
        char nextLoadingChar = this.loadingChars.get(loadingCharIndex);
        loadingCharIndex++;
        return nextLoadingChar;
    }

    private void addTime(int time){
        if(this.timeList.size()<=10){
            this.timeList.add(time);
        }else{
            this.timeList.remove(0);
            this.timeList.add(time);
        }
    }

    private float getAverageTime(){
        int sum = 0;
        for(int i : this.timeList){
            sum+=i;
        }
        return (float)sum/this.timeList.size();
    }
}

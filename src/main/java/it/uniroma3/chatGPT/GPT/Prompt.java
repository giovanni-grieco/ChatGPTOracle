package it.uniroma3.chatGPT.GPT;

public class Prompt {
    private final String prompt;

    public Prompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return this.prompt;
    }

    @Override
    public String toString(){
        return "Prompt: "+prompt+"\n";
    }
}

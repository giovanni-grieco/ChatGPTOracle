package it.uniroma3.chatGPT.GPT;

public class Prompt {
    private final String prompt;

    private final boolean[] isPositive;

    public Prompt(String prompt, boolean ... isPositive) {
        this.prompt = prompt;
        this.isPositive = isPositive;
    }

    public String getPrompt() {
        return this.prompt;
    }

    public boolean isPositive() {
        return this.isPositive[0];
    }

    @Override
    public String toString(){
        String output =  "Prompt: "+prompt+"\n"+" Expected result: ";
        if(isPositive[0]){
            output += "yes";
        }else{
            output += "no";
        }
        return output;
    }
}

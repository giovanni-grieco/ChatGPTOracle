package it.uniroma3.chatGPT.GPT.prompt;

public class ClassificationPrompt extends Prompt {

    private final boolean isPositive;

    public ClassificationPrompt(String prompt, boolean isPositive) {
        super(prompt);
        this.isPositive = isPositive;
    }

    public boolean isPositive() {
        return this.isPositive;
    }

    @Override
    public String toString(){
        String output =  "Prompt: "+this.getPrompt()+"\n"+" Expected result: ";
        if(isPositive){
            output += "yes";
        }else{
            output += "no";
        }
        return output;
    }
}

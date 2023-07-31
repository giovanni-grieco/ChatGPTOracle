package it.uniroma3.chatGPT.GPT;

public class GPTQuery {
    private String prompt;
    private String risposta;
    private String modello;
    private int tempoDiRisposta;

    public GPTQuery(String risposta, String modello, String prompt, int tempoDiRisposta){
        this.risposta = risposta;
        this.modello = modello;
        this.prompt = prompt;
        this.tempoDiRisposta = tempoDiRisposta;
    }

    public String getRisposta() {
        return risposta;
    }

    public void setRisposta(String risposta) {
        this.risposta = risposta;
    }

    public String getModello() {
        return modello;
    }

    public void setModello(String modello) {
        this.modello = modello;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int getTempoDiRisposta() {
        return tempoDiRisposta;
    }

    public void setTempoDiRisposta(int tempoDiRisposta) {
        this.tempoDiRisposta = tempoDiRisposta;
    }

    public boolean isYes(){
        return risposta.contains("yes") || risposta.contains("Yes") || risposta.contains("YES") || risposta.contains("y") || risposta.contains("Y");
    }

    @Override
    public String toString(){
        return "Prompt: " + prompt + "\n" + "Risposta: " + risposta + "\n" + "Modello: " + modello + "\n" + "Tempo di risposta: " + tempoDiRisposta + "ms";
    }
}

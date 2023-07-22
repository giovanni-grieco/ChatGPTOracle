package it.uniroma3.chatGPT;

public class Main {


    public static void main(String[] args) throws Exception {

        ChatGPT gpt = new ChatGPT();

        System.out.println("All available models:");
        for(String model : gpt.availableOpenAiModels()){
            System.out.println(model);
        }
        //Usiamo una lista di modelli più piccola presa dal sito della OpenAI
        for(String modello : ChatGPT.models){
            System.out.println("----");
            System.out.println("Model used-> "+modello);
            long initTime = System.currentTimeMillis();
            try {
                System.out.println(gpt.answerQuestion(gpt.buildQuestion("D. Gallinari", "Danilo Gallinari"), modello));
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Utilizzo del modello \""+modello+"\" fallito");
            }
            System.out.println("ChatGPT Query time:"+(System.currentTimeMillis()-initTime)+" ms");
            Thread.sleep(15000); // altrimenti si raggiunge un overflow di richieste e da errore 429
        }
    }
}

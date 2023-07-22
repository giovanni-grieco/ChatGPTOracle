package it.uniroma3.chatGPT;

import java.io.IOException;
import java.util.List;

public class Main {


    public static void main(String[] args){
        try {
            ChatGPT gpt = new ChatGPT();
            List<GPTQuery> risposte;
            risposte = gpt.processPrompts(List.of("Is 5+5 equals to 10? answer with yes or no", "Can a squirrel fly? Answer with yes or no"), "text-davinci-003", 1000);
            for (GPTQuery risposta : risposte) {
                System.out.println(risposta.toString());
            }
        }catch (IOException e){
            System.out.println(e.getMessage()+"\nAssicurarsi di avere un file chiamato app.properties nella root folder!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

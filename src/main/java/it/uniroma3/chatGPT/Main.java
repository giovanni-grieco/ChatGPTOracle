package it.uniroma3.chatGPT;

import java.util.List;

public class Main {


    public static void main(String[] args) throws Exception {

        ChatGPT gpt = new ChatGPT();
        List<GPTQuery> risposte;
        risposte = gpt.processPrompts(List.of("Is 5+5 equals to 10? answer with yes or no", "Can a squirrel fly? Answer with yes or no"),"text-davinci-003",1000);
        for(GPTQuery risposta : risposte) {
            System.out.println(risposta.toString());
        }
    }
}

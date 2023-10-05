package it.uniroma3.chatGPT.comando;

import it.uniroma3.chatGPT.Application;

import java.io.IOException;

public interface Comando {
    void esegui(Application application) throws InterruptedException, IOException;

}

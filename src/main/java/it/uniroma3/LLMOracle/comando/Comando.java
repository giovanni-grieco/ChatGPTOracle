package it.uniroma3.LLMOracle.comando;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.GPT.GPTException;

import java.io.IOException;

/**
 * Interfaccia che rappresenta un comando eseguibile dall'applicazione
 * Ogni nome di comando deve essere univoco, e verrà rilevato automaticamente dal programma
 * Basta aggiungere una nuova classe che implementa l'interface Comando nel package it.uniroma3.LLMOracle.comando.comandi
 */

public interface Comando {
    void esegui(Application application) throws InterruptedException, IOException, GPTException;

}

package it.uniroma3.LLMOracle.comando;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.GPT.GPTException;

import java.io.IOException;

public interface Comando {
    void esegui(Application application) throws InterruptedException, IOException, GPTException;

}

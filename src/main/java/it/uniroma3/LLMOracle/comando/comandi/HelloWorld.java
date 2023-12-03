package it.uniroma3.LLMOracle.comando.comandi;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.GPT.GPTException;
import it.uniroma3.LLMOracle.comando.Comando;
import java.io.IOException;

//Questo comando, quando invocato, stampa a schermo "Hello World!"
public class HelloWorld implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException, IOException, GPTException {
        System.out.println("Hello World!");
    }
}

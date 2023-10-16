package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.comando.AnalisiCompletaWorkerThread;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AnalisiCompleta implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException, IOException {
        System.out.print("Inserisci il numero di prompt totali: ");
        int numeroDiPromptTotali = new Scanner(System.in).nextInt();
        int percentualePositivi = 0;
        int entityTypes = application.getEntityTypes();
        List<Entity> entityList = new ArrayList<>(application.getEntities());
        List<Thread> workerThreads = new ArrayList<>();
        for(int i = 0; i<entityTypes; i++){
            while(percentualePositivi != 100) {
                System.out.println("Creazione thread");
                workerThreads.add(new AnalisiCompletaWorkerThread(application, i, percentualePositivi, numeroDiPromptTotali, entityList));
                percentualePositivi += 5;
            }
            percentualePositivi = 0;
        }
        for(Thread t : workerThreads){
            t.start();
            t.join();
        }
        System.out.println("Analisi completa finita. Thread arrestati.");
    }
}
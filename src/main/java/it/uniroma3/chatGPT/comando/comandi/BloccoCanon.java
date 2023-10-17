package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.comando.InterrogatoreGPTThread;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;

import java.io.IOException;
import java.util.*;

public class BloccoCanon implements Comando {

    private static final String[] blocksDiscriminants = {"Canon", "Nikon", ""};

    @Override
    public void esegui(Application application) throws InterruptedException, IOException {

        List<Entity> entityListAllTypes = new ArrayList<>(application.getEntities());
        List<Entity> entityList = new ArrayList<>();

        //filtriamo quelli di tipo 0 (fotocamere)

        for (Entity e : entityListAllTypes) {
            if (e.getType() == 0) {
                entityList.add(e);
            }
        }

        //filtriamo tutte le entità che hanno nel campo title il discriminante del blocco che vogliamo analizzare
        for (String blockDiscriminant : blocksDiscriminants) {
            List<Entity> inBlockEntities = new ArrayList<>();
            for (Entity e : entityList) {
                for (Data d : e.getData()) {
                    if (belongToBlock(d.getTitle(), blockDiscriminant)) {
                        inBlockEntities.add(e);
                        break;
                    }
                }
            }

            //Stampa di prova
            for (Entity e : inBlockEntities) {
                System.out.println(e.getName());
                for (Data d : e.getData()) {
                    System.out.println(d.getTitle());
                }
            }

            int numeroDiPromptTotali = 2500;
            int percentualePositivi = 0;

            List<Thread> workerThreads = new ArrayList<>();
            while (percentualePositivi != 100) {
                System.out.println("Creazione thread");
                Thread t = new InterrogatoreGPTThread(application, 0, percentualePositivi, numeroDiPromptTotali, inBlockEntities);
                workerThreads.add(t);
                percentualePositivi += 5;
                t.start();
            }

            for (Thread t : workerThreads) {
                t.join();
            }
        }

    }

    private boolean belongToBlock(String text, String blockDiscriminant) {
        String textLower = text.toLowerCase();
        String blockDiscriminantLower = blockDiscriminant.toLowerCase();
        return textLower.contains(blockDiscriminantLower);
    }

}

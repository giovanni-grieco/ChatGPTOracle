package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.comando.AnalisiCompletaWorkerThread;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.comando.BloccoCanonWorkerThread;
import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;

public class BloccoCanon implements Comando {

    //Implementato per motivi di ottimizzazione


    public static void putPairInMap(Map<Entity, List<Pair<Data, Data>>> entity2PairOfData, Entity e, Pair<Data, Data> pairOfData) {
        if (entity2PairOfData.containsKey(e)) {
            entity2PairOfData.get(e).add(pairOfData);
        } else {
            List<Pair<Data, Data>> pairOfDataList = new ArrayList<>();
            pairOfDataList.add(pairOfData);
            entity2PairOfData.put(e, pairOfDataList);
        }
    }

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

        //loadData2TitleMap(entityList);

        Map<Entity, List<Pair<Data, Data>>> entity2PairOfData = new HashMap<>();

        int numThread = Runtime.getRuntime().availableProcessors();
        Thread[] workers = new Thread[numThread];
        for (int i = 0; i < numThread; i++) {
            System.out.println("Thread " + i + " started");
            workers[i] = new BloccoCanonWorkerThread(i, entityList.subList(i * entityList.size() / numThread, (i + 1) * entityList.size() / numThread), entity2PairOfData);
            System.out.println(entityList.subList(i * entityList.size() / numThread, (i + 1) * entityList.size() / numThread));
            workers[i].start();
        }

        for (int i = 0; i < numThread; i++) {
            workers[i].join();
        }
        System.out.println("All threads finished");
        System.out.println("Data Cache Dump");
        System.out.println(Data.Cache.dumpCacheInfo());

        List<Entity> allCanons = new ArrayList<>(entity2PairOfData.keySet());
        for (Entity e : allCanons) {
            System.out.println(e.getName());
            for (Data d : e.getData()) {
                System.out.println(d.getTitle());
            }
        }

        System.out.print("Inserisci il numero di prompt totali: ");
        int numeroDiPromptTotali = new Scanner(System.in).nextInt();
        int percentualePositivi = 0;

        List<Thread> workerThreads = new ArrayList<>();
        while (percentualePositivi != 100) {
            System.out.println("Creazione thread");
            workerThreads.add(new AnalisiCompletaWorkerThread(application, 0, percentualePositivi, numeroDiPromptTotali, allCanons));
            percentualePositivi += 5;
        }

        for (Thread t : workerThreads) {
            t.start();
            t.join();
        }
        System.out.println("Analisi completa finita. Thread arrestati.");


        //System.out.println(entity2PairOfData);
    }
}

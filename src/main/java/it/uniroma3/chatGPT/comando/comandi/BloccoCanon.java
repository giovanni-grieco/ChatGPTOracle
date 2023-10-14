package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.comando.WorkerThread;
import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloccoCanon implements Comando {

    //Implementato per motivi di ottimizzazione
    private static final Map<Data, String> data2Title = new HashMap<>();


    public static void putPairInMap(Map<Entity, List<Pair<Data, Data>>> entity2PairOfData, Entity e, Pair<Data, Data> pairOfData) {
        if (entity2PairOfData.containsKey(e)) {
            entity2PairOfData.get(e).add(pairOfData);
        } else {
            List<Pair<Data, Data>> pairOfDataList = new ArrayList<>();
            pairOfDataList.add(pairOfData);
            entity2PairOfData.put(e, pairOfDataList);
        }
    }

    private void loadData2TitleMap(List<Entity> entityList) throws IOException {
        for (Entity e : entityList) {
            for (Data d : e.getData()) {
                String title = d.getTitle();
                data2Title.put(d, title);
            }
        }
    }

    private String getTitleFromDataMap(Data d) {
        return data2Title.get(d);
    }

    @Override
    public void esegui(Application application) throws InterruptedException, IOException {
        List<Entity> entityList = new ArrayList<>(application.getEntities());

        //loadData2TitleMap(entityList);

        Map<Entity, List<Pair<Data, Data>>> entity2PairOfData = new HashMap<>();

        int numThread = Runtime.getRuntime().availableProcessors();
        Thread[] workers = new Thread[numThread];
        for (int i = 0; i < numThread; i++) {
            System.out.println("Thread " + i + " started");
            workers[i] = new WorkerThread(i, entityList.subList(i * entityList.size() / numThread, (i + 1) * entityList.size() / numThread), entity2PairOfData);
            System.out.println(entityList.subList(i * entityList.size() / numThread, (i + 1) * entityList.size() / numThread));
            workers[i].start();
        }

        for (int i = 0; i < numThread; i++) {
            workers[i].join();
        }
        System.out.println("All threads finished");
        System.out.println("Data Cache Dump");
        System.out.println(Data.Cache.dumpCacheInfo());
        //System.out.println(entity2PairOfData);
    }
}

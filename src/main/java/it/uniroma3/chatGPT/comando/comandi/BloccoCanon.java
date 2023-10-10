package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloccoCanon implements Comando{

    public static boolean isCanon(String title){
        String[] discriminanti = {"canon", "Canon", "CANON"};
        for(String s : discriminanti){
            if(title.contains(s)){
                return true;
            }
        }
        return false;
    }

    public static void putPairInMap(Map<Entity, List<Pair<Data, Data>>> entity2PairOfData, Entity e, Pair<Data, Data> pairOfData){
        if(entity2PairOfData.containsKey(e)){
            entity2PairOfData.get(e).add(pairOfData);
        }else{
            List<Pair<Data, Data>> pairOfDataList = new ArrayList<>();
            pairOfDataList.add(pairOfData);
            entity2PairOfData.put(e, pairOfDataList);
        }
    }

    @Override
    public void esegui(Application application) throws InterruptedException, IOException {
        List<Entity> entityList = new ArrayList<>(application.getEntities());
        Map<Entity, List<Pair<Data, Data>>> entity2PairOfData = new HashMap<>();

        int numThread = Runtime.getRuntime().availableProcessors();
        Thread[] workers = new Thread[numThread];
        for(int i=0; i<numThread; i++){
            System.out.println("Thread " + i + " started");
            workers[i] = new WorkerThread(i, entityList.subList(i * entityList.size() / numThread, (i + 1) * entityList.size() / numThread), entity2PairOfData);
            System.out.println(entityList.subList(i*entityList.size()/numThread, (i+1)*entityList.size()/numThread));
            workers[i].start();
        }

        for(int i=0; i<numThread; i++){
            workers[i].join();
        }


        System.out.println(entity2PairOfData);
    }
    static private class WorkerThread extends Thread{

        private int id;

        private List<Entity> assignedEntity;

        private Map<Entity, List<Pair<Data, Data>>> entity2PairOfData;


        public WorkerThread(int id, List<Entity> assignedEntity, Map<Entity, List<Pair<Data, Data>>> entity2PairOfData){
            this.id = id;
            this.assignedEntity = assignedEntity;
            this.entity2PairOfData = entity2PairOfData;
        }

        @Override
        public void run() {
            List<Entity> entityList = this.assignedEntity;
            for(Entity e : entityList){
                List<Data> dataList = e.getData();
                for(int i = 0; i < dataList.size(); i++){
                    for(int j = i+1; j < dataList.size(); j++){
                        try {
                            if(isCanon(dataList.get(i).getTitle()) && isCanon(dataList.get(j).getTitle())){
                                putPairInMap(entity2PairOfData, e, Pair.of(dataList.get(i), dataList.get(j)));
                            }
                        } catch (IOException ex) {
                            System.err.println("Error occurred in thread " + id + " while processing entity " + e + " with data " + dataList.get(i) + " and " + dataList.get(j));
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        }
    }
}

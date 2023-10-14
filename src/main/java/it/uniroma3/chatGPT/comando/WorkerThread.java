package it.uniroma3.chatGPT.comando;

import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static it.uniroma3.chatGPT.comando.comandi.BloccoCanon.putPairInMap;

public class WorkerThread extends Thread {

    private final int id;

    private final List<Entity> assignedEntity;

    private final Map<Entity, List<Pair<Data, Data>>> entity2PairOfData;


    public WorkerThread(int id, List<Entity> assignedEntity, Map<Entity, List<Pair<Data, Data>>> entity2PairOfData) {
        this.id = id;
        this.assignedEntity = assignedEntity;
        this.entity2PairOfData = entity2PairOfData;
    }

    @Override
    public void run() {
        for (Entity e : this.assignedEntity) {
            List<Data> dataList = e.getData();
            for (int i = 0; i < dataList.size(); i++) {
                for (int j = i + 1; j < dataList.size(); j++) {
                    try {
                        if (isCanon(dataList.get(i).getTitle()) && isCanon(dataList.get(j).getTitle())) {
                            putPairInMap(entity2PairOfData, e, Pair.of(dataList.get(i), dataList.get(j)));
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    private static boolean isCanon(String title) {
        String[] discriminanti = {"canon", "Canon", "CANON"};
        for (String s : discriminanti) {
            if (title.contains(s)) {
                return true;
            }
        }
        return false;
    }
}
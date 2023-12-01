package it.uniroma3.LLMOracle.data;

import it.uniroma3.LLMOracle.data.extraction.AlaskaEntityExtractor;
import it.uniroma3.LLMOracle.data.extraction.EntityExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static it.uniroma3.LLMOracle.Application.appProperties;

public class AlaskaDataset implements Dataset{

    private final Set<Entity> entities;

    private final Map<Data, Entity> dataEntityMap;

    public AlaskaDataset() throws IOException {
        this.entities = new HashSet<>();
        this.dataEntityMap = new HashMap<>();
        System.out.println("Extracting entities");
        for (EntityType type : EntityType.values()) {
            EntityExtractor extractor = new AlaskaEntityExtractor(type, Path.of(appProperties.getDatasetPath() + "/" + appProperties.getGroundTruthFileNames()[type.getTypeIndex()]));
            extractor.extractEntitiesFromGroundTruth(entities);
        }
        int camere = 0;
        int monitor = 0;
        int notebook = 0;
        //Stampiamo le entità per controllare
        for (Entity e : entities) {
            if(e.getType().equals(EntityType.CAMERA)){
                camere++;
            }
            if(e.getType().equals(EntityType.MONITOR)){
                monitor++;
            }
            if(e.getType().equals(EntityType.NOTEBOOK)){
                notebook++;
            }
            System.out.println(e);
            for (Data d : e.getData()) {
                this.dataEntityMap.put(d, e);
            }
        }
        System.out.println("Entities extracted: " + entities.size());
        System.out.println("Camere: " + camere);
        System.out.println("Monitor: " + monitor);
        System.out.println("Notebook: " + notebook);
        System.out.println("Data-Entity map size: " + dataEntityMap.size());
        /*for(Data d: dataEntityMap.keySet()){
            System.out.println(d + " -> " + dataEntityMap.get(d));
        }*/
    }

    @Override
    public Set<Entity> getEntities() {
        return this.entities;
    }

    @Override
    public Entity getEntityByData(Data data) {
        return this.dataEntityMap.get(data);
    }

    @Override
    public String toString(){
        return "AlaskaDataset: "+this.entities.size()+" entities";
    }
}

package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.data.extraction.AlaskaEntityExtractor;
import it.uniroma3.chatGPT.data.extraction.EntityExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import static it.uniroma3.chatGPT.Application.appProperties;

public class AlaskaDataset implements Dataset{

    private final Set<Entity> entities;

    public AlaskaDataset() throws IOException {
        this.entities = new HashSet<>();
        System.out.println("Extracting entities");
        for (EntityType type : EntityType.values()) {
            EntityExtractor extractor = new AlaskaEntityExtractor(type, Path.of(appProperties.getDatasetPath() + "/" + appProperties.getGroundTruthFileNames()[type.getTypeIndex()]));
            extractor.extractEntitiesFromGroundTruth(entities);
        }
        //Stampiamo le entità per controllare
        for (Entity e : entities) {
            System.out.println(e);
        }
        System.out.println("Entities extracted: " + entities.size());
    }

    @Override
    public Set<Entity> getEntities() {
        return this.entities;
    }

    @Override
    public Entity getEntityByData(Data data) {
        return this.entities.stream().filter(e -> e.contains(data)).findAny().orElseThrow();
    }
}

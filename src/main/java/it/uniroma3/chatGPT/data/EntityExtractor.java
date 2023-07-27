package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.AppProperties;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityExtractor {
    private final Path datasetPath;
    private final Path groundTruthPath;

    public EntityExtractor(AppProperties appProperties) throws IOException{
        datasetPath = Path.of(appProperties.getDatasetPath()+"/"+appProperties.getDatasetFolder());
        groundTruthPath = Path.of(appProperties.getDatasetPath()+"/"+appProperties.getGroundTruthFileName());
    }

    public Set<Entity> extractEntities() throws IOException {
        Set<Entity> entities = new HashSet<>();
        FileReader reader = new FileReader(groundTruthPath.toString());
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
        for(CSVRecord record : records){
            Entity newEntity = new Entity(record.get(0));
            if(entities.contains(newEntity)){
                entities.stream().filter(e->e.equals(newEntity)).findAny().orElseThrow().addHtmlLocation(record.get(1));
            }else{
                newEntity.addHtmlLocation(record.get(1));
                entities.add(newEntity);
            }
        }
        reader.close();
        return entities;
    }

}

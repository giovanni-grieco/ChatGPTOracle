package it.uniroma3.chatGPT.data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class AlaskaEntityExtractor implements EntityExtractor{

    private final EntityType typeOfEntityToBeExtracted;
    private final Path groundTruthPath;

    public AlaskaEntityExtractor(EntityType type, Path path) {
        this.typeOfEntityToBeExtracted = type;
        this.groundTruthPath = path;
    }

    public void extractEntitiesFromGroundTruth(Set<Entity> entities) throws IOException {
        FileReader reader = new FileReader(groundTruthPath.toString());
        Iterable<CSVRecord> records = CSVFormat.Builder.create().setHeader().setSkipHeaderRecord(true).build().parse(reader);
        for (CSVRecord record : records) {
            Entity newEntity = new Entity(this.typeOfEntityToBeExtracted, record.get(0));
            if (entities.contains(newEntity)) {
                entities.stream().filter(e -> e.equals(newEntity)).findAny().orElseThrow().addHtmlLocation(record.get(1));
            } else {
                newEntity.addHtmlLocation(record.get(1));
                entities.add(newEntity);
            }
        }
        reader.close();
    }

}

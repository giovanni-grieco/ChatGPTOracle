package it.uniroma3.chatGPT.data;

import java.io.IOException;
import java.util.Set;

public interface EntityExtractor {

    public void extractEntitiesFromGroundTruth(Set<Entity> entities) throws IOException;
}

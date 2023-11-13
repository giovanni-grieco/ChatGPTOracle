package it.uniroma3.LLMOracle.data;

import java.util.Set;

public interface Dataset {
    public Set<Entity> getEntities();

    public Entity getEntityByData(Data data);

}

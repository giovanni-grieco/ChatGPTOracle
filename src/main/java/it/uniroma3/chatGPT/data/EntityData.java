package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.Application;

import java.nio.file.Path;

public class EntityData extends Data {

    private final Entity entity;

    public EntityData(String domain, String id, Entity entity) {
        super(domain,id);
        this.entity = entity;
    }

    @Override
    public Path toFullPath(){
        return Path.of(Application.appProperties.getDatasetPath()+ "/" + Application.appProperties.getDatasetFolders()[this.entity.getType().getTypeIndex()] + "/" + this.getDomain()+ "/" + this.getId() + ".html");
    }


    @Override
    public String toString() {
        return "Entity data: "+this.getDomain() + "/" + this.getId();
    }

}

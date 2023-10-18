package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.Application;
import java.nio.file.Path;

public class Data extends AbstractData{

    private final Entity entity;

    public Data(String domain, String id, Entity entity) {
        super(domain,id);
        this.entity = entity;
    }

    @Override
    public Path toFullPath(){
        return Path.of(Application.appProperties.getDatasetPath()+ "/" + Application.appProperties.getDatasetFolders()[this.entity.getType().getTypeIndex()] + "/" + this.getDomain()+ "/" + this.getId() + ".html");
    }


    @Override
    public String toString() {
        return "Data: "+this.getDomain() + "/" + this.getId();
    }

}

package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.AppProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Data {

    private final Entity entity;
    private String domain;
    private String id;

    public Data(String domain, String id, Entity entity) {
        this.entity = entity;
        this.domain = domain;
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Path toFullPath() throws IOException {
        return Path.of(AppProperties.getAppProperties().getDatasetPath()+ "/" + AppProperties.getAppProperties().getDatasetFolders()[this.entity.getType()] + "/" + domain + "/" + id + ".html");
    }

    public String getTextData() throws IOException {
        return Files.readString(this.toFullPath());
    }

    @Override
    public String toString() {
        return this.domain + "/" + this.id;
    }

}

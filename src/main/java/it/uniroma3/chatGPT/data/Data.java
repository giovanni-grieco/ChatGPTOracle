package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.AppProperties;
import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;

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

    public Path toFullPath(){
        return Path.of(Application.appProperties.getDatasetPath()+ "/" + Application.appProperties.getDatasetFolders()[this.entity.getType()] + "/" + domain + "/" + id + ".html");
    }

    public String getTextData() throws IOException {
        return Files.readString(this.toFullPath());
    }

    @Override
    public String toString() {
        return this.domain + "/" + this.id;
    }

    public String getTitle() throws IOException {
        return HTMLFilter.getTitle(this.getTextData());
    }
}

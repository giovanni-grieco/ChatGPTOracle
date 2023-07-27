package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.AppProperties;
import it.uniroma3.chatGPT.utils.FileRetriever;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Entity {
    private String name;
    private List<String> dataLocations;

    public Entity(String name) {
        this.name = name;
        this.dataLocations = new ArrayList<>();
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDataLocations() {
        return dataLocations;
    }

    public void setDataLocations(List<String> dataLocations) {
        this.dataLocations = dataLocations;
    }

    public void addHtmlLocation(String htmlLocation) {
        this.dataLocations.add(htmlLocation);
    }

    public List<File> dataLocationsToFiles() throws IOException {
        List<File> files = new ArrayList<>();
        for(String dataLocation : dataLocations){
            files.add(FileRetriever.getFile(AppProperties.getAppProperties().getDatasetPath()+"/"+AppProperties.getAppProperties().getDatasetFolder()+"/"+dataLocation+".html"));
        }
        return files;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Entity e){
            return e.name.equals(this.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString(){
        return "Entity: "+name+"\n"+"Data Locations: "+ dataLocations.toString();
    }
}

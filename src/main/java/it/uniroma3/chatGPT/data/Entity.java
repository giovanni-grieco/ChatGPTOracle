package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.AppProperties;
import it.uniroma3.chatGPT.utils.FileRetriever;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Entity {
    private String name;
    private List<Data> data;

    public Entity(String name) {
        this.name = name;
        this.data = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public void addHtmlLocation(String htmlLocation) {
        String[] split = htmlLocation.split("//");
        this.data.add(new Data(split[0], split[1]));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Entity e) {
            return e.name.equals(this.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Entity: " + name + "\n" + "Data Locations: " + data.toString();
    }
}

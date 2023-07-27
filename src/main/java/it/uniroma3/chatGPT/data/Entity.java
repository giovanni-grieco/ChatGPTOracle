package it.uniroma3.chatGPT.data;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    private String name;
    private List<String> htmlLocations;

    public Entity(String name) {
        this.name = name;
        this.htmlLocations = new ArrayList<>();
    }

    public void addHtmlLocation(String htmlLocation) {
        this.htmlLocations.add(htmlLocation);
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
        return "Entity: "+name+"\n"+"Html Locations: "+htmlLocations.toString();
    }
}

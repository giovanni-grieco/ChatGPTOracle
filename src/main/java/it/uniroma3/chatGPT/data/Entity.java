package it.uniroma3.chatGPT.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Entity {

    private final EntityType type;
    private String name;
    private List<Data> data;

    public Entity(EntityType type, String name) {
        this.type = type;
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
        this.data.add(new EntityData(split[0], split[1], this));
    }

    public EntityType getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return getType() == entity.getType() && Objects.equals(getName(), entity.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getName());
    }

    @Override
    public String toString() {
        return "Entity: " + name +"(Type"+type+")";
    }
}

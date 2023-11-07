package it.uniroma3.LLMOracle.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Blocco {

    private final String id;

    private  List<String> paths;

    private List<BlockData> blockData = null;

    private  EntityType type;

    public Blocco(String id){
        this.id = id;
    }

    public Blocco(String id, List<String> paths, EntityType type){
        this.id = id;
        this.paths = paths;
        this.type = type;
    }

    public String getId(){
        return this.id;
    }

    public List<String> getPaths(){
        return this.paths;
    }

    @Override
    public String toString(){
        return this.id;
    }

    public List<BlockData> makeDataList() {
        if(this.blockData==null) {
            this.blockData = new ArrayList<>();
            for(String path : this.paths){
                String[] split = path.split("/");
                this.blockData.add(new BlockData(this, split[0], split[1], type));
            }
        }
        return this.blockData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blocco blocco)) return false;

        if (!Objects.equals(id, blocco.id)) return false;
        return type == blocco.type;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}

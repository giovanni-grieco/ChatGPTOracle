package it.uniroma3.LLMOracle.data;

import java.util.ArrayList;
import java.util.List;

public class Blocco {

    private final String id;

    private final List<String> paths;

    private List<BlockData> blockData = null;

    private final EntityType type;

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
}

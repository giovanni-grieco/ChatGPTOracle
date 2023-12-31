package it.uniroma3.LLMOracle.data;

import it.uniroma3.LLMOracle.Application;

import java.nio.file.Path;

public class BlockData extends Data {

    private final Blocco blocco;

    private EntityType entityType;
    private final String path;

    public BlockData(Blocco blocco, String domain, String id, EntityType type){
        super(domain, id);
        this.blocco = blocco;
        this.entityType = type;
        this.path = Application.appProperties.getDatasetPath()+"/"+Application.appProperties.getDatasetFolders()[type.getTypeIndex()];
    }

    @Override
    public Path toFullPath() {
        return Path.of(this.path+"/"+this.getDomain()+"/"+this.getId()+".html");
    }

    public Blocco getBlocco(){
        return this.blocco;
    }

    @Override
    public String toString(){
        return "BlockData: "+this.getDomain()+"/"+this.getId();
    }
}

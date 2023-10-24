package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.Application;

import java.nio.file.Path;

public class BlockData extends Data {

    private final Blocco blocco;

    private final String path;

    private final EntityType type;

    public BlockData(Blocco blocco, String domain, String id, EntityType type){
        super(domain, id);
        this.blocco = blocco;
        this.type = type;
        this.path = Application.appProperties.getDatasetPath()+"/"+Application.appProperties.getDatasetFolders()[this.type.getTypeIndex()];
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

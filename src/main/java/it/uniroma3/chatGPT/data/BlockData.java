package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.Application;

import java.nio.file.Path;

public class BlockData extends Data {

    private final Blocco blocco;

    private final String path;

    public BlockData(Blocco blocco, String domain, String id, int type) {
        super(domain, id);
        this.blocco = blocco;
        this.path = Application.appProperties.getDatasetPath()+Application.appProperties.getDatasetFolders()[type];
    }

    @Override
    public Path toFullPath() {
        return Path.of(this.path+"/"+this.getDomain()+"/"+this.getId()+".html");
    }

    public Blocco getBlocco(){
        return this.blocco;
    }
}

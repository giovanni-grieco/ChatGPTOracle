package it.uniroma3.chatGPT.data;

import java.nio.file.Path;

public class Data {
    private String domain;
    private String id;

    public Data(String domain, String id) {
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

    public Path toPath(){
        return Path.of(domain+"/"+id);
    }

    @Override
    public String toString() {
        return this.domain+"/"+this.id;
    }

}

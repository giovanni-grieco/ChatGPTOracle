package it.uniroma3.LLMOracle.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Blocco {

    private final String id;

    private List<BlockData> blockData;

    public Blocco(String id) {
        this.id = id;
    }

    public Blocco(String id, List<BlockData> data) {
        this.id = id;
        this.blockData = data;
    }

    public String getId() {
        return this.id;
    }


    @Override
    public String toString() {
        return this.id;
    }

    public List<BlockData> getDataList() {
        return this.blockData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Blocco blocco = (Blocco) o;

        return Objects.equals(id, blocco.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

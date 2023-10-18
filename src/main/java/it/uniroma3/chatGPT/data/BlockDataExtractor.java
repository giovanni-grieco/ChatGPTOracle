package it.uniroma3.chatGPT.data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BlockDataExtractor {

    private final String groundTruthPath;

    private Map<String, List<String>> block2PathList;
    private long enumerator;

    private final EntityType EType;

    public BlockDataExtractor(String groundTruthPath, EntityType type) throws IOException {
        this.groundTruthPath = groundTruthPath;
        this.block2PathList = new HashMap<>();
        this.extractBlocks();
        this.enumerator = 0L;
        this.EType = type;
    }

    public String nextBlockName(){
        return block2PathList.keySet().iterator().next();
    }

    public Blocco nextBlock(){
        String block = block2PathList.keySet().iterator().next();
        return new Blocco(this.nextBlockName(), block2PathList.remove(block), this.EType.getTypeIndex());
    }

    public boolean hasNextBlock(){
        return !block2PathList.isEmpty();
    }

    private void extractBlocks() throws IOException {
        FileReader fr = new FileReader(groundTruthPath);
        Iterable<CSVRecord> records = CSVFormat.Builder.create().setHeader().setSkipHeaderRecord(true).build().parse(fr);
        for(CSVRecord record : records){
            String row = record.get(0);
            String[] columns = row.split(";");
            String block = columns[0];
            for(int i=1; i<columns.length; i++){
                String unfilteredPath = columns[i];
                String path = filtraPath(unfilteredPath);
                addToMapList(block, path, block2PathList);
            }
        }
        fr.close();
    }

    private <E,T> void addToMapList(T key, E value, Map<T, List<E>> map){
        if(map.containsKey(key)){
            map.get(key).add(value);
        }else{
            map.put(key, new ArrayList<>(List.of(value)));
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Block2PathList:");
        for(String block : block2PathList.keySet()){
            sb.append("\n").append(block).append("-> ");
            for(String path : block2PathList.get(block)){
                sb.append(path).append(", ");
            }
        }
        return sb.toString();
    }

    public String filtraPath(String string){
        return string.split("/file:/Users/rvoyat/git/weir/dataset/alaska/camera/")[1].replaceAll(".json",".html");
    }
}

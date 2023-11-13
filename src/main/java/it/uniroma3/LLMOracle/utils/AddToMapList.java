package it.uniroma3.LLMOracle.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddToMapList {
    public static <K, E> void addToMapList(K key, E value, Map<K, List<E>> map){
        if(map.containsKey(key)){
            map.get(key).add(value);
        }else{
            map.put(key, new ArrayList<>(List.of(value)));
        }
    }
}

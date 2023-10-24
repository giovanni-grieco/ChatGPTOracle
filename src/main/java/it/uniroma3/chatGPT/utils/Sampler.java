package it.uniroma3.chatGPT.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Sampler <T>{

    private final int numberOfSamples;
    private Collection<T> collection;

    private final List<T> unsampledList;

    public Sampler(int numberOfSamples, Collection<T> collection){
        this.numberOfSamples = numberOfSamples;
        this.unsampledList = new ArrayList<>(collection);
    }

    public List<T> sampleCollection(){
        List<T> sampled = new ArrayList<>();
        if(numberOfSamples > unsampledList.size()){
            return unsampledList;
        }
        for(int i = 0; i < numberOfSamples; i++){
            int randomIndex = (int) (Math.random() * unsampledList.size());
            sampled.add(unsampledList.get(randomIndex));
            unsampledList.remove(randomIndex);
        }
        return sampled;
    }







}

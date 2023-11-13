package it.uniroma3.LLMOracle.GPT.segmentazione;

public class SegmentationException extends RuntimeException{
    public SegmentationException(String message, Throwable t){
        super(message,t);
    }
}

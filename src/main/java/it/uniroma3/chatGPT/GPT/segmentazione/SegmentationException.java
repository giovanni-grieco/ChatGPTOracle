package it.uniroma3.chatGPT.GPT.segmentazione;

public class SegmentationException extends RuntimeException{
    public SegmentationException(String message, Throwable t){
        super(message,t);
    }
}

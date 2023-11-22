package it.uniroma3.LLMOracle.GPT.tokenizer;

public class TokenizationException extends RuntimeException{
    public TokenizationException(String message, Throwable t){
        super(message,t);
    }
}

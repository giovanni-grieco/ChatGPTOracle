package it.uniroma3.LLMOracle.GPT.segmentazione;

import com.didalgo.gpt3.Encoding;
import com.didalgo.gpt3.GPT3Tokenizer;

import java.util.Collections;
import java.util.List;

public class Segmenter {
    private int maxTokens;
    private final GPT3Tokenizer tokenizer;

    private final String string2beSegmented;

    private List<Integer> tokens = null;
    public Segmenter(String string2beSegmented){
        this.tokenizer = new GPT3Tokenizer(Encoding.CL100K_BASE);
        this.string2beSegmented = string2beSegmented;
        this.tokens = tokenizer.encode(this.string2beSegmented);
    }

    public String getNextToken(){
        try {
            return tokenizer.decode(Collections.singletonList(tokens.remove(0)));
        }catch(Exception e){
            throw new SegmentationException("Out of tokens", e.getCause());
        }
    }

    public boolean hasNextToken(){
        return !this.tokens.isEmpty();
    }

    public String getNextNTokens(int n){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<n && this.hasNextToken(); i++){
            sb.append(this.getNextToken());
        }
        return sb.toString();
    }

    public int getTokenAmount(){
        return this.tokens.size();
    }

    public String getString2BeSegmented(){
        return this.string2beSegmented;
    }


}

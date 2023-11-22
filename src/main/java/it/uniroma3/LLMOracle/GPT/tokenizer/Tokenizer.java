package it.uniroma3.LLMOracle.GPT.tokenizer;

import com.didalgo.gpt3.Encoding;
import com.didalgo.gpt3.GPT3Tokenizer;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.ModelType;

import java.util.Collections;
import java.util.List;

public class Tokenizer {
    private int maxTokens;
    private final EncodingRegistry registry;
    private final com.knuddels.jtokkit.api.Encoding enc;

    private final String string2beSegmented;

    private List<Integer> tokens = null;

    public Tokenizer(String string2beSegmented){
        this.registry = Encodings.newDefaultEncodingRegistry();
        this.enc = registry.getEncodingForModel(ModelType.GPT_3_5_TURBO);
        this.string2beSegmented = string2beSegmented;
        this.tokens = this.enc.encode(this.string2beSegmented);
    }

    public String getNextToken(){
        try {
            return enc.decode(Collections.singletonList(tokens.remove(0)));
        }catch(Exception e){
            throw new TokenizationException("Out of tokens", e.getCause());
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

    public String getFirstNCharacters(int n){
        return this.string2beSegmented.substring(0, n);
    }

}

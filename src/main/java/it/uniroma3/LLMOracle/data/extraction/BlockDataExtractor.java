package it.uniroma3.LLMOracle.data.extraction;

import it.uniroma3.LLMOracle.data.Blocco;

public interface BlockDataExtractor {
    public String nextBlockName();
    public Blocco nextBlock();

    public boolean hasNextBlock();
}

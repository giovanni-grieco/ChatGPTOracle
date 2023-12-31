package it.uniroma3.LLMOracle.GPT.segmentazione;

import static org.junit.Assert.assertEquals;

public class SegmenterTest {

    private Segmenter segmenter;

    @org.junit.Test
    public void getNextToken1Token() {
        String testString1 = "he";
        this.segmenter = new Segmenter(testString1);
        assertEquals(1, this.segmenter.getTokenAmount());
        assertEquals("he", this.segmenter.getNextToken());
    }

    @org.junit.Test
    public void getNextTokenMoreTokens() {
        String testString2 = "he;";
        this.segmenter = new Segmenter(testString2);
        assertEquals(2, this.segmenter.getTokenAmount());
        assertEquals("he", this.segmenter.getNextToken());
    }

    @org.junit.Test
    public void getNextNTokens() {
        String testString3 = "hello world, how are you?";
        this.segmenter = new Segmenter(testString3);
        assertEquals(testString3, this.segmenter.getNextNTokens(this.segmenter.getTokenAmount()));
    }
}
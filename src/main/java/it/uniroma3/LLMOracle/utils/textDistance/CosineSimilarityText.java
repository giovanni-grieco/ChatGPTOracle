package it.uniroma3.LLMOracle.utils.textDistance;

import org.apache.commons.text.similarity.CosineSimilarity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class CosineSimilarityText {


    public static Double apply(String text1, String text2) {
        CosineSimilarity documentsSimilarity = new CosineSimilarity();

        String textDelimiter = " ";
        Map<CharSequence, Integer> vectorA = Arrays.stream(text1.split(textDelimiter)).collect(Collectors.toMap(character -> character, character -> 1, Integer::sum));
        Map<CharSequence, Integer> vectorB = Arrays.stream(text2.split(textDelimiter)).collect(Collectors.toMap(character -> character, character -> 1, Integer::sum));

        return documentsSimilarity.cosineSimilarity(vectorA, vectorB);
    }
}
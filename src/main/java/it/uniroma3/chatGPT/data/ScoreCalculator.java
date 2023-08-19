package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.GPT.GPTQuery;

import java.util.List;

public class ScoreCalculator {

    public static Score calculateScore(List<GPTQuery> answers, int negativi){
        int truePositive = 0;
        int trueNegative = 0;
        int falsePositive = 0;
        int falseNegative = 0;

        for (int i = 0; i < answers.size(); i++) {
            if (i < negativi) {
                if (!answers.get(i).isYes()) {
                    trueNegative++;
                } else {
                    falsePositive++;
                }
            } else {
                if (answers.get(i).isYes()) {
                    truePositive++;
                } else {
                    falseNegative++;
                }
            }
        }

        return new Score(truePositive, trueNegative, falsePositive, falseNegative);
    }

}

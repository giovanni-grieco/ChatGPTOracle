package it.uniroma3.LLMOracle.GPT.score;

import it.uniroma3.LLMOracle.GPT.GPTQuery;
import it.uniroma3.LLMOracle.GPT.prompt.ClassificationPrompt;

import java.util.List;

public class ScoreCalculator {

    public static Score calculateScore(List<GPTQuery> answers){
        int truePositive = 0;
        int trueNegative = 0;
        int falsePositive = 0;
        int falseNegative = 0;

        for (GPTQuery answer : answers) {
            if (answer.isYes() && ((ClassificationPrompt)answer.getPrompt()).isPositive()) {
                truePositive++;
            } else if (answer.isYes() && !((ClassificationPrompt)answer.getPrompt()).isPositive()) {
                falsePositive++;
            } else if (!answer.isYes() &&((ClassificationPrompt)answer.getPrompt()).isPositive()) {
                falseNegative++;
            } else if (!answer.isYes() && !((ClassificationPrompt)answer.getPrompt()).isPositive()) {
                trueNegative++;
            }
        }

        return new Score(truePositive, trueNegative, falsePositive, falseNegative);
    }

}

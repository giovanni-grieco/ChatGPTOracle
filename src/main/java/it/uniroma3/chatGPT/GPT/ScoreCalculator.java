package it.uniroma3.chatGPT.GPT;

import java.util.List;

public class ScoreCalculator {

    public static Score calculateScore(List<GPTQuery> answers){
        int truePositive = 0;
        int trueNegative = 0;
        int falsePositive = 0;
        int falseNegative = 0;

        for (GPTQuery answer : answers) {
            if (answer.isYes() && answer.getPrompt().isPositive()) {
                truePositive++;
            } else if (answer.isYes() && !answer.getPrompt().isPositive()) {
                falsePositive++;
            } else if (!answer.isYes() && answer.getPrompt().isPositive()) {
                falseNegative++;
            } else if (!answer.isYes() && !answer.getPrompt().isPositive()) {
                trueNegative++;
            }
        }

        return new Score(truePositive, trueNegative, falsePositive, falseNegative);
    }

}
package it.uniroma3.chatGPT.GPT;

public class Score {
    int truePositive = 0;
    int trueNegative = 0;
    int falsePositive = 0;
    int falseNegative = 0;

    protected Score( int truePositive, int trueNegative, int falsePositive, int falseNegative ) {
        this.truePositive = truePositive;
        this.trueNegative = trueNegative;
        this.falsePositive = falsePositive;
        this.falseNegative = falseNegative;
    }

    public double getFScore(){
        return 2 * ((this.getPrecision()* this.getRecall()) / (this.getPrecision() + this.getRecall()));
    }

    public double getPrecision(){
        return (double) truePositive / (truePositive + falsePositive);
    }

    public double getRecall(){
        return (double) truePositive / (truePositive + falseNegative);
    }

    @Override
    public String toString(){
        return "True Positive: " + this.truePositive + "\n" +
                "True Negative: " + this.trueNegative + "\n" +
                "False Positive: " + this.falsePositive + "\n" +
                "False Negative: " + this.falseNegative + "\n" +
                "Precision: " + this.getPrecision() + "\n" +
                "Recall: " + this.getRecall() + "\n" +
                "F-Score: " + this.getFScore();
    }


}

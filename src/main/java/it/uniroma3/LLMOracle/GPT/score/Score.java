package it.uniroma3.LLMOracle.GPT.score;

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

    public int getTP(){
        return this.truePositive;
    }

    public int getTN(){
        return this.trueNegative;
    }

    public int getFP(){
        return this.falsePositive;
    }

    public int getFN(){
        return this.falseNegative;
    }

    public double getFScore(){
        return 2 * ((this.getPrecision()* this.getRecall()) / (this.getPrecision() + this.getRecall()));
    }

    public double getMCC(){
        return ((double)(truePositive*trueNegative)-(falsePositive*falseNegative))/(Math.sqrt((double)(truePositive+falsePositive)*(truePositive+falseNegative)*(trueNegative+falsePositive)*(trueNegative+falseNegative)));
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
                "F-Score: " + this.getFScore()+"\n"+
                "MCC [-1, 1]: " + this.getMCC()+"\n"+
                "Percentage MCC [0, 1]: "+ ((this.getMCC()+1)/2);
    }

}

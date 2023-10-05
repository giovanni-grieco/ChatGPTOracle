package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.comando.Comando;

public class Test implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException {
        System.out.println("Test");
        int tp=3552;
        int tn=5567;
        int fp=715;
        int fn=1089;
        double precision = (double) tp/(tp+fp);
        double recall = (double) tp/(tp+fn);
        double f1score = 2* (double) ((precision*recall)/(precision+recall));
        double numerator = (double) ((tp*tn)-(fp*fn));
        double denominator = Math.sqrt((double) (tp+fp)*(tp+fn)*(tn+fp)*(tn+fn));
        System.out.println(numerator);
        System.out.println(denominator);
        double mcc = (double) numerator/denominator;
        System.out.println("F1 score: "+f1score);
        System.out.println("MCC: "+mcc);
    }
}

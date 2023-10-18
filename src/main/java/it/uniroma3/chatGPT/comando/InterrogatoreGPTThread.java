package it.uniroma3.chatGPT.comando;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.*;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.utils.FileSaver;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class InterrogatoreGPTThread extends Thread {

    private final Application application;

    private final List<Entity> entityList;

    private final int entityIndex;

    private final int percentualePositivi;

    private final int numeroDiPromptTotali;

    private Score finalScore;

    private String blocco;

    public InterrogatoreGPTThread(Application application, int entityIndex, int percentageOfPositive, int numeroDiPromptTotali, List<Entity> entityList) {
        this.entityIndex = entityIndex;
        this.percentualePositivi = percentageOfPositive;
        this.numeroDiPromptTotali = numeroDiPromptTotali;
        this.entityList = entityList;
        this.application = application;
        this.blocco = null;
    }

    public InterrogatoreGPTThread(Application application, int entityIndex, int percentageOfPositive, int numeroDiPromptTotali, List<Entity> entityList, String blocco){
        this(application, entityIndex, percentageOfPositive, numeroDiPromptTotali, entityList);
        this.blocco = blocco;
    }

    @Override
    public void run() {
        try {
            int numeroPositivi = (int) (numeroDiPromptTotali / 100 * percentualePositivi);
            int numeroNegativi = numeroDiPromptTotali - numeroPositivi;
            List<Entity> filteredEntityList = new ArrayList<>();
            for (Entity e : entityList) {
                if (e.getType() == entityIndex)
                    filteredEntityList.add(e);
            }
            PromptBuilder pb = new PromptBuilder(filteredEntityList, numeroPositivi, numeroNegativi, false);
            List<Prompt> prompts = new ArrayList<>();
            pb.generateNonMatchingEntityPrompts(prompts);
            pb.generateMatchingEntityPrompts(prompts);
            System.out.println("Prompts size: " + prompts.size());
            System.out.println("Inizio interrogazione...");
            LLM gpt = new AzureGPT("You will be given 2 snippets of texts. You will have to answer whether the 2 texts are talking about the same entity, object or subject. Answer only with yes or no.");
            //String modello = "curie:ft-personal-2023-08-19-19-36-38";
            String modello = "gpt-3.5-turbo";
            List<GPTQuery> answers = null;
            answers = gpt.processPrompts(prompts, modello, 0);

            this.finalScore = ScoreCalculator.calculateScore(answers);

            String results = this.finalScore.toString();
            results += "\n"+"positive prompts: " + numeroPositivi + "\n";
            results += "negative prompts: " + numeroNegativi + "\n";
            results += "percentage of positive prompts: " + percentualePositivi + "\n";
            results += "model: " + modello + "\n";
            System.out.println(results);

            LocalDate now = LocalDate.now();
            LocalTime nowTime = LocalTime.now();
            String fileName = application.getAppProperties().getDatasetFolders()[entityIndex] + "-" + now + "_" + nowTime.getHour() + "-" + nowTime.getMinute() + "-" + nowTime.getSecond()+"-"+blocco+percentualePositivi+"%";
            FileSaver.saveFile("./results/", fileName + ".txt", results + "\n\n" + modello);
            System.out.println("File saved as ./results/" + fileName + ".txt");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Score getScore(){
        return this.finalScore;
    }

    public String getBlocco(){
        return this.blocco;
    }

    public Score getFinalScore() {
        return this.getScore();
    }

    public int getPercentualePositivi() {
        return this.percentualePositivi;
    }
}

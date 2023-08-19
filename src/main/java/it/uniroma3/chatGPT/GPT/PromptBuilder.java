package it.uniroma3.chatGPT.GPT;

import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;

import java.util.List;
import java.util.Random;

public class PromptBuilder {

    public static String buildPromptTwoSnippets(String webPageA, String webPageB) {
        String prompt = "";
        prompt += "first: " + webPageA + "##";
        prompt += "second: " + webPageB + "##";
        return prompt;
    }

    public static void generateNonMatchingEntityPrompts(List<Entity> entityList, int amountOfPrompts, List<String> outputPrompts) {
        for (int i = 0; i < amountOfPrompts; i++) {
            try {
                Random random = new Random();
                int randomNumber = random.nextInt();
                randomNumber = Math.abs(randomNumber);
                randomNumber = randomNumber % entityList.size();
                Entity e1 = entityList.get(randomNumber);
                int anotherRandomNumber = random.nextInt() % entityList.size();
                anotherRandomNumber = Math.abs(anotherRandomNumber);
                int maxRetries = 10;
                int retries = 0;
                while (anotherRandomNumber == randomNumber && retries < maxRetries) {
                    anotherRandomNumber = random.nextInt() % entityList.size();
                    anotherRandomNumber = Math.abs(anotherRandomNumber);
                    retries++;
                }
                Entity e2 = entityList.get(anotherRandomNumber);
                //estraiamo 2 informazioni a caso dalle entità
                int randomDataNumber1 = random.nextInt() % e1.getData().size();
                randomDataNumber1 = Math.abs(randomDataNumber1);
                int randomDataNumber2 = random.nextInt() % e2.getData().size();
                randomDataNumber2 = Math.abs(randomDataNumber2);
                String dataE1 = e1.getData().get(randomDataNumber1).getTextData();
                String dataE2 = e2.getData().get(randomDataNumber2).getTextData();
                //Filtriamo le informazioni
                String pureDataE1 = HTMLFilter.filterTemplate(dataE1, HTMLFilter.DEFAULT_TO_REMOVE_TAGS, e1.getData().get(randomDataNumber1).getDomain());
                String pureDataE2 = HTMLFilter.filterTemplate(dataE2, HTMLFilter.DEFAULT_TO_REMOVE_TAGS, e2.getData().get(randomDataNumber2).getDomain());
                //Creiamo il prompt
                outputPrompts.add(buildPromptTwoSnippets(pureDataE1, pureDataE2));
            } catch (Exception e) {
                i--;
                e.printStackTrace();
                System.out.println("Exception while creating prompt: " + e.getMessage());
            }
        }
    }

    public static void generateMatchingEntityPrompts(List<Entity> entityList, int amountOfPrompts, List<String> outputPrompts) {
        for (int i = 0; i < amountOfPrompts; i++) {
            try {
                Random random = new Random();
                int randomNumber = random.nextInt();
                randomNumber = randomNumber % entityList.size();
                randomNumber = Math.abs(randomNumber);
                Entity e1 = entityList.get(randomNumber);
                int randomDataNumber1 = random.nextInt() % e1.getData().size();
                int randomDataNumber2 = random.nextInt() % e1.getData().size();
                randomDataNumber1 = Math.abs(randomDataNumber1);
                randomDataNumber2 = Math.abs(randomDataNumber2);
                int maxRetries = 3;
                int retries = 0;
                while (randomDataNumber1 == randomDataNumber2 && retries < maxRetries) {
                    randomDataNumber2 = random.nextInt() % e1.getData().size();
                    randomDataNumber2 = Math.abs(randomDataNumber2);
                    retries++;
                }
                String data1 = e1.getData().get(randomDataNumber1).getTextData();
                String data2 = e1.getData().get(randomDataNumber2).getTextData();
                //Filtriamo le informazioni
                String pureDataE1 = HTMLFilter.filterTemplate(data1, HTMLFilter.DEFAULT_TO_REMOVE_TAGS, e1.getData().get(randomDataNumber1).getDomain());
                String pureDataE2 = HTMLFilter.filterTemplate(data2, HTMLFilter.DEFAULT_TO_REMOVE_TAGS, e1.getData().get(randomDataNumber2).getDomain());
                    /*System.out.println(pureDataE1);
                    System.out.println(pureDataE2);*/
                outputPrompts.add(buildPromptTwoSnippets(pureDataE1, pureDataE2));
            } catch (Exception e) {
                i--;
                e.printStackTrace();
                System.out.println("Exception while creating prompt: " + e.getMessage());
            }
        }
    }

}

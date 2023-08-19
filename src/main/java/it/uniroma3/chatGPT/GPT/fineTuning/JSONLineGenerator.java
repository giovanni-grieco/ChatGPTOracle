package it.uniroma3.chatGPT.GPT.fineTuning;

import java.util.List;

public class JSONLineGenerator {
    public static String generateJSONLines(List<String> prompts){
        StringBuilder sb = new StringBuilder();
        int iterator = 0;
        for (String p : prompts) {
            sb.append("{");
            sb.append("\"prompt\":\"").append(p.toLowerCase()).append("###").append("\",");
            if (iterator <= prompts.size() / 2) {
                sb.append(" \"completion\":\" no\"");
            } else {
                sb.append("\"completion\":\" yes\"");
            }
            sb.append("}\n");
            iterator++;
        }
        return sb.toString();
    }
}

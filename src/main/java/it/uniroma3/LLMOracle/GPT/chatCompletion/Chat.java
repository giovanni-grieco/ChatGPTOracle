package it.uniroma3.LLMOracle.GPT.chatCompletion;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private final List<ChatElement> chatElements;
    public Chat(){
        chatElements = new ArrayList<>();
    }

    public Chat(Chat chat){
        chatElements = new ArrayList<>(chat.getChatElements());
    }

    private List<ChatElement> getChatElements() {
        return chatElements;
    }

    public Chat addUserChatMessage(String message){
        chatElements.add(new ChatElement(message, "user"));
        return this;
    }

    public Chat addSystemChatAnswer(String answer){
        chatElements.add(new ChatElement(answer, "assistant"));
        return this;
    }

    public String toJson(){
        StringBuilder result= new StringBuilder();
        for(int i = 0;i<chatElements.size()-1;i++){
            result.append(chatElements.get(i).toJson()).append(",");
        }
        result.append(chatElements.get(chatElements.size()-1).toJson());
        return result.toString();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(ChatElement ce : chatElements){
            sb.append(ce.toString()).append("\n");
        }
        return sb.toString();
    }

    public boolean isEmpty() {
        return chatElements.isEmpty();
    }
}

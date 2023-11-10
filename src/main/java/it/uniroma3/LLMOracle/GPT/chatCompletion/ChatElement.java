package it.uniroma3.LLMOracle.GPT.chatCompletion;

public class ChatElement {
    private String textChatElement;

    private String role;

    public ChatElement(String text, String role){
        this.textChatElement = text;
        this.role = role;
    }

    public String toJson(){
        return "{\"role\":\""+role+"\",\"content\":\""+textChatElement+"\"}";
    }

    @Override
    public String toString(){
        return "From "+role+": "+textChatElement;
    }

}

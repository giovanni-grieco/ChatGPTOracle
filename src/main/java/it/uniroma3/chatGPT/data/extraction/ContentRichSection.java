package it.uniroma3.chatGPT.data.extraction;

public class ContentRichSection {

    String templateName;
    String start;
    String end;

    public ContentRichSection(String templateName,String start, String end) {
        this.templateName = templateName;
        this.start = start;
        this.end = end;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public String toString(){
        return "templateName: "+templateName+"\nstart: "+start+"\nend: "+end;
    }
}

package it.uniroma3.chatGPT.data.extraction;

import java.util.List;

public class XPaths {

    private String templateName;

    private List<String> xPaths;


    public XPaths(String templateName, List<String> xPaths) {
        this.templateName = templateName;
        this.xPaths = xPaths;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setXPaths(List<String> xPaths) {
        this.xPaths = xPaths;
    }

    public List<String> getXPaths() {
        return xPaths;
    }

    @Override
    public String toString() {
        return "templateName: " + templateName + " xPaths: " + xPaths.toString();
    }
}

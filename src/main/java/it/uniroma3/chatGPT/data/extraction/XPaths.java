package it.uniroma3.chatGPT.data.extraction;

import java.util.List;

public class XPaths {
    private final String templateName;
    private final List<String> xPaths;

    public XPaths(String templateName, List<String> xPaths) {
        this.templateName = templateName;
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

package it.uniroma3.chatGPT.data.extraction;

import java.util.List;

public class XPaths {
    private final String templateDomainName;
    private final List<String> xPaths;

    public XPaths(String templateName, List<String> xPaths) {
        this.templateDomainName = templateName;
        this.xPaths = xPaths;
    }

    public List<String> getXPaths() {
        return xPaths;
    }

    @Override
    public String toString() {
        return "templateName: " + templateDomainName + " xPaths: " + xPaths.toString();
    }
}

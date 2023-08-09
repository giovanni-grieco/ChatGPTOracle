package it.uniroma3.chatGPT.data.extraction;

import it.uniroma3.chatGPT.utils.FileRetriever;
import it.uniroma3.chatGPT.utils.FileSaver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HTMLFilter {

    public static final Iterable<String> DEFAULT_TAGS = List.of("style", "script", "head", "meta", "img", "link", "br", "input","wbr","embed","area","param");


    public static String filterTemplate(String html, Iterable<String> tagsToRemove, String templateName) throws HTMLTemplateException, IOException {
        HTMLFilter.loadXPaths(templateName, FileRetriever.getFile("contentRichTemplate.txt"));
        String partialFilter = filter(html, tagsToRemove);
        // System.out.println("Template: "+templateName);
        //System.out.println("partialFilter: " + partialFilter);
        FileSaver.saveFile("C:/Users/giovi/Desktop/","partialFilter.txt",partialFilter);
        XPaths xPath = loadXPaths(templateName, FileRetriever.getFile("contentRichTemplate.txt"));
        int xPathIndex = 0;
        do{
            XPathExtractor xPathExtractor = new XPathExtractor(xPath.getXPaths().get(xPathIndex), partialFilter);
            String result = xPathExtractor.extract();
            if(result!=null && result.length()>1){
                return result;
            }
            xPathIndex++;
        }while(xPathIndex<xPath.getXPaths().size());
        return null;
    }

    private static String filter(String html, Iterable<String> tagsToRemove) {
        Document doc = Jsoup.parse(html);
        removeSpecifiedTags(doc, tagsToRemove);
        removeHTMLAttributes(doc);
        removeEmptyTags(doc);
        return doc.toString(); //ritorna l'html con i suoi tag
        //return doc.text(); //ritorna il testo senza i tag
    }

    private static String convert2Text(Document doc) {
        //sostituisce i div con \n
        doc.select("div").append("\\n");
        return doc.select("body").text().replaceAll("\\\\n", "\n");
    }

    private static void removeSpecifiedTags(Document doc, Iterable<String> tagsToRemove) {
        //rimuove i tag specificati
        for (String tag : tagsToRemove) {
            doc.select(tag).remove();
        }
    }

    private static void removeEmptyTags(Document doc) {
        //rimuove tutti i tag vuoti
        for (Element element : doc.select("*")) {
            if (!element.hasText() && element.isBlock()) {
                element.remove();
            }
        }
    }

    private static void removeHTMLAttributes(Document doc) {
        //rimuove tutti gli attributi dei tag HTML
        Elements el = doc.getAllElements();
        for (Element e : el) {
            List<String> attToRemove = new ArrayList<>();
            Attributes at = e.attributes();
            for (Attribute a : at) {
                attToRemove.add(a.getKey());
            }
            for (String att : attToRemove) {
                e.removeAttr(att);
            }
        }
    }

    private static XPaths loadXPaths(String templateName, File contentRichTemplateInformation) throws HTMLTemplateException, IOException {
        String contentRichTemplate = Files.readString(contentRichTemplateInformation.toPath());
        String[] contentRichTemplateLines = contentRichTemplate.split("\n");
        XPaths xpaths = new XPaths(templateName, new ArrayList<String>());
        for (String line : contentRichTemplateLines) {
            if (line.startsWith(templateName)) {
                String[] templateDetails = line.split("-");
                for(int i = 1; i<templateDetails.length-1; i++){
                    xpaths.getXPaths().add(templateDetails[i]);
                }
                return xpaths;
            }
        }
        throw new HTMLTemplateException("Template '" + templateName + "'not found in contentRichTemplate.txt");
    }
}

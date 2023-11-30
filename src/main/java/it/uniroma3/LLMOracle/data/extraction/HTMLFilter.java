package it.uniroma3.LLMOracle.data.extraction;

import it.uniroma3.LLMOracle.utils.file.FileRetriever;
import it.uniroma3.LLMOracle.utils.file.FileSaver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HTMLFilter {

    public static final Iterable<String> DEFAULT_TO_REMOVE_TAGS = List.of("style", "script", "head", "meta", "img", "link", "br", "input","wbr","embed","area","param");

    public static String filterTemplate(String html, Iterable<String> tagsToRemove, String templateName) throws HTMLTemplateException, IOException {
        String partialFilter = filter(html, tagsToRemove);
        XPaths xPaths = loadXPaths(templateName, FileRetriever.getFile("./templates_xpaths.txt"));
        int xPathIndex = 0;
        String result = null;
        if(!xPaths.getXPaths().isEmpty()) {
            do {
                XPathExtractor xPathExtractor = new XPathExtractor(xPaths.getXPaths().get(xPathIndex), partialFilter);
                result = xPathExtractor.extract();
                if (result != null && (result.isEmpty() || result.isBlank())) {
                    result = null;
                }
                xPathIndex++;
            } while (xPathIndex < xPaths.getXPaths().size() && result == null);
        }
        if(result==null){
            LocalDate now = LocalDate.now();
            LocalTime nowTime = LocalTime.now();
            String fileName = templateName+now+ "_" + nowTime.getHour() +"-error";
            System.err.println("Unable to extract valid information from XPaths specified in the template: "+templateName);
            System.err.println("Saving the html page at: ./errorCausingHtmlPages/"+fileName+".html");
            FileSaver.saveFile("./errorCausingHtmlPages/", fileName + ".html", partialFilter);
            throw new HTMLTemplateException("Unable to extract valid information from XPaths specified in the template: "+templateName);
        }
        return result.trim();
    }

    public static String getTitle(String html) {
        Document doc = Jsoup.parse(html);
        return doc.title();
    }

    public static String getTitleQuick(String html){
        int titleStartIndex = html.indexOf("<title>");
        int titleEndIndex = html.indexOf("</title>");
        if(titleStartIndex==-1 || titleEndIndex==-1){
            return null;
        }
        String title = html.substring(titleStartIndex+7,titleEndIndex).replaceAll("\n","");
        title = removeChar(title, '"');
        title = removeChar(title , '\n');
        return title;
    }
    public static String removeChar(String text, char c){
        StringBuilder sb = new StringBuilder();
        for(char ch : text.toCharArray()){
            if(ch!=c){
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String filter(String html, Iterable<String> tagsToRemove) {
        Document doc = Jsoup.parse(html);
        removeSpecifiedTags(doc, tagsToRemove);
        removeHTMLAttributes(doc);
        removeHTMLComments(doc);
        removeEmptyTags(doc);
        return doc.toString(); //ritorna l'html con i suoi tag
        //return doc.text(); //ritorna il testo senza i tag
    }

    public static String filterText(String html, Iterable<String> tagsToRemove) {
        Document doc = Jsoup.parse(html);
        removeSpecifiedTags(doc, tagsToRemove);
        removeHTMLAttributes(doc);
        removeHTMLComments(doc);
        removeEmptyTags(doc);
        //return doc.toString(); //ritorna l'html con i suoi tag
        return doc.text(); //ritorna il testo senza i tag
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

    private static void removeHTMLComments(Document doc){
        Elements el = doc.getAllElements();
        for (Element e : el) {
            removeComments(e);
        }
    }

    private static void removeComments(Element article) {
        article.filter(new NodeFilter() {
            @Override
            public FilterResult tail(Node node, int depth) {
                if (node instanceof Comment) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult head(Node node, int depth) {
                if (node instanceof Comment) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }
        });
    }

    private static XPaths loadXPaths(String templateName, File contentRichTemplateInformation) throws HTMLTemplateException, IOException {
        String contentRichTemplate = Files.readString(contentRichTemplateInformation.toPath());
        String[] contentRichTemplateLines = contentRichTemplate.split("\n");
        XPaths xpaths = new XPaths(templateName, new ArrayList<String>());
        for (String line : contentRichTemplateLines) {
            if (line.startsWith(templateName)) {
                String[] templateDetails = line.split("-");
                for(int i = 1; i<templateDetails.length-2; i++){
                    xpaths.getXPaths().add(templateDetails[i]);
                }
                return xpaths;
            }
        }
        FileWriter fw = new FileWriter("templates_xpaths.txt",true);
        fw.write("\n"+templateName+"---");
        fw.close();
        throw new HTMLTemplateException("Template '" + templateName + "'not found in templates_xpaths.txt\nAdding template to file.");
    }
}

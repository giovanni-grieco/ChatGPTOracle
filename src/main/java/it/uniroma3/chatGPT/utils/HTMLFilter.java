package it.uniroma3.chatGPT.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HTMLFilter {
    public static String filter(String html, Iterable<String> tagsToRemove){
        Document doc = Jsoup.parse(html);
        for(String tag : tagsToRemove){
            doc.select(tag).remove();
        }
        return doc.html();
    }
}

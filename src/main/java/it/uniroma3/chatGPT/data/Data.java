package it.uniroma3.chatGPT.data;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class Data {

    private final Entity entity;
    private String domain;
    private String id;

    public Data(String domain, String id, Entity entity) {
        this.entity = entity;
        this.domain = domain;
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Path toFullPath(){
        return Path.of(Application.appProperties.getDatasetPath()+ "/" + Application.appProperties.getDatasetFolders()[this.entity.getType()] + "/" + domain + "/" + id + ".html");
    }

    /**
     * Returns the html page referenced by this data object
     * @return html page string
     * @throws IOException
     */
    public String getTextData() throws IOException {
        return Cache.getTextData(this);
    }


    @Override
    public String toString() {
        return this.domain + "/" + this.id;
    }

    /**
     * Returns the title field of a specified html page referenced by this data object
     * @return title field string
     * @throws IOException
     */
    public String getTitle() throws IOException {
        return Cache.getTitle(this);
    }

    /**
     * Avoids the cache and loads directly from disk
     * @return
     */
    public String getTextDataFromDisk() {
        try {
            return Files.readString(this.toFullPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Avoids the cache and reruns the parser in order to get the title
     * @return
     */
    public String getTitleFromParser(){
        try {
            return HTMLFilter.getTitle(this.getTextData());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Cache {
        private static final Semaphore titleSemaphore = new Semaphore(1);

        private static final Semaphore textSemaphore = new Semaphore(1);

        private static final int maxCacheSize = 1000;

        private static final int maxTitleCacheSize = maxCacheSize*100;

        private static final int maxTextCacheSize = maxCacheSize;
        private static final Map<Data, Integer> data2CacheIndexTitle = new HashMap<>();
        //cache
        private static final Map<Data, String> data2Title = new HashMap<>();
        //cache
        private static final Map<Data, Integer> data2CacheIndexTextData = new HashMap<>();
        private static final Map<Data, String> data2TextData = new HashMap<>();

        private static final AtomicLong titlehits = new AtomicLong(0);

        private static final AtomicLong titlemisses = new AtomicLong(0);

        private static final AtomicLong texthits = new AtomicLong(0);

        private static final AtomicLong textmisses = new AtomicLong(0);


        private static String getTitle(Data d) throws IOException {
            try {
                titleSemaphore.acquire();
                if (data2Title.containsKey(d)) {
                    data2CacheIndexTitle.put(d, data2CacheIndexTitle.get(d) + 1);
                    titlehits.incrementAndGet();
                    return data2Title.get(d);
                } else {
                    titlemisses.incrementAndGet();
                    if (data2Title.size() == maxTitleCacheSize) {
                        //deallochiamo il meno usato
                        Data dataToRemove = new ArrayList<>(data2CacheIndexTitle.keySet()).get(0);
                        Integer minimumUsage = data2CacheIndexTitle.get(dataToRemove);
                        for (Data data : data2CacheIndexTitle.keySet()) {
                            if (data2CacheIndexTitle.get(data) < minimumUsage) {
                                dataToRemove = data;
                                minimumUsage = data2CacheIndexTitle.get(data);
                            }
                        }
                        data2Title.remove(dataToRemove);
                        data2CacheIndexTitle.remove(dataToRemove);
                    }
                    String title = d.getTitleFromParser();
                    data2CacheIndexTitle.put(d, 0);
                    data2Title.put(d, title);
                    return title;
                }
            } catch (Exception e) {
                System.err.println(dumpCacheInfo());
                System.err.println("Data: " + d);
                System.err.println("Is contained in text data map: " + data2TextData.containsKey(d));
                System.err.println("Is contained in text index map: " + data2CacheIndexTextData.containsKey(d));
                System.err.println("Is contained in title map: " + data2Title.containsKey(d));
                System.err.println("Is contained in title index map: " + data2CacheIndexTitle.containsKey(d));
                throw new RuntimeException(e);
            }finally{
                titleSemaphore.release();
            }
        }

        private static String getTextData(Data d) throws IOException {
            try {
                textSemaphore.acquire();
                if (data2TextData.containsKey(d)) {
                    data2CacheIndexTextData.put(d, data2CacheIndexTextData.get(d) + 1);
                    texthits.incrementAndGet();
                    return data2TextData.get(d);
                } else {
                    textmisses.incrementAndGet();
                    if (data2TextData.size() == maxTextCacheSize) {
                        //deallochiamo il meno usato
                        Data dataToRemove = new ArrayList<>(data2CacheIndexTextData.keySet()).get(0);
                        Integer minimumUsage = data2CacheIndexTextData.get(dataToRemove);
                        for (Data data : data2CacheIndexTextData.keySet()) {
                            if (data2CacheIndexTextData.get(data) < minimumUsage) {
                                dataToRemove = data;
                                minimumUsage = data2CacheIndexTextData.get(data);
                            }
                        }
                        data2TextData.remove(dataToRemove);
                        data2CacheIndexTextData.remove(dataToRemove);
                    }
                    String textData = d.getTextDataFromDisk();
                    data2CacheIndexTextData.put(d, 0);
                    data2TextData.put(d, textData);
                    return textData;
                }
            } catch (Exception e) {
                System.err.println(dumpCacheInfo());
                System.err.println("Data: " + d);
                System.err.println("Is contained in text data map: " + data2TextData.containsKey(d));
                System.err.println("Is contained in text index map: " + data2CacheIndexTextData.containsKey(d));
                System.err.println("Is contained in title map: " + data2Title.containsKey(d));
                System.err.println("Is contained in title index map: " + data2CacheIndexTitle.containsKey(d));
                throw new RuntimeException(e);
            }finally{
                textSemaphore.release();
            }
        }

        public static String dumpCacheInfo(){
            String dump;
            dump = "data2Title.size(): " + data2Title.size() + "\n";
            dump += "data2CacheIndexTitle.size(): " + data2CacheIndexTitle.size() + "\n";
            dump += "data2TextData.size(): " + data2TextData.size() + "\n";
            dump += "data2CacheIndexData.size(): " + data2CacheIndexTextData.size() + "\n";
            dump += "title semaphore available permits: " + titleSemaphore.availablePermits() + "\n";
            dump += "text semaphore available permits: " + textSemaphore.availablePermits() + "\n";
            dump += "hits: " + titlehits.get() + "\n";
            dump += "misses: " + titlemisses.get() + "\n";
            dump += "hits: " + texthits.get() + "\n";
            dump += "misses: " + textmisses.get() + "\n";
            return dump;
        }

    }
}

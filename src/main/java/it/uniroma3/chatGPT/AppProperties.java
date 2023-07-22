package it.uniroma3.chatGPT;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {
    private static final String appPropertiesFile = "app.properties";
    private final Properties appProperties;
    public AppProperties() throws IOException {
        appProperties = new Properties();
        appProperties.load(new FileInputStream(appPropertiesFile));
    }

    public String getAPIKey(){
        System.out.println(appProperties.getProperty("OpenAI_API_KEY"));
        return appProperties.getProperty("OpenAI_API_KEY");
    }

}

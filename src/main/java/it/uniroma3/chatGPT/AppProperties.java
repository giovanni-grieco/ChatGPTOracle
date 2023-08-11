package it.uniroma3.chatGPT;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {

    private static AppProperties appProperties;
    private static final String appPropertiesFile = "app.properties";
    private final Properties properties;

    private AppProperties() throws IOException {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(appPropertiesFile));
        } catch (IOException e) {
            throw new IOException("app.properties file not found! It must be placed at the root of classpath");
        }
    }

    public static AppProperties getAppProperties() throws IOException {
        if (appProperties == null) {
            appProperties = new AppProperties();
        }
        return appProperties;
    }

    public String getAPIKey() {
        return properties.getProperty("OpenAI_API_KEY");
    }

    public String getDatasetPath() {
        return properties.getProperty("DATASET_ROOT_PATH");
    }

    public String getDatasetFolder() {
        return properties.getProperty("DATASET_FOLDER_NAME");
    }

    public String getGroundTruthFileName() {
        return properties.getProperty("DATASET_GT_FILE_NAME");
    }

}

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

    public String[] getDatasetFolders() {
        return properties.getProperty("DATASET_FOLDER_NAME").split(",");
    }

    public String[] getGroundTruthFileNames() {
        return properties.getProperty("DATASET_GT_FILE_NAME").split(",");
    }

    public void validate() throws InvalidPropertiesException {
        if(this.getAPIKey()==null || this.getAPIKey().isEmpty() || this.getAPIKey().isBlank()) throw new InvalidPropertiesException("OpenAI key null or empty");
        if(this.getDatasetPath()==null || this.getDatasetPath().isEmpty() || this.getDatasetPath().isBlank()) throw new InvalidPropertiesException("Dataset folder not specified");
        if(this.getDatasetFolders().length == 0 || this.getGroundTruthFileNames().length == 0) throw new InvalidPropertiesException("");
    }

    public void validate() throws InvalidPropertiesException {
        if (this.getAPIKey() == null || this.getAPIKey().isEmpty() || this.getAPIKey().isBlank())
            throw new InvalidPropertiesException("OpenAI key null or empty");
        if (this.getDatasetPath() == null || this.getDatasetPath().isEmpty() || this.getDatasetPath().isBlank())
            throw new InvalidPropertiesException("Dataset Root Path not specified");
        if (this.getDatasetFolder() == null || this.getDatasetFolder().isEmpty() || this.getDatasetFolder().isBlank())
            throw new InvalidPropertiesException("Dataset Folder not specified");
        if (this.getGroundTruthFileName() == null || this.getGroundTruthFileName().isEmpty() || this.getGroundTruthFileName().isBlank())
            throw new InvalidPropertiesException("Ground Truth File Name not specified");
    }
}

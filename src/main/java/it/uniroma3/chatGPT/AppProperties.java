package it.uniroma3.chatGPT;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {

    private static AppProperties appProperties;
    private static final String appPropertiesFile = "app.properties";

    private final String APIKey;

    private final String datasetPath;

    private final String[] datasetFolders;

    private final String[] groundTruthFileNames;

    private AppProperties() throws IOException {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(appPropertiesFile));
            this.APIKey=properties.getProperty("OpenAI_API_KEY");
            this.datasetPath=properties.getProperty("DATASET_ROOT_PATH");
            this.datasetFolders=properties.getProperty("DATASET_FOLDER_NAME").split(",");
            this.groundTruthFileNames=properties.getProperty("DATASET_GT_FILE_NAME").split(",");
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
        return this.APIKey;
    }

    public String getDatasetPath() {
        return this.datasetPath;
    }

    public String[] getDatasetFolders() {
        return this.datasetFolders;
    }

    public String[] getGroundTruthFileNames() {
        return this.groundTruthFileNames;
    }

    public void validate() throws InvalidPropertiesException {
        if(this.getAPIKey()==null || this.getAPIKey().isEmpty() || this.getAPIKey().isBlank()) throw new InvalidPropertiesException("OpenAI key null or empty");
        if(this.getDatasetPath()==null || this.getDatasetPath().isEmpty() || this.getDatasetPath().isBlank()) throw new InvalidPropertiesException("Dataset folder not specified");
        if(this.getDatasetFolders().length == 0 || this.getGroundTruthFileNames().length == 0) throw new InvalidPropertiesException("Missing data or ground truths");
        if(this.getDatasetFolders().length!=this.getGroundTruthFileNames().length) throw new InvalidPropertiesException("Number of data folders and ground truths must be the same");
    }
}

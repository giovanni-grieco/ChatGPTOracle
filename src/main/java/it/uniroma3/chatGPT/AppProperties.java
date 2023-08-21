package it.uniroma3.chatGPT;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {

    private static AppProperties appProperties;
    private static final String appPropertiesFile = "app.properties";

    private final String APIKey;

    private final String datasetPath;

    private String[] datasetFolders;

    private String[] groundTruthFileNames;

    private final String datasetFoldersRaw;

    private final String groundTruthFileNamesRaw;

    private AppProperties() throws IOException, InvalidPropertiesException {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(appPropertiesFile));
            this.APIKey=properties.getProperty("OpenAI_API_KEY");
            this.datasetPath=properties.getProperty("DATASET_ROOT_PATH");
            this.datasetFoldersRaw=properties.getProperty("DATASET_FOLDER_NAME");
            this.groundTruthFileNamesRaw=properties.getProperty("DATASET_GT_FILE_NAME");
            this.validate();
            this.parseDatasetFolders();
            this.parseGroundTruthFileNames();
            this.validateSecondStage();
        } catch (IOException e) {
            throw new IOException("app.properties file not found! It must be placed at the root of classpath");
        }
    }

    public static AppProperties getAppProperties() throws IOException, InvalidPropertiesException {
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

    public void parseDatasetFolders(){
        this.datasetFolders=this.datasetFoldersRaw.split(",");
    }

    public void parseGroundTruthFileNames(){
        this.groundTruthFileNames=this.groundTruthFileNamesRaw.split(",");
    }

    public void validate() throws InvalidPropertiesException {
        if(this.getAPIKey()==null || this.getAPIKey().isEmpty() || this.getAPIKey().isBlank()) throw new InvalidPropertiesException("OpenAI_API_KEY field missing or empty");
        if(this.getDatasetPath()==null || this.getDatasetPath().isEmpty() || this.getDatasetPath().isBlank()) throw new InvalidPropertiesException("Dataset folder not specified");
        if(this.datasetFoldersRaw==null || this.datasetFoldersRaw.isEmpty() || this.datasetFoldersRaw.isBlank()) throw new InvalidPropertiesException("Dataset folders not specified");
        if(this.groundTruthFileNamesRaw==null || this.groundTruthFileNamesRaw.isEmpty() || this.groundTruthFileNamesRaw.isBlank()) throw new InvalidPropertiesException("Ground truth files not specified");
    }

    public void validateSecondStage() throws InvalidPropertiesException {
        if(this.getDatasetFolders().length!=this.getGroundTruthFileNames().length) throw new InvalidPropertiesException("Dataset folders and ground truth files number mismatch");
    }
}

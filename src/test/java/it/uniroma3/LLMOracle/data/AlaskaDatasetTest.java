package it.uniroma3.LLMOracle.data;

import it.uniroma3.LLMOracle.AppProperties;
import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.InvalidPropertiesException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AlaskaDatasetTest {

    private AppProperties appProperties;

    private Application app;
    private AlaskaDataset dataset;

    @Before
    public void setUp() throws IOException, InvalidPropertiesException {
        this.appProperties = AppProperties.getAppProperties();
        this.app = new Application(appProperties);
        this.dataset = new AlaskaDataset();
    }

    @Test
    public void getEntityByData() {
        Data data = new BlockData(null,"www.ebay.com","45973", EntityType.CAMERA);
        System.out.println(dataset);
        System.out.println(dataset.getEntities());
        Entity entity = this.dataset.getEntityByData(data);
        assertNotNull(entity);
    }
}
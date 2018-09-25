package tasks;

import io.github.cbadenes.crosslingual.data.Dataset;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatasetTasks {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetTasks.class);
    private static final Double TRAINING_RATIO = 0.6;
    private static Dataset dataset;


    @BeforeClass
    public static final void setup() throws IOException {
        dataset = new Dataset("https://delicias.dia.fi.upm.es/nextcloud/index.php/s/owKeTiGfz68KE4s/download");
    }

    /**
     * Monolingual Evaluation
     */
    @Test
    public void prepareTest1() throws IOException {
        dataset.export("test1", Arrays.asList("en"),TRAINING_RATIO);
    }

    /**
     * BiLingual Evaluation
     */
    @Test
    public void prepareTest2() throws IOException {
        dataset.export("test2", Arrays.asList("en","es"),TRAINING_RATIO);
    }

    /**
     * MultiLingual Evaluation
     */
    @Test
    public void prepareTest3() throws IOException {
        dataset.export("test3", Arrays.asList("en","es","pt"),TRAINING_RATIO);
    }

}

package tasks;

import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DownloadDatasetsTasks {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadDatasetsTasks.class);

    @Test
    public void setup() throws IOException {
        FileUtils.copyURLToFile(new URL("https://delicias.dia.fi.upm.es/nextcloud/index.php/s/LsrxLzAKkbonbEy/download"), new File("corpora.zip"));
    }


}

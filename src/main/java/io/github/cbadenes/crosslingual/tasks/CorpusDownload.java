package io.github.cbadenes.crosslingual.tasks;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CorpusDownload {

    private static final Logger LOG = LoggerFactory.getLogger(CorpusDownload.class);

    public static final String PATH = "corpus/papers.json.gz";


    public static void main(String[] args) throws IOException {


        File outputFile = new File(PATH);

        outputFile.getParentFile().mkdirs();

        if (outputFile.exists()) outputFile.delete();

        FileUtils.copyURLToFile(new URL("https://delicias.dia.fi.upm.es/nextcloud/index.php/s/6Xx8bpz79cwCJns/download"),outputFile);

        LOG.info("Articles downloaded at: " + PATH);


    }
}

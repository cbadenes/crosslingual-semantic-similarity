package io.github.cbadenes.crosslingual.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cbadenes.crosslingual.data.Relation;
import io.github.cbadenes.crosslingual.utils.ReaderUtils;
import io.github.cbadenes.crosslingual.utils.WriterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CorpusSplitTrainAndTest {

    private static final Logger LOG = LoggerFactory.getLogger(CorpusSplitTrainAndTest.class);

    public static final String TRAIN_DATASET = "corpus/papers-for-train.json.gz";

    public static final String TEST_DATASET = "corpus/papers-for-test.json.gz";

    public static final Double THRESHOLD = 0.65;


    public static void main(String[] args) throws IOException {


        // Read valid similarity relations
        BufferedReader reader = ReaderUtils.from(CorpusPrepare.PATH);

        ObjectMapper mapper = new ObjectMapper();

        String line;

        Set<String> testIds = new TreeSet<>();

        LOG.info("Getting papers to be used as tests from  '" + CorpusPrepare.PATH + "' ..");

        // Load papers in map
        while ((line = reader.readLine()) != null){
            Relation relation = mapper.readValue(line, Relation.class);
            if (relation.getScore() > THRESHOLD) {
                testIds.add(relation.getX());
                testIds.add(relation.getY());
            }
        }
        reader.close();

        LOG.info(testIds.size() + " papers reserved for evaluations");

        // Create TRAIN datasets

        BufferedReader paperReader  = ReaderUtils.from(CorpusDownload.PATH);
        BufferedWriter trainWriter  = WriterUtils.to(TRAIN_DATASET);
        BufferedWriter testWriter   = WriterUtils.to(TEST_DATASET);

        double testSize    = 0;
        double trainSize   = 0;
        while ((line = paperReader.readLine()) != null){
            JsonNode json = mapper.readTree(line);

            String id = json.get("id").asText();
            if (testIds.contains(id)){
                testSize++;
                testWriter.write(line + "\n");
            }else{
                trainSize++;
                trainWriter.write(line + "\n");
            }

        }
        paperReader.close();
        trainWriter.close();
        testWriter.close();

        LOG.info("Train-Dataset: " + trainSize + " papers (" + ((trainSize/(trainSize+testSize))*100.0) + "%)");
        LOG.info("Test-Dataset (>"+THRESHOLD+"): " + testSize + " papers (" + ((testSize/(trainSize+testSize))*100.0) + "%)");


    }

}

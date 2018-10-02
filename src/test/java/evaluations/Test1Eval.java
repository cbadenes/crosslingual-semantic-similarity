package evaluations;

import cc.mallet.util.MalletLogger;
import com.google.common.collect.ImmutableMap;
import io.github.cbadenes.crosslingual.algorithms.NlpSimilarityAlgorithm;
import io.github.cbadenes.crosslingual.algorithms.SimpleSimilarityAlgorithm;
import io.github.cbadenes.crosslingual.algorithms.SimilarityAlgorithm;
import io.github.cbadenes.crosslingual.data.AccuracyReport;
import io.github.cbadenes.crosslingual.data.Document;
import io.github.cbadenes.crosslingual.data.Relation;
import io.github.cbadenes.crosslingual.utils.ReaderUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Test1Eval {

    private static final Logger LOG = LoggerFactory.getLogger(Test1Eval.class);
    private static List<Document> devDocs;
    private static List<Document> testDocs;
    private static Map<String, List<Relation>> simDocs;

    private static final Double GOLD_THRESHOLD  = 0.55;
    private static final Double ALG_THRESHOLD   = 0.55;

    static{
        LogManager.getLogManager().reset();
    }


    @BeforeClass
    public static void setup() throws IOException {
        // Read Training Data
        devDocs     = ReaderUtils.readDocuments("corpora/test1/training-set.jsonl.gz");

        // Read Test Data
        testDocs    = ReaderUtils.readDocuments("corpora/test1/test-set.jsonl.gz");

        // Read Similarities
        simDocs     = ReaderUtils.readRelations("corpora/test1/similarity-set.jsonl.gz");

    }


    @Test
    public void simpleAlgorithm(){
        evaluateAlgorithm(new SimpleSimilarityAlgorithm());
    }


    @Test
    public void nlpAlgorithm(){
        evaluateAlgorithm(new NlpSimilarityAlgorithm());
    }



    private void evaluateAlgorithm(SimilarityAlgorithm algorithm){
        // Prepare Parameters
        Map<String, String> parameters = ImmutableMap.of("GOLD", String.valueOf(GOLD_THRESHOLD), "ALGORITHM", String.valueOf(ALG_THRESHOLD));

        // Compare documents
        Map<String, List<Relation>> similarities = algorithm.compare(devDocs, testDocs, 0.5);

        // Check results at: 1, 5, 10 and 20 precision
        List<AccuracyReport> reports = Arrays.asList(new AccuracyReport(1, parameters), new AccuracyReport(5, parameters), new AccuracyReport(10, parameters), new AccuracyReport(20, parameters),new AccuracyReport(50, parameters));
        for(String doc : simDocs.keySet()){

            List<String> goldStandard = simDocs.get(doc).parallelStream().filter(rel -> rel.getScore() > GOLD_THRESHOLD).sorted((a, b) -> -a.getScore().compareTo(b.getScore())).map(rel -> rel.getY()).collect(Collectors.toList());
            List<String> calculatedRelations = similarities.containsKey(doc)? similarities.get(doc).parallelStream().filter(rel -> rel.getScore() > ALG_THRESHOLD).sorted((a, b) -> -a.getScore().compareTo(b.getScore())).map(rel -> rel.getY()).collect(Collectors.toList()) : Collections.emptyList();

            reports.parallelStream().forEach( report -> report.addResult(goldStandard, calculatedRelations));
        }

        reports.forEach( report -> LOG.info(algorithm.id() + " Algorithm : " + report));

    }

}

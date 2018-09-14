package io.github.cbadenes.crosslingual.algorithms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.github.cbadenes.crosslingual.data.AccuracyReport;
import io.github.cbadenes.crosslingual.data.Evaluation;
import io.github.cbadenes.crosslingual.services.LibrairyService;
import io.github.cbadenes.crosslingual.tasks.CorpusSplitTrainAndTest;
import io.github.cbadenes.crosslingual.utils.WriterUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ParsingBasedAlgorithmsEvaluation {

    private static final Logger LOG = LoggerFactory.getLogger(ParsingBasedAlgorithmsEvaluation.class);

    private static BufferedWriter writer;
    private static String testId;
    private static ObjectMapper jsonMapper;
    private static LibrairyService librairyService;
    private static List tests;
    private static List evals;

    @BeforeClass
    public static void setup() throws IOException {
        jsonMapper = new ObjectMapper();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        testId = formatter.format(new Date());

        writer = WriterUtils.to("reports/" + testId + ".json.gz");


        tests = new ArrayList();
        tests.add(ImmutableMap.of("topics","10","iterations","1000"));
        tests.add(ImmutableMap.of("topics","50","iterations","1000"));
        tests.add(ImmutableMap.of("topics","100","iterations","1000"));
        tests.add(ImmutableMap.of("topics","500","iterations","1000"));

        evals = new ArrayList<>();
        evals.add(ImmutableMap.of("gold-threshold","0.6","threshold","0.6"));
        evals.add(ImmutableMap.of("gold-threshold","0.7","threshold","0.7"));
        evals.add(ImmutableMap.of("gold-threshold","0.8","threshold","0.8"));
        evals.add(ImmutableMap.of("gold-threshold","0.9","threshold","0.9"));
        evals.add(ImmutableMap.of("gold-threshold","0.6","threshold","0.7"));
        evals.add(ImmutableMap.of("gold-threshold","0.6","threshold","0.8"));
        evals.add(ImmutableMap.of("gold-threshold","0.6","threshold","0.9"));
    }

    @AfterClass
    public static void shutdown() throws IOException {
        writer.close();
    }

    @Test
    public void raw() throws IOException, InterruptedException {
        evaluate(new WordBasedParser());
    }

    @Test
    public void lemma() throws IOException, InterruptedException {
        evaluate(new LemmaBasedParser(Arrays.asList(PoS.NOUN)));
    }

    @Test
    public void lemmaVerb() throws IOException, InterruptedException {
        evaluate(new LemmaBasedParser(Arrays.asList(PoS.NOUN,PoS.VERB, PoS.ADJECTIVE)));
    }

    private void evaluate(Parser parser) throws IOException {
        Evaluation evaluation = new Evaluation(testId,CorpusSplitTrainAndTest.TRAIN_DATASET, CorpusSplitTrainAndTest.TEST_DATASET, parser);
        List<AccuracyReport> reports = evaluation.execute(tests, evals);
        reports.forEach(report -> {try {writer.write(jsonMapper.writeValueAsString(report) + "\n");} catch (IOException e) {e.printStackTrace();}});
    }

}

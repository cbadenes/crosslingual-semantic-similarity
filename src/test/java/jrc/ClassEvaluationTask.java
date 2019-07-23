package jrc;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.github.cbadenes.crosslingual.data.ResultReport;
import io.github.cbadenes.crosslingual.io.ParallelExecutor;
import io.github.cbadenes.crosslingual.io.SolrClient;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ClassEvaluationTask {

    private static final Logger LOG = LoggerFactory.getLogger(ClassEvaluationTask.class);

    Map<String,DescriptiveStatistics> precisionResults  = new ConcurrentHashMap<>();
    Map<String,DescriptiveStatistics> recallResults     = new ConcurrentHashMap<>();
    Map<String,DescriptiveStatistics> fMeasureResults   = new ConcurrentHashMap<>();
    Map<String,DescriptiveStatistics> sizeResults       = new ConcurrentHashMap<>();

    @Before
    public void setup(){
        initializeResults(precisionResults);
        initializeResults(recallResults);
        initializeResults(fMeasureResults);
        initializeResults(sizeResults);
    }

    @Test
    public void test1(){
        evaluate("test1", 1000);
    }


    @Test
    public void test1_1(){
        evaluate("test1_1", 1000);
    }

    @Test
    public void test1_2(){
        evaluate("test1_2", 1000);
    }

    @Test
    public void test1_3(){
        evaluate("test1_3", 1000);
    }


    @Test
    public void test2_1(){
        evaluate("test2_1", 1000);
    }

    @Test
    public void test2_2(){
        evaluate("test2_2", 1000);
    }

    @Test
    public void test2_4(){
        evaluate("test2_4", 1000);
    }

    @Test
    public void test3_2(){
        evaluate("test3_2", 1050);
    }


    public void evaluate(String collection, Integer sampleSize) {

        try{

            SolrClient input = new SolrClient("http://librairy.linkeddata.es/solr/"+collection);
            input.open();

            SolrClient.SolrIterator inputIterator = input.query("*:*", Arrays.asList(
                    "id",
                    "name_s",
                    "labels_t",
                    "topics0_t",
                    "topics1_t",
                    "topics2_t",
                    "synset0_t",
                    "synset1_t",
                    "synset2_t"
            ), sampleSize);

            Optional<Map<String, Object>> doc = Optional.empty();
            AtomicInteger counter = new AtomicInteger();

            ParallelExecutor executor = new ParallelExecutor();

            while((doc = inputIterator.next()).isPresent()){

                final Map<String, Object> values = doc.get();

                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            String id       = (String) values.get("id");
                            LOG.info("analyzing " + id);
                            String labelsVal   = (String) values.get("labels_t");

                            List<String> labels         = (!Strings.isNullOrEmpty(labelsVal))? Arrays.stream((labelsVal).split(" ")).collect(Collectors.toList()): Collections.emptyList();
                            List<String> topics0        = Arrays.stream(((String) values.get("topics0_t")).split(" ")).collect(Collectors.toList());
                            List<String> topics1        = Arrays.stream(((String) values.get("topics1_t")).split(" ")).collect(Collectors.toList());
                            List<String> topics2        = Arrays.stream(((String) values.get("topics2_t")).split(" ")).collect(Collectors.toList());
                            List<String> synset0        = Arrays.stream(((String) values.get("synset0_t")).split(" ")).collect(Collectors.toList());
                            List<String> synset1        = Arrays.stream(((String) values.get("synset1_t")).split(" ")).collect(Collectors.toList());
                            List<String> synset2        = Arrays.stream(((String) values.get("synset2_t")).split(" ")).collect(Collectors.toList());





//            LOG.info("------------------------- > MLT" + id);
//            LOG.info("by cpv-codes: -> " + input.getMoreLikeThis(id, Arrays.asList("cpv_codes_t"), 1000).size() + " docs");
//            LOG.info("by cpv-divisions: -> " + input.getMoreLikeThis(id, Arrays.asList("cpv_divisions_t"), 1000).size() + " docs");
//            LOG.info("by cpv-groups: -> " + input.getMoreLikeThis(id, Arrays.asList("cpv_groups_t"), 1000).size() + " docs");
//            LOG.info("by cpv-classes: -> " + input.getMoreLikeThis(id, Arrays.asList("cpv_classes_t"), 1000).size() + " docs");
//            LOG.info("by cpv-categories: -> " + input.getMoreLikeThis(id, Arrays.asList("cpv_categories_t"), 1000).size() + " docs");
//            LOG.info("by topics0: -> " + input.getMoreLikeThis(id, Arrays.asList("topics0_t"), 1000).size() + " docs");
//            LOG.info("by topics1: -> " + input.getMoreLikeThis(id, Arrays.asList("topics1_t"), 1000).size() + " docs");
//            LOG.info("by topics2: -> " + input.getMoreLikeThis(id, Arrays.asList("topics2_t"), 1000).size() + " docs");
//            LOG.info("by topics: -> " + input.getMoreLikeThis(id, Arrays.asList("topics0_t","topics1_t","topics2_t"), 1000).size() + " docs");
//            LOG.info("by synset0: -> " + input.getMoreLikeThis(id, Arrays.asList("synset0_t"), 1000).size() + " docs");
//            LOG.info("by synset1: -> " + input.getMoreLikeThis(id, Arrays.asList("synset1_t"), 1000).size() + " docs");
//            LOG.info("by synset2: -> " + input.getMoreLikeThis(id, Arrays.asList("synset2_t"), 1000).size() + " docs");

                            Map<String, Double> relatedByLabels           = input.getBooleanQuery(ImmutableMap.of("labels_t", labels), 1000, false, false);

                            Map<String, Double> relatedByTopics           = input.getBooleanQuery(ImmutableMap.of("topics0_t",topics0, "topics1_t",topics1, "topics2_t", topics2), 1000,false, false);
//                            Map<String, Double> relatedByTopics0          = input.getBooleanQuery(ImmutableMap.of("topics0_t",topics0), 1000,false, true);
//                            Map<String, Double> relatedByTopics1          = input.getBooleanQuery(ImmutableMap.of("topics1_t",topics1), 1000,false, true);
//                            Map<String, Double> relatedByTopics2          = input.getBooleanQuery(ImmutableMap.of("topics2_t",topics2), 1000,false, true);

                            Map<String, Double> relatedBySynset           = input.getBooleanQuery(ImmutableMap.of("synset0_t",synset0, "synset1_t",synset1, "synset2_t", synset2), 1000,false, false);
//                            Map<String, Double> relatedBySynset0          = input.getBooleanQuery(ImmutableMap.of("synset0_t",synset0), 1000,false, true);
//                            Map<String, Double> relatedBySynset1          = input.getBooleanQuery(ImmutableMap.of("synset1_t",synset1), 1000,false, true);
//                            Map<String, Double> relatedBySynset2          = input.getBooleanQuery(ImmutableMap.of("synset2_t",synset2), 1000,false, true);


                            List<String> groundTruth                       = relatedByLabels.keySet().stream().collect(Collectors.toList());

                            saveResults("topics", new ResultReport(groundTruth, relatedByTopics.keySet().stream().collect(Collectors.toList())));
                            List<String> sortedRelByTopics = relatedByTopics.entrySet().stream().sorted((a, b) -> -a.getValue().compareTo(b.getValue())).map(e -> e.getKey()).skip(1).collect(Collectors.toList());
                            saveResults("topics@3", new ResultReport(new ArrayList<>(groundTruth), sortedRelByTopics.stream().limit(3).collect(Collectors.toList()) ));
                            saveResults("topics@5", new ResultReport(new ArrayList<>(groundTruth), sortedRelByTopics.stream().limit(5).collect(Collectors.toList()) ));
                            saveResults("topics@10", new ResultReport(new ArrayList<>(groundTruth), sortedRelByTopics.stream().limit(10).collect(Collectors.toList()) ));
//                            saveResults("topics0", new ResultReport(groundTruth, relatedByTopics0.keySet()));
//                            saveResults("topics1", new ResultReport(groundTruth, relatedByTopics1.keySet()));
//                            saveResults("topics2", new ResultReport(groundTruth, relatedByTopics2.keySet()));
                            saveResults("synset", new ResultReport(groundTruth, relatedBySynset.keySet().stream().collect(Collectors.toList())));
                            List<String> sortedRelBySynset = relatedBySynset.entrySet().stream().sorted((a, b) -> -a.getValue().compareTo(b.getValue())).map(e -> e.getKey()).skip(1).collect(Collectors.toList());
                            saveResults("synset@3", new ResultReport(new ArrayList<>(groundTruth), sortedRelBySynset.stream().limit(3).collect(Collectors.toList())));
                            saveResults("synset@5", new ResultReport(new ArrayList<>(groundTruth), sortedRelBySynset.stream().limit(5).collect(Collectors.toList())));
                            saveResults("synset@10", new ResultReport(new ArrayList<>(groundTruth), sortedRelBySynset.stream().limit(10).collect(Collectors.toList())));
//                            saveResults("synset0", new ResultReport(groundTruth, relatedBySynset0.keySet()));
//                            saveResults("synset1", new ResultReport(groundTruth, relatedBySynset1.keySet()));
//                            saveResults("synset2", new ResultReport(groundTruth, relatedBySynset2.keySet()));

                            LOG.info("[" + counter.incrementAndGet() + "] analyzed");
                        }catch (Exception e){
                            LOG.error("Unexpected error on execution",e);
                        }
                    }
                });

            }

            executor.awaitTermination(1l, TimeUnit.HOURS);

            LOG.info("## Precision Values: ");
            precisionResults.entrySet().stream().sorted((a,b) -> a.getKey().compareTo(b.getKey())).forEach( e -> LOG.info("\t"+e.getKey() + ": \t" + getSummary(e.getValue())));

            LOG.info("## Recall Values: ");
            recallResults.entrySet().stream().sorted((a,b) -> a.getKey().compareTo(b.getKey())).forEach( e -> LOG.info("\t"+e.getKey() + ": \t" + getSummary(e.getValue())));

            LOG.info("## fMeasure Values: ");
//            for(String mode: precisionResults.keySet().stream().sorted((a,b) -> a.compareTo(b)).collect(Collectors.toList())){
//                double precision    = precisionResults.get(mode).getMean();
//                double recall       = recallResults.get(mode).getMean();
//                double fMeasure     = (2*precision*recall) / (precision+recall);
//                LOG.info("\t"+mode+":\t"+fMeasure);
//            }
            fMeasureResults.entrySet().stream().sorted((a,b) -> a.getKey().compareTo(b.getKey())).forEach( e -> LOG.info("\t"+e.getKey() + ": \t" + getSummary(e.getValue())));

            LOG.info("## Size Values: ");
            sizeResults.entrySet().stream().sorted((a,b) -> a.getKey().compareTo(b.getKey())).forEach( e -> LOG.info("\t"+e.getKey() + ": \t" + getSummary(e.getValue())));

        }catch (Exception e ){
            LOG.error("Unexpected error during test", e);
        }

    }

    private String getSummary(DescriptiveStatistics r){
        return "min:"+r.getMin()+"|max:"+r.getMax()+"|mean:"+r.getMean()+"|dev:"+r.getStandardDeviation();
    }

    private void saveResults(String id, ResultReport report){
        precisionResults.get(id).addValue(report.getPrecision());
        recallResults.get(id).addValue(report.getRecall());
        fMeasureResults.get(id).addValue(report.getFMeasure());
        sizeResults.get(id).addValue(report.getSize());
    }


    private void initializeResults(Map<String,DescriptiveStatistics> results){
        results.put("topics", new DescriptiveStatistics());
        results.put("topics@3", new DescriptiveStatistics());
        results.put("topics@5", new DescriptiveStatistics());
        results.put("topics@10", new DescriptiveStatistics());
        results.put("synset", new DescriptiveStatistics());
        results.put("synset@3", new DescriptiveStatistics());
        results.put("synset@5", new DescriptiveStatistics());
        results.put("synset@10", new DescriptiveStatistics());
    }
}

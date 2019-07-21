package tasks;

import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.github.cbadenes.crosslingual.data.AnnotatedDocument;
import io.github.cbadenes.crosslingual.data.Annotation;
import io.github.cbadenes.crosslingual.data.AnnotationRequest;
import io.github.cbadenes.crosslingual.data.HierarchicalTopics;
import io.github.cbadenes.crosslingual.io.ParallelExecutor;
import io.github.cbadenes.crosslingual.io.SolrClient;
import io.github.cbadenes.crosslingual.io.librAIryClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AnnotateJRCDocumentsTask {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotateJRCDocumentsTask.class);

    static librAIryClient librAIryClient;

    static final String input_endpoint  = "http://librairy.linkeddata.es/solr/jrc";

    static final String input_labels    = "root-labels_t";

    //static final String model_endpoint  = "http://librairy.linkeddata.es/jrc-%%-model";
    static final String model_endpoint  = "http://librairy.linkeddata.es/jrc-%%-model-unsupervised";

    static final String model_labels    = "http://librairy.linkeddata.es/solr/eurovoc";

    @BeforeClass
    public static void setup(){
        librAIryClient = new librAIryClient();
    }


    @AfterClass
    public static void shutdown(){
    }


//    @Test
//    public void test1(){
//
//        annotate("test1",100, "en", true);
//    }

    @Test
    public void test1_1(){

        annotate("test1_1",1000, "en", true);
    }

    @Test
    public void test1_2(){

        annotate("test1_2",1000, "es", true);

    }

    @Test
    public void test1_3(){

        annotate("test1_3",1000, "fr", true);

    }

//    @Test
//    public void test1_4(){
//
//        annotate("test1_4",100, "de", true);
//
//    }

    @Test
    public void test2_1(){

        annotate("test2_1",500, "en",true);
        annotate("test2_1",500, "es",false);

    }

    @Test
    public void test2_2(){

        annotate("test2_2",500, "en",true);
        annotate("test2_2",500, "fr",false);

    }

//    @Test
//    public void test2_3(){
//
//        annotate("test2_3",100, "en", true);
//        annotate("test2_3",100, "de", false);
//
//    }


    @Test
    public void test2_4(){

        annotate("test2_4",500, "es", true);
        annotate("test2_4",500, "fr", false);

    }

//    @Test
//    public void test2_5(){
//
//        annotate("test2_5",100, "es", true);
//        annotate("test2_5",100, "de", false);
//
//    }
//
//    @Test
//    public void test2_6(){
//
//        annotate("test2_6",100, "fr", true);
//        annotate("test2_6",100, "de", false);
//
//    }

//    @Test
//    public void test3_1(){
//
//        annotate("test3_1",100, "en", true);
//        annotate("test3_1",100, "es", false);
//        annotate("test3_1",100, "de", false);
//    }

    @Test
    public void test3_2(){

        annotate("test3_2",300, "en", true);
        annotate("test3_2",300, "es", false);
        annotate("test3_2",300, "fr", false);
    }

//    @Test
//    public void test3_3(){
//
//        annotate("test3_3",100, "en", true);
//        annotate("test3_3",100, "fr", false);
//        annotate("test3_3",100, "de", false);
//    }
//
//    @Test
//    public void test3_4(){
//
//        annotate("test3_4",100, "es", true);
//        annotate("test3_4",100, "fr", false);
//        annotate("test3_4",100, "de", false);
//    }
//
//    @Test
//    public void test4_1(){
//
//        annotate("test4_1",100, "en", true);
//        annotate("test4_1",100, "es", false);
//        annotate("test4_1",100, "fr", false);
//        annotate("test4_1",100, "de", false);
//    }



    public void annotate(String collection, int max, String lang, Boolean delete){

        if (delete) delete(collection);

        String modelEndpoint = model_endpoint.replace("%%",lang);

        // get topics-words
        Map<String, List<String>> topicWords = librAIryClient.getTopics(modelEndpoint);

        SolrClient descriptor    = new SolrClient(model_labels);
        descriptor.open();

        SolrClient input    = new SolrClient(input_endpoint);
        input.open();


        SolrClient output   = new SolrClient("http://librairy.linkeddata.es/solr/"+collection);
        output.open();

        try {
            SolrClient.SolrIterator inputIterator = input.query("lang_s:"+lang+" AND "+input_labels+":[* TO *] AND size_i:[100 TO *]", Arrays.asList("id", "name_s", "txt_t", input_labels), max*2);

            Optional<Map<String, Object>> doc = Optional.empty();
            final AtomicInteger counter =  new AtomicInteger();

            ParallelExecutor executor = new ParallelExecutor();

            while((doc = inputIterator.next()).isPresent() && counter.get() < max){

                final Map<String, Object> values = doc.get();

                executor.submit(new Runnable() {
                    @Override
                    public void run() {

                        if (counter.get() > max) return;

                        try{
                            String id   = (String) values.get("id");
                            String name = (String) values.get("name_s");
                            String txt  = (String) values.get("txt_t");


                            LOG.info("id: " + id);

                            // describe doc by topic distributions
                            StringBuffer content = new StringBuffer();
                            //content.append(name+" .");
                            content.append(StringUtils.repeat(name+" . ",5));
                            content.append(txt.replaceAll("\n",". "));
                            Optional<HierarchicalTopics> topics = librAIryClient.getTopics(content.toString(), modelEndpoint);
                            LOG.info("topics:" + topics);


                            HierarchicalTopics synsets = new HierarchicalTopics();

                            if (topics.isPresent()){

                                for (Integer level : Arrays.asList(0,1,2)){
                                    for(String topic: topics.get().getLevel(level)){

                                        // get synset from topic words
                                        List<String> words = topicWords.get(topic);
                                        int limit = 0;
                                        String synset = null;
                                        while(Strings.isNullOrEmpty(synset) && limit <= 10){
                                            limit += 5;
                                            synset = getSynset(lang, words.stream().limit(limit).collect(Collectors.joining(" ")));
                                        }
                                        if (Strings.isNullOrEmpty(synset)){
                                            LOG.error("No synset for: " + topic + "-" + words);
                                            continue;
                                        }
                                        synsets.addToLevel(level,synset);

                                    }
                                }
                            }
                            LOG.info("synset: " + synsets);

                            HierarchicalTopics topicsVal = topics.get();

                            if (!topicsVal.isValid() || !synsets.isValid()) return;

                            String labels           = Arrays.stream(((String) values.get(input_labels)).split(" ")).distinct().collect(Collectors.joining(" "));

                            AnnotatedDocument annotatedDocument = new AnnotatedDocument();
                            annotatedDocument.setId(id);
                            annotatedDocument.setName((String) values.get("name_s"));
                            annotatedDocument.setLabels(labels);
                            annotatedDocument.setLang(lang);
                            annotatedDocument.setTopics0(topicsVal.getLevel(0).stream().limit(20).collect(Collectors.joining(" ")));
                            annotatedDocument.setTopics1(topicsVal.getLevel(1).stream().limit(20).collect(Collectors.joining(" ")));
                            annotatedDocument.setTopics2(topicsVal.getLevel(2).stream().limit(20).collect(Collectors.joining(" ")));
                            annotatedDocument.setSynset0(synsets.getLevel(0).stream().limit(20).collect(Collectors.joining(" ")));
                            annotatedDocument.setSynset1(synsets.getLevel(1).stream().limit(20).collect(Collectors.joining(" ")));
                            annotatedDocument.setSynset2(synsets.getLevel(2).stream().limit(20).collect(Collectors.joining(" ")));


                            if (counter.incrementAndGet() <= max){
                                output.save(annotatedDocument);
                                LOG.info("["+counter.get()+"] saved");
                            }

                        }catch (Exception e){
                            LOG.error("Unexpected runtime error",e);
                        }
                    }
                });

            }

            executor.awaitTermination(1l, TimeUnit.HOURS);


        } catch (IOException e) {
            LOG.error("Unexpected SOLR error",e);
        }finally {
            output.close();
            input.close();
            descriptor.close();
        }

    }

    private String getSynset(String lang, String txt){
        // get Wordnet synsets from topics
        AnnotationRequest req = new AnnotationRequest();
        req.setFilter(Arrays.asList("NOUN","VERB","ADJECTIVE"));
        req.setLang(lang);
        req.setMultigrams(false);
        req.setReferences(false);
        req.setSynset(true);
        req.setText(txt.toLowerCase());
        List<Annotation> annotations = librAIryClient.getAnnotations(req);
        long noSynsetAnnotation = annotations.stream().filter(a -> a.getSynset().isEmpty()).count();
//                                if (noSynsetAnnotation>0){
//                                    LOG.error("No synset annotation for topic: " + topicDescription);
//                                    continue;
//                                }
        String synset = annotations.stream().flatMap(a -> a.getSynset().stream().map(s -> StringUtils.substringBefore(s,"."))).distinct().limit(10).collect(Collectors.joining(" "));
        return synset;
    }

    private void delete(String collection)  {
        try{
            String endpoint = "http://librairy.linkeddata.es/solr/"+collection+"/update";
            HttpResponse<String> result = Unirest.post(endpoint + "?commit=true")
                    .header("Content-Type", "text/xml")
                    .body("<delete><query>*:*</query></delete>")
                    .asString();
            LOG.info("Delete: " + result.getStatus() +":" + result.getStatus());
            if (result.getStatus() != 200){
                LOG.warn("ERROR deleting collection: " + result.getBody());
            }
        }catch (Exception e){
            LOG.error("Collection not deleted", e);
        }

    }

}

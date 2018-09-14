package io.github.cbadenes.crosslingual.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import io.github.cbadenes.crosslingual.algorithms.Parser;
import io.github.cbadenes.crosslingual.metrics.JensenShannon;
import io.github.cbadenes.crosslingual.services.LibrairyService;
import io.github.cbadenes.crosslingual.tasks.CorpusPrepare;
import io.github.cbadenes.crosslingual.tasks.CorpusSplitTrainAndTest;
import io.github.cbadenes.crosslingual.utils.ParallelExecutor;
import io.github.cbadenes.crosslingual.utils.ReaderUtils;
import io.github.cbadenes.crosslingual.utils.WriterUtils;
import org.librairy.service.learner.facade.rest.model.Document;
import org.librairy.service.modeler.facade.rest.model.Shape;
import org.librairy.service.modeler.facade.rest.model.ShapeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Evaluation {

    private static final Logger LOG = LoggerFactory.getLogger(Evaluation.class);

    private final Parser parser;
    private final LibrairyService librairyService;
    private final ObjectMapper jsonMapper;
    private final String id;

    String trainingSet;

    String testSet;

    List<Integer> precisionList = new ArrayList<>();

    Map<String,String> parameters = new HashMap<>();

    public Evaluation(String id, String trainingSet, String testSet, Parser parser) throws IOException {
        this.id = id;
        this.trainingSet = trainingSet;
        this.testSet = testSet;
        this.parser = parser;

        //librairyService = new LibrairyService("http://librairy.linkeddata.es/cross-topics","oeg","oeg2018");
        librairyService = new LibrairyService("http://localhost:7777","oeg","oeg2018");
        librairyService.reset();


        jsonMapper = new ObjectMapper();

        // Topic Model Parameters
        parameters.put("topics","5");
        parameters.put("iterations","1000");
        parameters.put("alpha","0.1");
        parameters.put("beta","0.01");
        parameters.put("language","en");
        parameters.put("retries","5");
        parameters.put("topwords","50");
        parameters.put("minfreq","5");
        parameters.put("maxdocratio","0.95");
        parameters.put("raw","true");

        // DockerHub Parameters
        parameters.put("contactEmail","cbadenes@fi.upm.es");
        parameters.put("contactName","Carlos Badenes-Olmedo");
        parameters.put("contactUrl","http://cbadenes.github.io/");
        parameters.put("credentials.email","cbadenes@gmail.com");
        parameters.put("credentials.password",System.getenv("DOCKERHUB_PWD"));
        parameters.put("credentials.username","cbadenes");
        parameters.put("description","Topic Model created from a Parallel Corpus by using a " + parser.id() + " parser algorithm");
        parameters.put("title","Cross-lingual Topic Model by " + parser.id());
        parameters.put("licenseName","Apache License Version 2.0");
        parameters.put("licenseUrl","https://www.apache.org/licenses/LICENSE-2.0");



        prepareTrainingSet();
    }

    private void prepareTrainingSet() throws IOException {
        String dockerHubPwd = System.getenv("DOCKERHUB_PWD");

        if (Strings.isNullOrEmpty(dockerHubPwd)) throw new RuntimeException("DockerHub credentials required");

        ObjectMapper mapper = new ObjectMapper();

        BufferedReader trainReader = ReaderUtils.from(trainingSet);

        String line;

        AtomicInteger counter = new AtomicInteger();
        LOG.info("Training a Topic Model from papers at: '" + CorpusSplitTrainAndTest.TRAIN_DATASET+ "' ..");
        Set<String> keywords = new TreeSet<>();
        while ((line = trainReader.readLine()) != null){

            JsonNode json = mapper.readTree(line);

            String id = json.get("id").asText();
            String language = parser.language();
            String name = json.get("articles").get(language).get("title").asText();
            String text = json.get("articles").get(language).get("description").asText() + " " + json.get("articles").get(language).get("content").asText();
            Iterator<JsonNode> it = json.get("articles").get(language).withArray("keywords").iterator();
            while(it.hasNext()){
                String kw = it.next().asText().trim().toLowerCase();
                keywords.add(kw);
            }

            Document document = new Document();
            document.setId(id);
            document.setName(name);
            document.setText(parser.parse(text));
            librairyService.save(document,false,true);

            if (counter.incrementAndGet() % 100 == 0) LOG.info(counter.get() + " papers added");

        }
        trainReader.close();
        LOG.info(counter.get() + " papers added to learner");
    }

    private Map<String,List<Double>> prepareTestSet(){
        BufferedReader testReader = null;
        Map<String,List<Double>> space = new ConcurrentHashMap<>();
        try {
            testReader = ReaderUtils.from(testSet);

            AtomicInteger counter = new AtomicInteger();
            String line;
            ParallelExecutor shapeExecutor = new ParallelExecutor();
            LOG.info("Getting vectorial representation of papers at: '" + testSet+ "' ..");
            while ((line = testReader.readLine()) != null){

                final JsonNode json = jsonMapper.readTree(line);

                shapeExecutor.submit(() -> {
                    String id   = json.get("id").asText();
                    LOG.info("shape of " + id);
                    String language = parser.language();
                    String text = json.get("articles").get(language).get("description").asText() + " " + json.get("articles").get(language).get("content").asText();

                    ShapeRequest request = new ShapeRequest();
                    request.setText(parser.parse(text));
                    Shape shape = librairyService.inference(request);
                    space.put(id,shape.getVector());

                    if (counter.incrementAndGet() % 100 == 0) LOG.info(counter.get() + " papers shaped");

                });


            }
            shapeExecutor.awaitTermination(1, TimeUnit.HOURS);
            testReader.close();

        } catch (Exception e) {
            LOG.error("Unexpected Error");
            return new HashMap<>();
        }
        return space;
    }


    public List<AccuracyReport> execute(List<Map<String,String>> tests, List<Map<String,String>> evals) throws IOException {

        List<AccuracyReport> reports = new ArrayList<>();

        for(Map test : tests){

            // Prepare environment
            Map<String, List<Double>> testingSpace = trainAndShape(test);

            // Calculate metrics
            for(Map eval: evals){
                reports.add(evaluate(testingSpace, eval));
            }
        }
        return reports;
    }

    private Map<String, List<Double>> trainAndShape(Map<String,String> extraParameters){

        extraParameters.entrySet().forEach(entry -> parameters.put(entry.getKey(), entry.getValue()));
        LOG.info("Training a Topic Model by " + parameters);
        librairyService.train(parameters);

        LOG.info("waiting for complete ..");
        while(!librairyService.isCompleted()){try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}}
        LOG.info("Topic Model created!!");

        LOG.info("Creating Docker image from Topic Model with parameters: ");
        parameters.put("credentials.repository","cbadenes/cross-"+parser.id()+":"+id+"_"+parameters.get("topics"));
        librairyService.export(parameters);
        LOG.info("Docker image created and exported to: " + parameters.get("credentials.repository"));

        return prepareTestSet();
    }

    private AccuracyReport evaluate(Map<String, List<Double>> space, Map<String,String> extraParameters) throws IOException {

        extraParameters.entrySet().forEach(entry -> parameters.put(entry.getKey(), entry.getValue()));


        Double goldStandardThreshold = Double.valueOf(parameters.get("gold-threshold"));
        Map<String, Set<String>> goldStandard = createGoldStandard(goldStandardThreshold);

        AccuracyReport report = analyzeResults(space, goldStandard);
        LOG.info("Analysis completed: " + report);
        return report;

    }


    private Map<String,Set<String>>  createGoldStandard(Double threshold){

        LOG.info("Getting relations between papers from gold-standard (>"+threshold + ") ..");

        Map<String,Set<String>> goldStandard = new HashMap<>();

        String line;
        BufferedReader simReader = null;
        try {
            simReader = ReaderUtils.from(CorpusPrepare.PATH);
            ParallelExecutor relExecutor = new ParallelExecutor();
            while ((line = simReader.readLine()) != null) {
                final Relation relation = jsonMapper.readValue(line, Relation.class);
                relExecutor.submit(() -> {
                    if (relation.getScore() > threshold) {

                        // add y to x
                        Set<String> simPapers = new TreeSet<>();

                        if (goldStandard.containsKey(relation.getX())) {
                            simPapers = goldStandard.get(relation.getX());
                        }

                        simPapers.add(relation.getY());
                        goldStandard.put(relation.getX(), simPapers);

                        // add x to y
                        simPapers = new TreeSet<>();

                        if (goldStandard.containsKey(relation.getY())) {
                            simPapers = goldStandard.get(relation.getY());
                        }

                        simPapers.add(relation.getX());
                        goldStandard.put(relation.getY(), simPapers);

                    }

                });
            }

            relExecutor.awaitTermination(1, TimeUnit.HOURS);
            return goldStandard;
        }catch (Exception e){
            LOG.error("Unexpected error",e);
            return new HashMap<>();
        }

    }

    private AccuracyReport analyzeResults (Map<String, List<Double>> space, Map<String,Set<String>> goldStandard){
        LOG.info("Analyzing results");

        AccuracyReport report = new AccuracyReport(Integer.valueOf(parameters.get("n")), parameters, parser.id());

        AtomicInteger counter = new AtomicInteger();

        ParallelExecutor evalExecutor = new ParallelExecutor();

        for (String id : space.keySet()){

            evalExecutor.submit(() -> {
                List<String> relatedPapers = new ArrayList<>(goldStandard.get(id));

                List<Double> vector = space.get(id);

                List<String> calculatedRelatedPapers = space.entrySet().stream().filter(entry -> !entry.getKey().equalsIgnoreCase(id)).map(entry -> new Relation("sim", id, entry.getKey(), JensenShannon.similarity(vector, entry.getValue()))).filter(rel -> rel.getScore() > CorpusSplitTrainAndTest.THRESHOLD).sorted((a, b) -> -a.getScore().compareTo(b.getScore())).limit(50).map(rel -> rel.getY()).collect(Collectors.toList());

                report.addResult(relatedPapers, calculatedRelatedPapers.subList(0, report.getN()));

                if (counter.incrementAndGet() % 100 == 0) LOG.info(counter.get() + " papers evaluated");
            });

        }
        LOG.info(counter.get() + " papers evaluated");

        evalExecutor.awaitTermination(1, TimeUnit.HOURS);

        return report;
    }
}

package io.github.cbadenes.crosslingual.algorithms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import io.github.cbadenes.crosslingual.data.Evaluation;
import io.github.cbadenes.crosslingual.data.Relation;
import io.github.cbadenes.crosslingual.metrics.JensenShannon;
import io.github.cbadenes.crosslingual.services.LibrairyService;
import io.github.cbadenes.crosslingual.tasks.CorpusPrepare;
import io.github.cbadenes.crosslingual.tasks.CorpusSplitTrainAndTest;
import io.github.cbadenes.crosslingual.utils.ParallelExecutor;
import io.github.cbadenes.crosslingual.utils.ReaderUtils;
import io.github.cbadenes.crosslingual.utils.WriterUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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

public class ParsingBasedAlgorithmsEvaluation {

    private static final Logger LOG = LoggerFactory.getLogger(ParsingBasedAlgorithmsEvaluation.class);

    private static final String LANGUAGE = "en";
    private static BufferedWriter writer;
    private static String testId;
    private static ObjectMapper jsonMapper;

    @BeforeClass
    public static void setup() throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH_mm");
        testId = formatter.format(new Date());
        writer = WriterUtils.to("reports/" + testId + ".json");
        jsonMapper = new ObjectMapper();
    }

    @AfterClass
    public static void shutdown() throws IOException {
        writer.close();
    }

    @Test
    public void raw() throws IOException, InterruptedException {
        eval(new WordBasedParser());
    }

    @Test
    public void lemma() throws IOException, InterruptedException {
        eval(new LemmaBasedParser());
    }



    private List<Evaluation> eval(Parser parserAlgorithm) throws IOException, InterruptedException {

        String dockerHubPwd = System.getenv("DOCKERHUB_PWD");

        if (Strings.isNullOrEmpty(dockerHubPwd)) throw new RuntimeException("DockerHub credentials required");

        ObjectMapper mapper = new ObjectMapper();

        LibrairyService librairyService = new LibrairyService("http://librairy.linkeddata.es/cross-topics","oeg","oeg2018");
        librairyService.reset();

        BufferedReader trainReader = ReaderUtils.from(CorpusSplitTrainAndTest.TRAIN_DATASET);

        String line;

        AtomicInteger counter = new AtomicInteger();
        LOG.info("Training a Topic Model from papers at: '" + CorpusSplitTrainAndTest.TRAIN_DATASET+ "' ..");
        Set<String> keywords = new TreeSet<>();
        while ((line = trainReader.readLine()) != null){

            JsonNode json = mapper.readTree(line);

            String id = json.get("id").asText();
            String name = json.get("articles").get(LANGUAGE).get("title").asText();
            String text = json.get("articles").get(LANGUAGE).get("description").asText() + " " + json.get("articles").get(LANGUAGE).get("content").asText();
            Iterator<JsonNode> it = json.get("articles").get(LANGUAGE).withArray("keywords").iterator();
            while(it.hasNext()){
                String kw = it.next().asText().trim().toLowerCase();
                keywords.add(kw);
            }

            Document document = new Document();
            document.setId(id);
            document.setName(name);
            document.setText(parserAlgorithm.parse(text));
            librairyService.save(document,false,true);

            if (counter.incrementAndGet() % 100 == 0) LOG.info(counter.get() + " papers added");

        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("topics","5");
        parameters.put("iterations","10");
        parameters.put("alpha","0.1");
        parameters.put("beta","0.01");
        parameters.put("language",LANGUAGE);
        parameters.put("retries","5");
        parameters.put("topwords","50");
        parameters.put("minfreq","5");
        parameters.put("maxdocratio","0.95");
        parameters.put("raw","true");

        LOG.info("Training a Topic Model by " + parameters);

        librairyService.train(parameters);

        trainReader.close();
        LOG.info("waiting for complete ..");
        while(!librairyService.isCompleted()){
            Thread.sleep(2000);
        }
        LOG.info("Topic Model created!!");

        //TODO export model

        Map<String,String> dockerHubParameters = new HashMap<>();
        dockerHubParameters.put("contactEmail","cbadenes@fi.upm.es");
        dockerHubParameters.put("contactName","Carlos Badenes-Olmedo");
        dockerHubParameters.put("contactUrl","http://cbadenes.github.io/");
        dockerHubParameters.put("credentials.email","cbadenes@gmail.com");
        dockerHubParameters.put("credentials.password","");
        dockerHubParameters.put("credentials.repository","cbadenes/cross-"+parserAlgorithm.id()+":"+testId);
        dockerHubParameters.put("credentials.username","cbadenes");
        dockerHubParameters.put("description","Topic Model created from a Parallel Corpus by using a " + parserAlgorithm.id() + " parser algorithm");
        dockerHubParameters.put("title","Cross-lingual Topic Model by " + parserAlgorithm.id());
        dockerHubParameters.put("licenseName","Apache License Version 2.0");
        dockerHubParameters.put("licenseUrl","https://www.apache.org/licenses/LICENSE-2.0");



        BufferedReader testReader = ReaderUtils.from(CorpusSplitTrainAndTest.TEST_DATASET);

        Map<String,List<Double>> space = new ConcurrentHashMap<>();

        counter.set(0);
        ParallelExecutor shapeExecutor = new ParallelExecutor();
        LOG.info("Getting vectorial representation of papers at: '" + CorpusSplitTrainAndTest.TEST_DATASET+ "' ..");
        while ((line = testReader.readLine()) != null){

            final JsonNode json = mapper.readTree(line);

//            shapeExecutor.submit(() -> {
                String id   = json.get("id").asText();
                LOG.info("shape of " + id);
                String text = json.get("articles").get(LANGUAGE).get("description").asText() + " " + json.get("articles").get(LANGUAGE).get("content").asText();

                ShapeRequest request = new ShapeRequest();
                request.setText(parserAlgorithm.parse(text));
                Shape shape = librairyService.inference(request);
                space.put(id,shape.getVector());

                if (counter.incrementAndGet() % 100 == 0) LOG.info(counter.get() + " papers shaped");

//            });


        }
//        shapeExecutor.awaitTermination(1, TimeUnit.HOURS);
        testReader.close();

        LOG.info("Getting relations between papers from gold-standard (>"+CorpusSplitTrainAndTest.THRESHOLD + ") ..");

        Map<String,Set<String>> goldStandard = new HashMap<>();

        BufferedReader simReader = ReaderUtils.from(CorpusPrepare.PATH);
        ParallelExecutor relExecutor = new ParallelExecutor();
        while ((line = simReader.readLine()) != null){
            final Relation relation = mapper.readValue(line, Relation.class);
            relExecutor.submit(() -> {
                if (relation.getScore() > CorpusSplitTrainAndTest.THRESHOLD){

                    // add y to x
                    Set<String> simPapers = new TreeSet<>();

                    if (goldStandard.containsKey(relation.getX())){
                        simPapers =  goldStandard.get(relation.getX());
                    }

                    simPapers.add(relation.getY());
                    goldStandard.put(relation.getX(), simPapers);

                    // add x to y
                    simPapers = new TreeSet<>();

                    if (goldStandard.containsKey(relation.getY())){
                        simPapers =  goldStandard.get(relation.getY());
                    }

                    simPapers.add(relation.getX());
                    goldStandard.put(relation.getY(), simPapers);

                }

            });
        }

        relExecutor.awaitTermination(1, TimeUnit.HOURS);

        LOG.info("Analyzing results");

        List<Evaluation> evaluations = Arrays.asList(1, 5, 10, 20, 50).stream().map(n -> new Evaluation(n, parameters)).collect(Collectors.toList());

        counter.set(0);

        ParallelExecutor evalExecutor = new ParallelExecutor();

        for (String id : space.keySet()){

            evalExecutor.submit(() -> {
                List<String> relatedPapers = new ArrayList<>(goldStandard.get(id));

                List<Double> vector = space.get(id);

                List<String> calculatedRelatedPapers = space.entrySet().stream().filter(entry -> !entry.getKey().equalsIgnoreCase(id)).map(entry -> new Relation("sim", id, entry.getKey(), JensenShannon.similarity(vector, entry.getValue()))).filter(rel -> rel.getScore() > CorpusSplitTrainAndTest.THRESHOLD).sorted((a, b) -> -a.getScore().compareTo(b.getScore())).limit(50).map(rel -> rel.getY()).collect(Collectors.toList());

                evaluations.forEach( evaluation -> evaluation.addResult(relatedPapers, calculatedRelatedPapers.subList(0, evaluation.getN())));

                if (counter.incrementAndGet() % 10 == 0) LOG.info(counter.get() + " papers evaluated");
            });

        }

        evalExecutor.awaitTermination(1, TimeUnit.HOURS);

        evaluations.forEach(evaluation -> LOG.info("Accuracy@" + evaluation.getN() + " -> " + evaluation));

        evaluations.stream().map(evaluation -> evaluation.setAlgorithm(parserAlgorithm.id()).setTestId(testId)).forEach(eval -> {
            try {
                LOG.info(""+eval);
                writer.write(jsonMapper.writeValueAsString(eval) + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return evaluations;

    }


}

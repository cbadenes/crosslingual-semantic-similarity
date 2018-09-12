package io.github.cbadenes.crosslingual.algorithms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cbadenes.crosslingual.data.Evaluation;
import io.github.cbadenes.crosslingual.data.Relation;
import io.github.cbadenes.crosslingual.metrics.JensenShannon;
import io.github.cbadenes.crosslingual.services.LibrairyService;
import io.github.cbadenes.crosslingual.tasks.CorpusPrepare;
import io.github.cbadenes.crosslingual.tasks.CorpusSplitTrainAndTest;
import io.github.cbadenes.crosslingual.utils.ParallelExecutor;
import io.github.cbadenes.crosslingual.utils.ReaderUtils;
import org.junit.Test;
import org.librairy.service.learner.facade.rest.model.Document;
import org.librairy.service.modeler.facade.rest.model.Shape;
import org.librairy.service.modeler.facade.rest.model.ShapeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ParsingBasedAlgorithmsTest {

    private static final Logger LOG = LoggerFactory.getLogger(ParsingBasedAlgorithmsTest.class);

    private static final String LANGUAGE = "en";


    @Test
    public void raw() throws IOException, InterruptedException {
        eval(new RawParser());
    }

    @Test
    public void lemma() throws IOException, InterruptedException {
        eval(new LemmaParser());
    }



    private void eval(Parser parserAlgorithm) throws IOException, InterruptedException {

        ObjectMapper mapper = new ObjectMapper();

        LibrairyService librairyService = new LibrairyService("http://librairy.linkeddata.es/cross-topics","oeg","oeg2018");
        librairyService.reset();

        BufferedReader trainReader = ReaderUtils.from(CorpusSplitTrainAndTest.TRAIN_DATASET);

        String line;

        AtomicInteger counter = new AtomicInteger();
        LOG.info("Training a Topic Model from papers at: '" + CorpusSplitTrainAndTest.TRAIN_DATASET+ "' ..");
        while ((line = trainReader.readLine()) != null){

            JsonNode json = mapper.readTree(line);

            String id = json.get("id").asText();
            String name = json.get("articles").get(LANGUAGE).get("title").asText();
            String text = json.get("articles").get(LANGUAGE).get("description").asText() + " " + json.get("articles").get(LANGUAGE).get("content").asText();



            Document document = new Document();
            document.setId(id);
            document.setName(name);
            document.setText(parserAlgorithm.parse(text));
            librairyService.save(document,false,true);

            if (counter.incrementAndGet() % 100 == 0) LOG.info(counter.get() + " papers added");

        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("topics","100");
        parameters.put("iterations","1000");
        parameters.put("alpha","0.1");
        parameters.put("beta","0.01");
        parameters.put("language",LANGUAGE);
        parameters.put("retries","5");
        parameters.put("topwords","50");
        parameters.put("minfreq","5");
        parameters.put("maxdocratio","0.95");
        parameters.put("raw","true");
        librairyService.train(parameters);

        trainReader.close();
        LOG.info("waiting for complete ..");
        while(!librairyService.isCompleted()){
            Thread.sleep(2000);
        }
        LOG.info("Topic Model created!!");

        //TODO export model

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

        Evaluation eval1    = new Evaluation();
        Evaluation eval5    = new Evaluation();
        Evaluation eval10   = new Evaluation();
        Evaluation eval20   = new Evaluation();

        counter.set(0);

        ParallelExecutor evalExecutor = new ParallelExecutor();

        for (String id : space.keySet()){

            evalExecutor.submit(() -> {
                List<String> relatedPapers = new ArrayList<>(goldStandard.get(id));

                List<Double> vector = space.get(id);

                List<String> calculatedRelatedPapers = space.entrySet().stream().filter(entry -> !entry.getKey().equalsIgnoreCase(id)).map(entry -> new Relation("sim", id, entry.getKey(), JensenShannon.similarity(vector, entry.getValue()))).filter(rel -> rel.getScore() > CorpusSplitTrainAndTest.THRESHOLD).sorted((a, b) -> -a.getScore().compareTo(b.getScore())).limit(50).map(rel -> rel.getY()).collect(Collectors.toList());

                eval1.addResult(relatedPapers, calculatedRelatedPapers.subList(0,1));
                eval5.addResult(relatedPapers, calculatedRelatedPapers.subList(0,5));
                eval10.addResult(relatedPapers, calculatedRelatedPapers.subList(0,10));
                eval20.addResult(relatedPapers, calculatedRelatedPapers.subList(0,20));

                if (counter.incrementAndGet() % 10 == 0) LOG.info(counter.get() + " papers evaluated");
            });

        }

        evalExecutor.awaitTermination(1, TimeUnit.HOURS);

        LOG.info("Accuracy@1 -> " + eval1);
        LOG.info("Accuracy@5 -> " + eval5);
        LOG.info("Accuracy@10 -> " + eval10);
        LOG.info("Accuracy@20 -> " + eval20);


    }


}
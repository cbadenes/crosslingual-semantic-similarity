package io.github.cbadenes.crosslingual.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cbadenes.crosslingual.data.Relation;
import io.github.cbadenes.crosslingual.metrics.WordListSimilarity;
import io.github.cbadenes.crosslingual.utils.ParallelExecutor;
import io.github.cbadenes.crosslingual.utils.ReaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CorpusPrepare {

    private static final Logger LOG = LoggerFactory.getLogger(CorpusPrepare.class);

    public static final String PATH = "corpus/papers-similarity.json.gz";

    public static final Double THRESHOLD = 0.5;

    public static void main(String[] args) throws IOException {


        BufferedReader reader = ReaderUtils.from(CorpusDownload.PATH);

        ObjectMapper mapper = new ObjectMapper();

        String articleJson;

        Map<String,List<String>> papers = new HashMap<>();

        LOG.info("Reading papers from '" + CorpusDownload.PATH + "' ..");

        // Load papers in map
        while ((articleJson = reader.readLine()) != null){

            JsonNode jsonNode = mapper.readTree(articleJson);

            String id = jsonNode.get("id").asText();

            JsonNode keywords = jsonNode.get("articles").get("en").withArray("keywords");

            Iterator<JsonNode> it = keywords.iterator();

            List<String> kws = new ArrayList<>();

            while(it.hasNext()){

                String kw = it.next().asText();

                kws.add(kw.toLowerCase().trim().replaceAll(" ","_"));

            }
            if (!kws.isEmpty()) papers.put(id,kws.stream().distinct().collect(Collectors.toList()));

        }

        reader.close();

        LOG.info(papers.size() + " papers with keywords indexed");

        // Calculate Similarities

        ParallelExecutor executor = new ParallelExecutor();

        List<String> paperIds = papers.keySet().stream().collect(Collectors.toList());

        ConcurrentLinkedQueue<Relation> relations = new ConcurrentLinkedQueue<>();

        AtomicInteger counter = new AtomicInteger();

        for(int i=0; i< paperIds.size(); i++){

            String paperId = paperIds.get(i);
            final Integer from = i+1;

            executor.submit(() -> {
                List<String> keywords = papers.get(paperId);

                for(int j=from; j < paperIds.size(); j++){
                    String y = paperIds.get(j);
                    Relation rel = new Relation("sim", paperId, y, WordListSimilarity.calculate(keywords, papers.get(y)) );
                    if (rel.getScore() > THRESHOLD) relations.add(rel);
                }
                LOG.info("Paper '"+ paperId + "' reviewed. [" + counter.incrementAndGet() +"]");
            });

        }

        executor.awaitTermination(1, TimeUnit.HOURS);

        LOG.info(relations.size() + " similarity relations discovered");



        File outputFile = new File(PATH);
        if (outputFile.exists()) outputFile.delete();
        else outputFile.getParentFile().mkdirs();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFile))));


        LOG.info("Writing relations to '" + PATH + "' ..");

        for(Relation relation : relations){
            writer.write(mapper.writeValueAsString(relation)+"\n");
        }
        writer.close();

        LOG.info("All relations saved.");

    }

}

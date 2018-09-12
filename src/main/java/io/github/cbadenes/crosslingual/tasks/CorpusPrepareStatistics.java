package io.github.cbadenes.crosslingual.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cbadenes.crosslingual.data.Relation;
import io.github.cbadenes.crosslingual.utils.ReaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CorpusPrepareStatistics {

    private static final Logger LOG = LoggerFactory.getLogger(CorpusPrepareStatistics.class);


    public static void main(String[] args) throws IOException {


        BufferedReader reader = ReaderUtils.from(CorpusPrepare.PATH);

        ObjectMapper mapper = new ObjectMapper();

        String articleJson;

        List<Relation> relations = new ArrayList<>();

        LOG.info("Reading papers from '" + CorpusPrepare.PATH + "' ..");

        // Load papers in map
        while ((articleJson = reader.readLine()) != null){
            relations.add(mapper.readValue(articleJson, Relation.class));
        }

        reader.close();


        List<Relation> papers = relations.stream().sorted((a, b) -> -a.getScore().compareTo(b.getScore())).limit(10).collect(Collectors.toList());


        LOG.info("Top10 Similar Papers: ");
        for(Relation rel : papers){
            LOG.info("> " + rel);
        }


        for (Double threshold : Arrays.asList(0.5, 0.6, 0.7, 0.8, 0.9)){
            LOG.info("Num Papers with similarity score higher than "+ threshold + ": " + relations.stream().filter(rel -> rel.getScore() > threshold).flatMap(rel -> Arrays.asList(rel.getX(), rel.getY()).stream()).distinct().count());
        }


    }

}

package io.github.cbadenes.crosslingual.utils;

import io.github.cbadenes.crosslingual.data.Relation;
import io.github.cbadenes.crosslingual.metrics.JensenShannon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SimilarityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SimilarityUtils.class);


    public static Map<String,List<Relation>> compare(Map<String, List<Double>> vectors, Double threshold){

        Map<String,List<Relation>> relations = new HashMap<>();

        for(String ref: vectors.keySet()){

            List<Relation> similarities = vectors.entrySet().parallelStream().filter(entry -> !entry.getKey().equalsIgnoreCase(ref)).map(entry -> new Relation("sim", ref, entry.getKey(), JensenShannon.similarity(vectors.get(ref), entry.getValue()))).filter(rel -> rel.getScore() > threshold).collect(Collectors.toList());
            relations.put(ref,similarities);

        }

        return relations;

    }

}

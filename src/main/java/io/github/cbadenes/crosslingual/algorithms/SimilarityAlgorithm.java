package io.github.cbadenes.crosslingual.algorithms;

import io.github.cbadenes.crosslingual.data.Document;
import io.github.cbadenes.crosslingual.data.Relation;

import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public interface SimilarityAlgorithm {

    String id();

    Map<String,List<Relation>> compare(List<Document> trainingSet, List<Document> testSet, Double threshold);

}

package io.github.cbadenes.crosslingual.algorithms;

import io.github.cbadenes.crosslingual.data.Document;
import io.github.cbadenes.crosslingual.data.Relation;
import io.github.cbadenes.crosslingual.data.TopicModel;
import io.github.cbadenes.crosslingual.tokenizers.RegularTokenizer;
import io.github.cbadenes.crosslingual.tokenizers.Tokenizer;
import io.github.cbadenes.crosslingual.utils.SimilarityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SimpleSimilarityAlgorithm implements SimilarityAlgorithm {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleSimilarityAlgorithm.class);

    @Override
    public String id() {
        return "Simple";
    }

    @Override
    public Map<String, List<Relation>> compare(List<Document> trainingSet, List<Document> testSet, Double threshold) {

        Tokenizer tokenizer = new RegularTokenizer();

        List<Document> dset = trainingSet.parallelStream().map(doc -> new Document(doc.getId(), doc.getLang(), tokenizer.tokens(doc.getText()))).collect(Collectors.toList());
        List<Document> tset = testSet.parallelStream().map(doc -> new Document(doc.getId(), doc.getLang(), tokenizer.tokens(doc.getText()))).collect(Collectors.toList());

        // Create a topic Model
        TopicModel topicModel = new TopicModel(dset, new TopicModel.Parameters().setNumTopics(500).setNumIterations(1000));


        // Inference test documents
        Map<String, List<Double>> vectors = topicModel.inference(tset);


        // Compare all topic distributions
        Map<String, List<Relation>> similarities = SimilarityUtils.compare(vectors, threshold);

        return similarities;
    }
}

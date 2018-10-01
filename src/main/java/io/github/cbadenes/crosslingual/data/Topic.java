package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Topic {

    private static final Logger LOG = LoggerFactory.getLogger(Topic.class);
    private final String id;

    Map<String,Double> words = new ConcurrentHashMap<>();

    public Topic(String id) {
        this.id = id;
    }

    public Topic add(String word, Double score){
        this.words.put(word,score);
        return this;
    }

    public String getId() {
        return id;
    }

    public Map<String, Double> getWords() {
        return words;
    }

    @Override
    public String toString() {
        return "Topic "+ id + " => " + words.entrySet().stream().sorted((a,b) -> -a.getValue().compareTo(b.getValue())).limit(10).map(entry -> entry.getKey()).collect(Collectors.joining(", "));
    }
}

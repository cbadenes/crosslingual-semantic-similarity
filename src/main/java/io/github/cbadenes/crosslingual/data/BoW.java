package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class BoW {

    private static final Logger LOG = LoggerFactory.getLogger(BoW.class);
    private final Map<String, Long> wordCounts;

    public BoW(List<String> tokens) {
        this.wordCounts = tokens.parallelStream().map(token -> new AbstractMap.SimpleEntry<>(token, 1)).collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey, Collectors.counting()));
    }

    public Map<String, Long> getWordCounts() {
        return wordCounts;
    }
}

package io.github.cbadenes.crosslingual.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class WordListSimilarity {

    private static final Logger LOG = LoggerFactory.getLogger(WordListSimilarity.class);


    public static Double calculate(List<String> kw1, List<String> kw2){


        Double simAcc = kw1.stream().map(kw -> calculate(kw, kw2)).reduce((a, b) -> a + b).get();

        return simAcc / Double.valueOf(Math.max(kw1.size(),kw2.size()));
    }

    public static Double calculate(String kw1, List<String> kw2){

        return kw2.stream().map(w -> LevenhsteinSimilarity.calculate(w,kw1)).reduce((a,b) -> a > b? a : b).get();

    }
}

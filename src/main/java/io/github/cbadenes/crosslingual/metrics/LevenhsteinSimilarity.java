package io.github.cbadenes.crosslingual.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LevenhsteinSimilarity {

    private static final Logger LOG = LoggerFactory.getLogger(LevenhsteinSimilarity.class);


    public static Double calculate(String x, String y){

        int maxDistance = Math.max(x.length(), y.length());
        int editDistance = LevenhsteinDistance.calculate(x, y);
        double score = 1.0 - (Double.valueOf(editDistance) / Double.valueOf(maxDistance));
        if (score < 0 ){
            LOG.warn("error");
        }
        return score;
    }

}

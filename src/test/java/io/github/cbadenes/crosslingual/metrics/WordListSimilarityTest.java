package io.github.cbadenes.crosslingual.metrics;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class WordListSimilarityTest {

    private static final Logger LOG = LoggerFactory.getLogger(WordListSimilarityTest.class);


    @Test
    public void equal(){

        List<String> x = Arrays.asList("one","two","three");

        Double res = WordListSimilarity.calculate(x, x);
        LOG.info("score="+ res);
        Assert.assertEquals(new Double(1.0),res);

    }


    @Test
    public void maximum(){

        List<String> x = Arrays.asList("one","two","three","four");
        List<String> y = Arrays.asList("one","two","three","foureign");

        Double res = WordListSimilarity.calculate(x, y);
        LOG.info("score="+ res);
        Assert.assertTrue(res > 0.8);

    }


    @Test
    public void minimum(){

        List<String> x = Arrays.asList("one","two","three","four");
        List<String> y = Arrays.asList("six","seven","eight");

        Double res = WordListSimilarity.calculate(x, y);
        LOG.info("score="+ res);
        Assert.assertTrue( res < 0.1);
    }


    @Test
    public void unbalanced(){

        List<String> y = Arrays.asList("one","two","three","four");
        List<String> x = Arrays.asList("one","two");

        Double res = WordListSimilarity.calculate(x, y);
        LOG.info("score="+ res);
        Assert.assertEquals(new Double(0.5), res);
    }

}

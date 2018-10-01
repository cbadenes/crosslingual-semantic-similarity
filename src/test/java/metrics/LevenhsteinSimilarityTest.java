package metrics;

import io.github.cbadenes.crosslingual.metrics.LevenhsteinSimilarity;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LevenhsteinSimilarityTest {

    private static final Logger LOG = LoggerFactory.getLogger(LevenhsteinSimilarityTest.class);


    @Test
    public void equal(){

        String x = "house";

        Double res = LevenhsteinSimilarity.calculate(x, x);

        Assert.assertEquals(new Double(1.0),res);

    }


    @Test
    public void maximum(){

        String x = "test";
        String y = "tent";

        Double res = LevenhsteinSimilarity.calculate(x, y);

        Assert.assertEquals(new Double(0.75),res);

    }


    @Test
    public void minimum(){

        String x = "land";
        String y = "house";

        Double res = LevenhsteinSimilarity.calculate(x, y);

        Assert.assertEquals(new Double(0.0),res);
    }


}

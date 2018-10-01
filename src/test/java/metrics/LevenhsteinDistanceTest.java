package metrics;

import io.github.cbadenes.crosslingual.metrics.LevenhsteinDistance;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LevenhsteinDistanceTest {

    private static final Logger LOG = LoggerFactory.getLogger(LevenhsteinDistanceTest.class);


    @Test
    public void equal(){

        String x = "house";

        int res = LevenhsteinDistance.calculate(x, x);

        Assert.assertEquals(0,res);

    }


    @Test
    public void minimum(){

        String x = "test";
        String y = "tent";

        int res = LevenhsteinDistance.calculate(x, y);

        Assert.assertEquals(1,res);

    }


    @Test
    public void maximum(){

        String x = "test";
        String y = "house";

        int res = LevenhsteinDistance.calculate(x, y);

        Assert.assertEquals(Math.min(x.length(),y.length()),res);

    }


}

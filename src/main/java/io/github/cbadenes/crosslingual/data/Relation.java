package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Relation {

    private static final Logger LOG = LoggerFactory.getLogger(Relation.class);

    String type;

    String x;

    String y;

    Double score;

    public Relation() {
    }

    public Relation(String type, String x, String y, Double score) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public Double getScore() {
        return score;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setX(String x) {
        this.x = x;
    }

    public void setY(String y) {
        this.y = y;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "type='" + type + '\'' +
                ", x='" + x + '\'' +
                ", y='" + y + '\'' +
                ", score=" + score +
                '}';
    }
}

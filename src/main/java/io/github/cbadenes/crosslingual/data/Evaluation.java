package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Evaluation {

    private static final Logger LOG = LoggerFactory.getLogger(Evaluation.class);

    private long truePositive = 0;

    private long falsePositive = 0;

    private long falseNegative = 0;


    public synchronized void addResult(List<String> reference, List<String> value) {

        truePositive += value.stream().filter(e -> reference.contains(e)).count();

        falsePositive += value.stream().filter(e -> !reference.contains(e)).count();

        falseNegative += reference.stream().filter(e -> !value.contains(e)).count();

    }

    public Double getPrecision() {
        return Double.valueOf(truePositive) / (Double.valueOf(truePositive) + Double.valueOf(falsePositive));
    }


    public Double getRecall() {
        return Double.valueOf(truePositive) / (Double.valueOf(truePositive) + Double.valueOf(falseNegative));
    }

    public Double getFMeasure() {
        Double precision = getPrecision();
        Double recall = getRecall();
        return 2 * (precision * recall) / (precision + recall);
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "truePositive=" + truePositive +
                ", falsePositive=" + falsePositive +
                ", falseNegative=" + falseNegative +
                ", precision=" + getPrecision() +
                ", recall=" + getRecall() +
                ", fMeasure=" + getFMeasure() +
                '}';
    }

}
package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AccuracyReport {

    private static final Logger LOG = LoggerFactory.getLogger(AccuracyReport.class);

    private Integer n;

    private long truePositive = 0;

    private long falsePositive = 0;

    private long falseNegative = 0;

    private String algorithm;

    private String testId;

    private Map<String,String> parameters = new HashMap<>();

    public AccuracyReport(Integer n) {
        this.n = n;
    }

    public String getTestId() {
        return testId;
    }

    public AccuracyReport setTestId(String testId) {
        this.testId = testId;
        return this;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public AccuracyReport setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public AccuracyReport(Integer n, Map<String, String> parameters) {
        this.n = n;
        this.parameters = parameters;
    }

    public AccuracyReport(Integer n, Map<String, String> parameters, String algorithm) {
        this.n = n;
        this.parameters = parameters;
        this.algorithm = algorithm;
    }

    public AccuracyReport() {
    }

    public Integer getN() {
        return n;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

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
        if ((precision == 0.0) && (recall == 0.0)) return 0.0;
        return 2 * (precision * recall) / (precision + recall);
    }

    public long getTruePositive() {
        return truePositive;
    }

    public long getFalsePositive() {
        return falsePositive;
    }

    public long getFalseNegative() {
        return falseNegative;
    }

    @Override
    public String toString() {
        return "Evaluation@"+n+"{" +
                "algorithm="+algorithm+
                ", truePositive=" + truePositive +
                ", falsePositive=" + falsePositive +
                ", falseNegative=" + falseNegative +
                ", precision=" + getPrecision() +
                ", recall=" + getRecall() +
                ", fMeasure=" + getFMeasure() +
                '}';
    }

}
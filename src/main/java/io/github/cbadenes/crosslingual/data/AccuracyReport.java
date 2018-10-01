package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AccuracyReport {

    private static final Logger LOG = LoggerFactory.getLogger(AccuracyReport.class);

    private Integer n = 1;

    private long truePositive = 0;

    private long falsePositive = 0;

    private long falseNegative = 0;

    private Map<String,String> parameters = new HashMap<>();

    public AccuracyReport(Integer n) {
        this.n = n;
    }


    public AccuracyReport(Integer n, Map<String, String> parameters) {
        this.n = n;
        this.parameters = parameters;
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

        List<String> v1 = reference.size() > n ? reference.subList(0, n) : reference;
        List<String> v2 = value.size() > n ? value.subList(0, n) : value;


        LOG.debug("adding result from reference: " + v1 + " and value: " + v2);

        truePositive += v2.stream().filter(e -> v1.contains(e)).count();

        falsePositive += v2.stream().filter(e -> !v1.contains(e)).count();

        falseNegative += v1.stream().filter(e -> !v2.contains(e)).count();

    }

    public Double getPrecision() {

        Double total = Double.valueOf(truePositive) + Double.valueOf(falsePositive);

        if (total == 0.0) return 0.0;

        return Double.valueOf(truePositive) / total;
    }


    public Double getRecall() {

        Double total = (Double.valueOf(truePositive) + Double.valueOf(falseNegative));

        if (total == 0.0) return 0.0;

        return Double.valueOf(truePositive) / total;
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
                "tp=" + truePositive +
                ", fp=" + falsePositive +
                ", fn=" + falseNegative +
                ", precision@"+n+"=" + getPrecision() +
                ", recall@"+n+"=" + getRecall() +
                ", fMeasure@"+n+"=" + getFMeasure() +
                ", parameters=" + parameters.entrySet().stream().map(entry -> "'"+entry.getKey()+"':"+entry.getValue()).collect(Collectors.joining("|")) +
                '}';
    }

}
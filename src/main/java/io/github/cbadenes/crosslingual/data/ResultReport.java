package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ResultReport {

    private static final Logger LOG = LoggerFactory.getLogger(ResultReport.class);
    private final Map<String,Integer> goldstandard;
    private final Map<String,Integer> returned;

    public ResultReport(List<String> referenced, List<String> returned) {
        this.goldstandard = new HashMap<>();
        referenced.forEach(r -> this.goldstandard.put(r,1));
        this.returned   = new HashMap<>();
        returned.forEach(r -> this.returned.put(r,1));
    }

    public ResultReport(Set<String> referenced, Set<String> returned) {
        this.goldstandard = new HashMap<>();
        referenced.forEach(r -> this.goldstandard.put(r,1));
        this.returned   = new HashMap<>();
        returned.forEach(r -> this.returned.put(r,1));
    }

    public Double getPrecision(){
        if ((returned.isEmpty()) || (goldstandard.isEmpty())) return 1.0;
        long tp = this.returned.entrySet().stream().filter(e -> this.goldstandard.containsKey(e.getKey())).count();
        long fp = this.returned.entrySet().stream().filter(e -> !this.goldstandard.containsKey(e.getKey())).count();
        return Double.valueOf(tp) / (Double.valueOf(tp) + Double.valueOf(fp));
    }


    public Double getRecall(){
        if ((returned.isEmpty()) && (!goldstandard.isEmpty())) return 0.0;
        if ((returned.isEmpty()) && (goldstandard.isEmpty())) return 1.0;
        long tp = this.returned.entrySet().stream().filter(e -> this.goldstandard.containsKey(e.getKey())).count();
        long fn = this.goldstandard.entrySet().stream().filter(e -> !this.returned.containsKey(e.getKey())).count();
        return Double.valueOf(tp) / (Double.valueOf(tp) + Double.valueOf(fn));
    }

    public Double getFMeasure(){
        Double precision    = getPrecision();
        Double recall       = getRecall();

        if (precision+recall == 0.0) return 0.0;

        return (2 *  (precision*recall)) / (precision+recall);
    }

    public int getSize(){
        return returned.size() - goldstandard.size();
    }

}

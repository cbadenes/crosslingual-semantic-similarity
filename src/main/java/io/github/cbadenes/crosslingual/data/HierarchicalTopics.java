package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class HierarchicalTopics {

    private static final Logger LOG = LoggerFactory.getLogger(HierarchicalTopics.class);


    Map<Integer,List<String>> levels;


    public HierarchicalTopics() {
        levels = new HashMap<>();
        levels.put(0, new ArrayList<>());
        levels.put(1, new ArrayList<>());
        levels.put(2, new ArrayList<>());
    }

    public HierarchicalTopics addToLevel(int level, String id){
        this.levels.get(level).add(id);
        return this;
    }


    public boolean isValid(){
        return (!levels.get(0).isEmpty() && !levels.get(1).isEmpty() && !levels.get(2).isEmpty());
    }


    public List<String> getLevel(int level) {
        return levels.get(level);
    }

    @Override
    public String toString() {
        return "HierarchicalTopics{" + levels+ '}';
    }
}

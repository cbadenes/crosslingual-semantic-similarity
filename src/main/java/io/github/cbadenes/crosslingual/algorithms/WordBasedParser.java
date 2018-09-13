package io.github.cbadenes.crosslingual.algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class WordBasedParser implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(WordBasedParser.class);


    @Override
    public String id() {
        return "WordBased";
    }

    @Override
    public String parse(String text) {
        return text.replaceAll(";"," "); // special character for librAIry
    }
}

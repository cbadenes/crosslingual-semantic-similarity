package io.github.cbadenes.crosslingual.algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LemmaParser implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(LemmaParser.class);


    @Override
    public String parse(String text) {
        return text;
    }
}

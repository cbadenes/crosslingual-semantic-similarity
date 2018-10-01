package io.github.cbadenes.crosslingual.tokenizers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class NLPTokenizer implements Tokenizer {

    private static final Logger LOG = LoggerFactory.getLogger(NLPTokenizer.class);

    @Override
    public List<String> tokens(String text) {
        return Collections.emptyList();
    }

    @Override
    public String parse(String text) {
        return tokens(text).stream().collect(Collectors.joining(" "));
    }
}

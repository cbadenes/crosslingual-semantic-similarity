package io.github.cbadenes.crosslingual.tokenizers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class RegularTokenizer implements Tokenizer {

    private static final Logger LOG = LoggerFactory.getLogger(RegularTokenizer.class);

    @Override
    public List<String> tokens(String text) {
        return Arrays.stream(text.split(" ")).parallel().filter(token -> token.length() > 3).collect(Collectors.toList());
    }

    @Override
    public String parse(String text) {
        return tokens(text).stream().collect(Collectors.joining(" "));
    }
}

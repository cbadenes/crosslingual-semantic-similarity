package io.github.cbadenes.crosslingual.tokenizers;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public interface Tokenizer {

    List<String> tokens(String text);

    String parse(String text);
}

package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Annotation {

    private static final Logger LOG = LoggerFactory.getLogger(Annotation.class);

    private Token token;

    private Integer offset;

    private List<String> synset;

    public Annotation() {
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public List<String> getSynset() {
        return synset;
    }

    public void setSynset(List<String> synset) {
        this.synset = synset;
    }
}

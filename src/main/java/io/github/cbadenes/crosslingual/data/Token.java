package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Token {

    private static final Logger LOG = LoggerFactory.getLogger(Token.class);

    private String target;

    private String lemma;

    private String pos;

    public Token() {
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }
}

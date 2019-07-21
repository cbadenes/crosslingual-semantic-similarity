package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Text {

    private static final Logger LOG = LoggerFactory.getLogger(Text.class);

    private String text;

    public Text() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

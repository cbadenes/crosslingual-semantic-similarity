package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Document {

    private static final Logger LOG = LoggerFactory.getLogger(Document.class);

    private String id;

    private String lang;

    private String text;

    public Document(String id, String lang, String text) {
        this.id = id;
        this.lang = lang;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getText() {
        return text;
    }
}

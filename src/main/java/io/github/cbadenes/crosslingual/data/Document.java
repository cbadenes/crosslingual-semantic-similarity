package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

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

    public Document(String id, String lang, List<String> tokens) {
        this.id = id;
        this.lang = lang;
        this.text = tokens.stream().collect(Collectors.joining(" "));
    }

    public Document() {
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

    public void setId(String id) {
        this.id = id;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setText(String text) {
        this.text = text;
    }
}

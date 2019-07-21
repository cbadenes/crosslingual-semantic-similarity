package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AnnotationRequest {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationRequest.class);

    private List<String> filter;

    private String lang;

    private boolean multigrams;

    private boolean references;

    private boolean synset;

    private String text;

    public AnnotationRequest() {
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean getMultigrams() {
        return multigrams;
    }

    public void setMultigrams(Boolean multigrams) {
        this.multigrams = multigrams;
    }

    public boolean getReferences() {
        return references;
    }

    public void setReferences(Boolean references) {
        this.references = references;
    }

    public boolean isSynset() {
        return synset;
    }

    public void setSynset(boolean synset) {
        this.synset = synset;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

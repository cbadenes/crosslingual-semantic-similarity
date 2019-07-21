package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AnnotatedDocument {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotatedDocument.class);

    private String id;

    private String name;

    private String lang;

    private String labels;

    private String topics0;

    private String topics1;

    private String topics2;

    private String synset0;

    private String synset1;

    private String synset2;

    public AnnotatedDocument() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getTopics0() {
        return topics0;
    }

    public void setTopics0(String topics0) {
        this.topics0 = topics0;
    }

    public String getTopics1() {
        return topics1;
    }

    public void setTopics1(String topics1) {
        this.topics1 = topics1;
    }

    public String getTopics2() {
        return topics2;
    }

    public void setTopics2(String topics2) {
        this.topics2 = topics2;
    }

    public String getSynset0() {
        return synset0;
    }

    public void setSynset0(String synset0) {
        this.synset0 = synset0;
    }

    public String getSynset1() {
        return synset1;
    }

    public void setSynset1(String synset1) {
        this.synset1 = synset1;
    }

    public String getSynset2() {
        return synset2;
    }

    public void setSynset2(String synset2) {
        this.synset2 = synset2;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}

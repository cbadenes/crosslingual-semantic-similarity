package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Article {

    private static final Logger LOG = LoggerFactory.getLogger(Article.class);

    private String id;

    private String doi;

    private String title;

    private String language;

    private String description;

    private String text;

    private List<String> citedBy = new ArrayList<>();

    private List<String> keywords = new ArrayList<>();

    private List<String> labels = new ArrayList<>();

    private Journal journal;

    public Article() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getCitedBy() {
        return citedBy;
    }

    public void setCitedBy(List<String> citedBy) {
        this.citedBy = citedBy;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }



    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", doi='" + doi + '\'' +
                ", title='" + title + '\'' +
                ", language='" + language + '\'' +
                ", citedBy=" + citedBy +
                ", keywords=" + keywords +
                '}';
    }
}

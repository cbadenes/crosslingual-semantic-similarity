package io.github.cbadenes.crosslingual.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultiLangArticle {

    private static final Logger LOG = LoggerFactory.getLogger(MultiLangArticle.class);

    private String id;

    private String doi;

    private List<String> citedBy = new ArrayList<>();

    private Journal journal;

    private Map<String,ArticleInfo> articles = new HashMap<>();

    public MultiLangArticle() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public List<String> getCitedBy() {
        return citedBy;
    }

    public void setCitedBy(List<String> citedBy) {
        this.citedBy = citedBy;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public Map<String, ArticleInfo> getArticles() {
        return articles;
    }

    public void setArticles(Map<String, ArticleInfo> articles) {
        this.articles = articles;
    }

    public boolean isEmpty(){
        return articles.isEmpty();
    }


    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", doi='" + doi + '\'' +
                ", languages='" + articles.keySet()+ '\'' +
                ", citedBy=" + citedBy +
                ", journal=" + journal +
                '}';
    }
}

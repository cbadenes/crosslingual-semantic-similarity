package io.github.cbadenes.crosslingual.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Cite {

    private static final Logger LOG = LoggerFactory.getLogger(Cite.class);

    private ArticleMetaInf article;

    private List<ArticleMetaInf> cited_by;

    public Cite() {
    }

    public boolean isEmpty(){
        return article == null;
    }

    public ArticleMetaInf getArticle() {
        return article;
    }

    public void setArticle(ArticleMetaInf article) {
        this.article = article;
    }

    public List<ArticleMetaInf> getCited_by() {
        return cited_by;
    }

    public void setCited_by(List<ArticleMetaInf> cited_by) {
        this.cited_by = cited_by;
    }

    @Override
    public String toString() {
        return "Cite{" +
                "article=" + article +
                ", cited_by=" + cited_by +
                '}';
    }
}



package io.github.cbadenes.crosslingual.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleMetaInf {

    private static final Logger LOG = LoggerFactory.getLogger(ArticleMetaInf.class);

    private String code;

    private String start_page;

    private Author first_author;

    private String issn;

    private String doi;

    private Integer total_received;

    private String source;

    private List<String> titles;

    private String url;

    private Object authors;

    private String end_page;

    private Object translated_titles;

    private String collection;

    private String publication_year;

    public ArticleMetaInf() {
    }

    public String getPublication_year() {
        return publication_year;
    }

    public void setPublication_year(String publication_year) {
        this.publication_year = publication_year;
    }

    public Object getTranslated_titles() {
        return translated_titles;
    }

    public void setTranslated_titles(Object translated_titles) {
        this.translated_titles = translated_titles;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStart_page() {
        return start_page;
    }

    public void setStart_page(String start_page) {
        this.start_page = start_page;
    }

    public Author getFirst_author() {
        return first_author;
    }

    public void setFirst_author(Author first_author) {
        this.first_author = first_author;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getAuthors() {
        return authors;
    }

    public List<String> getAuthorList() {
        if ((authors != null) && !(authors instanceof String)){
            return (List<String>) authors;
        }else{
            return Collections.emptyList();
        }
    }

    public List<String> getTranslatedTitlesList() {
        if ((translated_titles != null) && !(translated_titles instanceof String)){
            return (List<String>) translated_titles;
        }else{
            return Collections.emptyList();
        }
    }


    public String getEnd_page() {
        return end_page;
    }

    public void setEnd_page(String end_page) {
        this.end_page = end_page;
    }

    public void setAuthors(Object authors){
        this.authors = authors;
    }

    @Override
    public String toString() {
        return "ArticleMetaInf{" +
                "code='" + code + '\'' +
                ", start_page='" + start_page + '\'' +
                ", first_author=" + first_author +
                ", issn='" + issn + '\'' +
                ", source='" + source + '\'' +
                ", titles=" + titles +
                ", url='" + url + '\'' +
                ", end_page='" + end_page + '\'' +
                '}';
    }
}

package io.github.cbadenes.crosslingual.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {

    private static final Logger LOG = LoggerFactory.getLogger(Author.class);

    private String surname;

    private String role;

    private String given_names;

    private List<String> xref;

    public Author() {
    }

    public Author(String name){
        given_names = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGiven_names() {
        return given_names;
    }

    public void setGiven_names(String given_names) {
        this.given_names = given_names;
    }

    public List<String> getXref() {
        return xref;
    }

    public void setXref(List<String> xref) {
        this.xref = xref;
    }

    @Override
    public String toString() {
        return "Author{" +
                "surname='" + surname + '\'' +
                ", role='" + role + '\'' +
                ", given_names='" + given_names + '\'' +
                ", xref=" + xref +
                '}';
    }
}

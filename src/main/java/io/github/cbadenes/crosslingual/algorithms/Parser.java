package io.github.cbadenes.crosslingual.algorithms;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public interface Parser {

    String id();

    String parse(String text);

}

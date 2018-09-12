package io.github.cbadenes.crosslingual.utils;

import io.github.cbadenes.crosslingual.data.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ArticleUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ArticleUtils.class);


    public static boolean isValid(Article article){

        String content = article.getDescription()+ " " + article.getText();

        return content.length() > 5000;
    }

    public static boolean isValidAsTest(Article article){

        return !article.getCitedBy().isEmpty() && article.getCitedBy().size() >= 2;
    }

}

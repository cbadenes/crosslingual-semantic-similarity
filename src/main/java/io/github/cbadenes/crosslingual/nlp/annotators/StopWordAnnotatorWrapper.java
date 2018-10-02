package io.github.cbadenes.crosslingual.nlp.annotators;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.Pair;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import edu.stanford.nlp.pipeline.Requirement;

import java.util.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class StopWordAnnotatorWrapper implements Annotator, CoreAnnotation<Pair<Boolean, Boolean>> {
    public static final String ANNOTATOR_CLASS = "stopword";
    public static final String STANFORD_STOPWORD = "stopword";
    public static final Requirement STOPWORD_REQUIREMENT = new Requirement("stopword");
    public static final String STOPWORDS_LIST = "stopword-list";
    public static final String IGNORE_STOPWORD_CASE = "ignore-stopword-case";
    public static final String CHECK_LEMMA = "check-lemma";
    private static Class<Pair<Boolean, Boolean>> boolPair = (Class<Pair<Boolean, Boolean>>) Pair.makePair(Boolean.valueOf(true), Boolean.valueOf(true)).getClass();
    private Properties props;
    private CharArraySet stopwords;
    private boolean checkLemma;

    public StopWordAnnotatorWrapper(String annotatorClass, Properties props) {
        this.props = props;
        this.checkLemma = Boolean.parseBoolean(props.getProperty("check-lemma", "false"));
        if(this.props.containsKey("stopword-list")) {
            String stopwordList = props.getProperty("stopword-list");
            boolean ignoreCase = Boolean.parseBoolean(props.getProperty("ignore-stopword-case", "false"));
            this.stopwords = getStopWordList(Version.LUCENE_36, stopwordList, ignoreCase);
        } else {
            this.stopwords = (CharArraySet) StopAnalyzer.ENGLISH_STOP_WORDS_SET;
        }

    }

    public void annotate(Annotation annotation) {
        if(this.stopwords != null && this.stopwords.size() > 0 && annotation.containsKey(CoreAnnotations.TokensAnnotation.class)) {
            List tokens = (List)annotation.get(CoreAnnotations.TokensAnnotation.class);
            Iterator var3 = tokens.iterator();

            while(var3.hasNext()) {
                CoreLabel token = (CoreLabel)var3.next();
                boolean isWordStopword = this.stopwords.contains(token.word().toLowerCase());
                boolean isLemmaStopword = this.checkLemma?this.stopwords.contains(token.word().toLowerCase()):false;
                Pair pair = Pair.makePair(Boolean.valueOf(isWordStopword), Boolean.valueOf(isLemmaStopword));
                token.set(StopWordAnnotatorWrapper.class, pair);
            }
        }

    }

    public Set<Requirement> requirementsSatisfied() {
        return Collections.singleton(STOPWORD_REQUIREMENT);
    }

    public Set<Requirement> requires() {
        return this.checkLemma?TOKENIZE_SSPLIT_POS_LEMMA:TOKENIZE_AND_SSPLIT;
    }

    public Class<Pair<Boolean, Boolean>> getType() {
        return boolPair;
    }

    public static CharArraySet getStopWordList(Version luceneVersion, String stopwordList, boolean ignoreCase) {
        String[] terms = stopwordList.split(",");
        CharArraySet stopwordSet = new CharArraySet(luceneVersion, terms.length, ignoreCase);
        String[] var5 = terms;
        int var6 = terms.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String term = var5[var7];
            stopwordSet.add(term);
        }

        return CharArraySet.unmodifiableSet(stopwordSet);
    }
}
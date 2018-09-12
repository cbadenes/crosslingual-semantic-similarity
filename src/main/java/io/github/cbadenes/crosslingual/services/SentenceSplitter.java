package io.github.cbadenes.crosslingual.services;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SentenceSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(SentenceSplitter.class);


    public static List<String> parse(String text){
        List<String> sentences = new ArrayList<>();

        Reader reader = new StringReader(text);
        DocumentPreprocessor dp = new DocumentPreprocessor(reader);

        for (List<HasWord> sentence : dp) {
            // SentenceUtils not Sentence
            String sentenceString = SentenceUtils.listToString(sentence);
            String normalizedSentence = sentenceString.trim().replaceAll("( )+", " ").replaceAll("-LRB-", "[").replaceAll("-RRB-", "]");
            String removedParentheses = removeParenthesis(normalizedSentence,"[]");
            sentences.add(removedParentheses);
        }

        return sentences;
    }

    public static String removeParenthesis(String input_string, String parenthesis_symbol){
        // removing parenthesis and everything inside them, works for (),[] and {}
        if(parenthesis_symbol.contains("[]")){
            return input_string.replaceAll("\\s*\\[[^\\]]*\\]\\s*", " ");
        }else if(parenthesis_symbol.contains("{}")){
            return input_string.replaceAll("\\s*\\{[^\\}]*\\}\\s*", " ");
        }else{
            return input_string.replaceAll("\\s*\\([^\\)]*\\)\\s*", " ");
        }
    }


    public static void main(String[] args) {


        String text = "Palabras clave: espaol de Bolivia, espaol andino, indexicalidad, registro, contacto de lenguas, contexto.  " +
                "\t \t     \t    INTRODUCTION\t \t    Language users interpretation of linguistic features at the microinteractional level is built through contrast to and congruence with largescale semiotic systems of interpretation. " +
                "The interpretation of particular features always takes place against the backdrop of an understanding of typical patterns of interaction, not only at the community level, but also for social groups and even individuals. " +
                "In this article, I describe the construction of social meaning in a Quechuainfluenced Bolivian dialect of Spanish. " +
                "In this context, the use of Quechua contact features gains meaning in concrete interactions by particular individuals as representatives of particular social groups." +
                " I demonstrate that the same contact features used by individuals identified with different groups results in differing social interpretations and characterizations of the speaker. " +
                "This work contributes to the existing literature by complementing formal studies of language contact in the Andean region (e.g. Escobar 2000; Snchez 2004) and by extending the literature on Bolivia, which has been relatively understudied as a contact zone. " +
                "Following Godenzzi (2005, 2011), this work brings a microcontextual perspective to existing work on sociolinguistics and language ideologies in the Andes (Alb 1970; Howard 2007).  " +
                "\t    Sociolinguists and linguistic anthropologists have long been involved in deciphering just how social and semiotic patterns are generated, how they shift, and how they change. " +
                "Over the past forty years, critiques of variationist sociolinguistics have led to shifts in the focus of sociolinguistic research. " +
                "Early variationist work linked linguistic variables to broad demographic categories (e.g. Labov 1972a; Labov 1972b); later work examined variables realization in social networks (Milroy  Milroy 1997). " +
                "Still other studies have shown the ways in which linguistic variables participate in identity and social identification (Bucholtz  Hall 2004; Eckert 2000; Kuipers 1998; Rampton 1995), and, more recently, the way that social structures, such as \"clan\", are constructed through local practices (Stanford 2009). " +
                "Linguistic behavior, it seems, is as fluid and contextdependent as other forms of social action.";

        List<String> sentences = SentenceSplitter.parse(text);

        sentences.forEach(sentence -> LOG.info(sentence));

        LOG.info("Sentences: " + sentences.size());

    }


}

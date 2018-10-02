package io.github.cbadenes.crosslingual.nlp.services;

import com.google.common.base.Strings;
import edu.stanford.nlp.pipeline.Annotation;
import org.apache.avro.AvroRemoteException;
import org.librairy.service.nlp.facade.model.Form;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.cbadenes.crosslingual.nlp.annotators.StanfordPipeAnnotatorEN;
import io.github.cbadenes.crosslingual.nlp.annotators.StanfordAnnotatorWrapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class ENCoreNLPService {

    private static final Logger LOG = LoggerFactory.getLogger(ENCoreNLPService.class);


    StanfordPipeAnnotatorEN annotator;


    StanfordAnnotatorWrapper tokenizer;

    public ENCoreNLPService() {
        this.annotator = new StanfordPipeAnnotatorEN();
        this.tokenizer = new StanfordAnnotatorWrapper();
    }


    public String tokens(String text, List<PoS> filter, Form form) throws AvroRemoteException {
        if (Strings.isNullOrEmpty(text)) return "";

        return annotations(text, filter).stream()
                .map(annotation -> (form.equals(Form.LEMMA) ? annotation.getToken().getLemma() : annotation.getToken().getTarget()))
                .collect(Collectors.joining(" "));
    }

    public List<org.librairy.service.nlp.facade.model.Annotation> annotations(String text, List<PoS> filter) throws AvroRemoteException {
        if (Strings.isNullOrEmpty(text)) return Collections.emptyList();

        List<org.librairy.service.nlp.facade.model.Annotation> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile(".{1,1000}(,|.$)").matcher(text);
        while (matcher.find()){

            String partialContent = matcher.group();
            Instant startAnnotation = Instant.now();
            Annotation annotation = annotator.annotate(partialContent);
            Instant endAnnotation = Instant.now();
            LOG.debug("Annotated  in: " +
                    ChronoUnit.MINUTES.between(startAnnotation,endAnnotation) + "min " +
                    (ChronoUnit.SECONDS.between(startAnnotation,endAnnotation)%60) + "secs");

            try{
                Instant startTokenizer = Instant.now();
                List<org.librairy.service.nlp.facade.model.Annotation> annotations = tokenizer.tokenize(annotation);
                Instant endTokenizer = Instant.now();
                LOG.debug("Parsed  into " + annotations.size() + " annotations  in: " +
                        ChronoUnit.MINUTES.between(startTokenizer,endTokenizer) + "min " + (ChronoUnit.SECONDS.between(startTokenizer,endTokenizer)%60) + "secs");
                tokens.addAll(annotations);
            }catch (Exception e){
                LOG.error("Error tokenizing", e);
            }

        }
        if (filter.isEmpty()) return tokens;
        return tokens.stream()
                .filter(annotation -> filter.contains(annotation.getToken().getPos())).collect(Collectors.toList());
    }
}

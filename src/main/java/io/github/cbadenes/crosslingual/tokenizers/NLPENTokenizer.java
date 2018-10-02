package io.github.cbadenes.crosslingual.tokenizers;

import io.github.cbadenes.crosslingual.nlp.services.ENCoreNLPService;
import org.apache.avro.AvroRemoteException;
import org.librairy.service.nlp.facade.model.Form;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class NLPENTokenizer implements Tokenizer {

    private static final Logger LOG = LoggerFactory.getLogger(NLPENTokenizer.class);
    private final ENCoreNLPService nlpService;
    private final List<PoS> posList;


    public NLPENTokenizer(List<PoS> posList) {
        this.nlpService = new ENCoreNLPService();
        this.posList = posList;
    }

    @Override
    public List<String> tokens(String text) {

        try {
            return nlpService.annotations(text,posList).stream().map(ann -> ann.getToken().getLemma()).collect(Collectors.toList());
        } catch (AvroRemoteException e) {
            LOG.warn("Unexpected error",e);
            return Collections.emptyList();
        }
    }

    @Override
    public String parse(String text) {
        try {
            return nlpService.tokens(text, posList, Form.LEMMA);
        } catch (AvroRemoteException e) {
            LOG.warn("Unexpected error",e);
            return "";
        }
    }
}

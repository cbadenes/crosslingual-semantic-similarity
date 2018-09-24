package io.github.cbadenes.crosslingual.algorithms;

import com.google.common.base.Optional;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.BuiltInLanguages;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.librairy.service.nlp.facade.model.Form;
import org.librairy.service.nlp.facade.model.PoS;
import org.librairy.service.nlp.facade.rest.model.TokensRequest;
import org.librairy.service.nlp.facade.rest.model.TokensResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LemmaBasedParser implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(LemmaBasedParser.class);
    private final LanguageDetector languageDetector;
    private final TextObjectFactory textObjectFactory;
    private final List<PoS> posList;

    public LemmaBasedParser(List<PoS> posList) throws IOException {
        this.posList = posList;
        Unirest.setTimeouts(10000, 90000);
        LanguageProfileReader langReader = new LanguageProfileReader();

        List<LanguageProfile> languageProfiles = new ArrayList<>();

        Iterator it = BuiltInLanguages.getLanguages().iterator();

        List<String> availableLangs = Arrays.asList(new String[]{"en","es","pt"});
        while(it.hasNext()) {
            LdLocale locale = (LdLocale)it.next();
            if (availableLangs.contains(locale.getLanguage())) {
                LOG.debug("language added: " + locale);
                languageProfiles.add(langReader.readBuiltIn(locale));
            }
        }

        //build language detector:
        this.languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();

        //create a text object factory
        this.textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
    }

    @Override
    public String id() {
        return "lemmaBased-"+posList.stream().map(w -> w.name().toLowerCase()).collect(Collectors.joining("_"));
    }

    @Override
    public String parse(String text) {
        try {
            String lang = getLanguageFrom(text);

            TokensRequest request = new TokensRequest();
            request.setText(text);
            request.setFilter(posList);
            request.setMultigrams(false);
            request.setForm(Form.LEMMA);

            HttpResponse<TokensResult> response = Unirest.post("http://librairy.linkeddata.es/"+lang+"/tokens").body(request).asObject(TokensResult.class);

            if (response.getStatus() != 200 && response.getStatus() != 201){
                throw new RuntimeException();
            }

            return response.getBody().getTokens();

        } catch (Exception e) {
            LOG.error("Unexpected error",e);
            return text;
        }

    }

    @Override
    public String language() {
        return "en";
    }

    private String getLanguageFrom(String text){
        TextObject textObject = textObjectFactory.forText(text);
        Optional<LdLocale> lang = languageDetector.detect(textObject);
        if (!lang.isPresent()) return "en";
        return lang.get().getLanguage();
    }
}

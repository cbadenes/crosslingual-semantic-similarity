package io.github.cbadenes.crosslingual.services;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.cbadenes.crosslingual.data.Article;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TranslationService {

    private static final Logger LOG = LoggerFactory.getLogger(TranslationService.class);
    private final PersistentRepository persistentRepository;

    LoadingCache<TransRequest, String> translations;

    Translate translateService = TranslateOptions.newBuilder().build().getService();

    Long characterCounter = 0l;


    public TranslationService() {
        persistentRepository = new PersistentRepository("src/main/resources/translations.db");
        translations = CacheBuilder.newBuilder()
                .maximumSize(500)
                .build(
                        new CacheLoader<TransRequest, String>() {
                            public String load(TransRequest key) {

                                Optional<String> translation = persistentRepository.get(key.getId());

                                if (translation.isPresent()) return translation.get();

                                characterCounter += key.getText().length();

                                LOG.debug("Requesting translation to Google API: " + key);

                                LOG.info("Characters translated = " + characterCounter);

//                                Translate.TranslateOption srcLang = Translate.TranslateOption.sourceLanguage(key.getFrom());
//                                Translate.TranslateOption tgtLang = Translate.TranslateOption.targetLanguage(key.getTo());
//
//                                // Use translate `model` parameter with `base` and `nmt` options.
//                                Translate.TranslateOption model = Translate.TranslateOption.model("nmt");
//
//
//                                Translation translation = translateService.translate(key.getText(), srcLang, tgtLang, model);
//
//                                return translation.getTranslatedText();

                                return "";

                            }
                        });
    }

    public Article translate(Article article, String to){

        Article translation = new Article();
        translation.setLanguage(to);
        try {
            BeanUtils.copyProperties(translation,article);
            translation.setTitle(translate(article.getTitle(), article.getLanguage(), to));
            translation.setText(translate(article.getText(), article.getLanguage(), to));
            translation.setDescription(translate(article.getDescription(), article.getLanguage(), to));
            translation.setKeywords(article.getKeywords().stream().map(kw -> translate(kw, article.getLanguage(), to)).collect(Collectors.toList()));

        } catch (Exception e) {
            LOG.error("Unexpected error translating article: " + article + " -> " + e.getMessage());
        }

        return translation;
    }

    private String translate(String text, String from, String to){
        try {
            return this.translations.get(new TransRequest(text, from, to));
        } catch (ExecutionException e) {
            LOG.error("Error translating text: " + text.substring(0,5) + " , from: " + from + " , to: " + to);
            return "";
        }
    }

    private class TransRequest{

        private final String text;
        private final String from;
        private final String to;

        public TransRequest(String text, String from, String to) {
            this.text = text;
            this.from = from;
            this.to = to;
        }

        public String getText() {
            return text;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getId(){
            return text.hashCode()+ "_" + to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TransRequest that = (TransRequest) o;

            if (!text.equals(that.text)) return false;
            if (!from.equals(that.from)) return false;
            return to.equals(that.to);

        }

        @Override
        public int hashCode() {
            int result = text.hashCode();
            result = 31 * result + from.hashCode();
            result = 31 * result + to.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "TransRequest{" +
                    "from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    '}';
        }
    }
}

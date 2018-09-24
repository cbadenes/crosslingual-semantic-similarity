package io.github.cbadenes.crosslingual.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cbadenes.crosslingual.metrics.WordListSimilarity;
import io.github.cbadenes.crosslingual.utils.ReaderUtils;
import io.github.cbadenes.crosslingual.utils.WriterUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Dataset {

    private static final Logger LOG = LoggerFactory.getLogger(Dataset.class);

    private final List<MultiLangArticle> multiLangArticles;
    private final List<Relation> articleSimilarities;

    private static final Double THRESHOLD = 0.5;

    private AtomicInteger articleCounter    = new AtomicInteger();
    private AtomicInteger similarityCounter = new AtomicInteger();

    public Dataset(String path) throws IOException {

        String dataPath = path;
        if (path.startsWith("http")){
            dataPath = download(new URL(path));
        }

        BufferedReader reader = ReaderUtils.from(dataPath);

        ObjectMapper jsonMapper = new ObjectMapper();

        String line;

        this.multiLangArticles = new ArrayList<>();
        Map<String, List<String>> simCandidates = new ConcurrentHashMap<>();

        LOG.info("Reading multi-language articles ..");
        while((line = reader.readLine()) != null){

            if (articleCounter.incrementAndGet() % 100 == 0) LOG.info(articleCounter.get() + " articles parsed");

            MultiLangArticle multiLangArticle = jsonMapper.readValue(line, MultiLangArticle.class);
            multiLangArticles.add(multiLangArticle);

            if (!multiLangArticle.getArticles().get("en").getKeywords().isEmpty()){
                simCandidates.put(multiLangArticle.getId(), multiLangArticle.getArticles().get("en").getKeywords());
            }
        }
        reader.close();
        LOG.info(articleCounter.get() + " articles parsed");

        LOG.info("Calculating similarities between " + simCandidates.size() + " articles ...");
        List<String> simArticles = new ArrayList<>(simCandidates.keySet());

        this.articleSimilarities = new ArrayList<Relation>();
        for(int i=0; i< simArticles.size()-1; i++){

            String referenceId = simArticles.get(i);
            List<String> referenceKWs = simCandidates.get(referenceId);

            List<Relation> similarities = simArticles.parallelStream().skip(i + 1).map(art -> new Relation("sim", referenceId, art, WordListSimilarity.calculate(referenceKWs, simCandidates.get(art)))).filter(rel -> rel.getScore() > THRESHOLD).collect(Collectors.toList());
            similarityCounter.set(similarityCounter.get()+similarities.size());
            articleSimilarities.addAll(similarities);

        }

        LOG.info("Dataset ready!");

    }

    private String download(URL url) throws IOException {
        File outputFile = Paths.get("corpora","papers.json.gz").toFile();

        if (outputFile.exists()) outputFile.delete();
        else outputFile.getParentFile().mkdirs();

        LOG.info("Downloading articles from: " + url);

        outputFile.getParentFile().mkdirs();

        FileUtils.copyURLToFile(url,outputFile);

        LOG.info("Corpus file created at: " + outputFile.getAbsolutePath());

        return outputFile.getAbsolutePath();

    }

    public void export(String id, List<String> langs, Double trainingTestRatio) throws IOException {
        double trainingSize = (trainingTestRatio * multiLangArticles.size()) / 100.0;
        double testSize     = multiLangArticles.size() - trainingSize;

        Double partialThreshold = 0.0;
        Map<String,Integer> testArticles = new ConcurrentHashMap<>();

        for(Double minScore = THRESHOLD; minScore < 1.0 ; minScore +=0.05){
            Map candTestArticles = prepareTest(minScore);
            if (testArticles.isEmpty()){
                testArticles = candTestArticles;
                partialThreshold = minScore;
            }
            if (candTestArticles.size() < testSize) break;
            testArticles = candTestArticles;
            partialThreshold = minScore;
        }
        LOG.info("Threshold set to: " + partialThreshold);

        // Save Training, Test and Similarity Set
        File baseDir = Paths.get("corpora", id).toFile();
        baseDir.mkdirs();

        BufferedWriter trainingSetWriter    = WriterUtils.to(Paths.get("corpora", id, "training-set.jsonl.gz").toString());
        BufferedWriter testSetWriter        = WriterUtils.to(Paths.get("corpora", id, "test-set.jsonl.gz").toString());
        BufferedWriter similaritySetWriter  = WriterUtils.to(Paths.get("corpora", id, "similarity-set.jsonl.gz").toString());

        ObjectMapper jsonMapper = new ObjectMapper();
        for(int i=0; i< multiLangArticles.size(); i++){

            MultiLangArticle multiLangArticle = multiLangArticles.get(i);

            String lang = langs.get(i % langs.size());

            Document doc = new Document(multiLangArticle.getId(), lang, multiLangArticle.getArticles().get(lang).getContent());

            if (testArticles.containsKey(multiLangArticle.getId())){
                testSetWriter.write(jsonMapper.writeValueAsString(doc)+"\n");
            }else{
                trainingSetWriter.write(jsonMapper.writeValueAsString(doc)+"\n");
            }
        }

        for(Relation articleSimilarity : articleSimilarities){
            if (testArticles.containsKey(articleSimilarity.getX()) && testArticles.containsKey(articleSimilarity.getY())){
                similaritySetWriter.write(jsonMapper.writeValueAsString(articleSimilarity)+"\n");
            }
        }

        trainingSetWriter.close();
        testSetWriter.close();
        similaritySetWriter.close();

        LOG.info("Saved datasets at: " + baseDir.getAbsolutePath());

    }


    private Map<String,Integer> prepareTest(double threshold){
        LOG.info("Analyzing test set for threshold: " + threshold + " ...");
        Map<String,Integer> refArticles = new ConcurrentHashMap<>();
        articleSimilarities.parallelStream().filter(rel -> rel.getScore()>threshold).forEach(rel -> {
            refArticles.put(rel.getX(),0);
            refArticles.put(rel.getY(),0);
        });
        return refArticles;
    }
}

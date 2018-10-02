package io.github.cbadenes.crosslingual.data;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.google.common.primitives.Doubles;
import org.librairy.service.modeler.facade.model.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicModel {

    private static final Logger LOG = LoggerFactory.getLogger(TopicModel.class);
    private final List<Document> documents;
    private final Parameters parameters;
    private final Pipe pipe;
    private TopicInferencer inferencer;
    private ParallelTopicModel model;

    private long numInstances = 0l;
    private long numWords = 0l;

    public TopicModel(List<Document> documents, Parameters parameters) {
        this.documents = documents;
        this.parameters = parameters;

        LOG.info("Training a new topic model with " + documents.size() + " docs and " + parameters);

        // Create Instances
        this.pipe = new SerialPipes(Arrays.asList(new Input2CharSequence("UTF-8"),new CharSequence2TokenSequence(Pattern.compile("\\S+")),new TokenSequence2FeatureSequence()));
        InstanceList instances = new InstanceList(pipe);

        for(Document doc : documents){
            String data = doc.getText();
            String name = doc.getId();
            String target = "";
            String source = "";
            instances.addThruPipe(new Instance(data,target,name,source));
        }
        numWords = instances.getDataAlphabet().size();
        numInstances = instances.size();

        // Prepare Model
        this.model = new ParallelTopicModel(parameters.getNumTopics(), parameters.getNumTopics() * parameters.getAlpha(), parameters.getBeta());
        model.addInstances(instances);
        model.setNumThreads((Runtime.getRuntime().availableProcessors() > 1) && (numInstances/Runtime.getRuntime().availableProcessors() >= 100)? Runtime.getRuntime().availableProcessors() -1: 1);
        model.printLogLikelihood = false;
        model.setOptimizeInterval(parameters.getNumIterations()/2);
        model.setTopicDisplay(parameters.getNumIterations()/2,5);
        model.setNumIterations(parameters.getNumIterations());

        // Train
        try {
            model.estimate();
            this.inferencer = model.getInferencer();

            getTopics().entrySet().forEach(entry -> LOG.info("\t" + entry.getValue()));

        } catch (Exception e) {
            LOG.error("Error building a topic model",e);
        }



    }


    public Map<Integer, Topic> getTopics() {

        int numTopics = this.model.getNumTopics();
        Alphabet alphabet = this.model.getAlphabet();

        ArrayList<TreeSet<IDSorter>> topicSortedWords = this.model.getSortedWords();

        Map<Integer,Topic> result = new HashMap<>();

        for (int topic = 0; topic < numTopics; topic++) {

            Topic tp = new Topic(String.valueOf(topic));

            TreeSet<IDSorter> sortedWords = topicSortedWords.get(topic);

            if (sortedWords.isEmpty()){
                result.put(topic,tp);
                continue;
            }

            Double totalWeight = sortedWords.stream().map(w -> w.getWeight()).reduce((w1, w2) -> w1 + w2).get();

            // How many words should we report? Some topics may have fewer than
            //  the default number of words with non-zero weight.
            long limit = numWords<0? sortedWords.size() : numWords;
            if (sortedWords.size() < numWords) { limit = sortedWords.size(); }

            Iterator<IDSorter> iterator = sortedWords.iterator();
            for (int i=0; i < limit; i++) {
                IDSorter info = iterator.next();
                tp.add(String.valueOf(alphabet.lookupObject(info.getID())), info.getWeight()/totalWeight);
            }

            result.put(topic,tp);
        }

        return result;
    }

    public Map<String,List<Double>> inference(List<Document> documents){

        Map<String,List<Double>> topicDistributions = new ConcurrentHashMap<>();

        InstanceList instances = new InstanceList(this.pipe);

        documents.forEach(doc -> instances.addThruPipe(new Instance(doc.getText(),"",doc.getId(),"")));

        for(Instance instance : instances){

            double[] topicDistribution = inferencer.getSampledDistribution(instance, parameters.getNumIterations(), 1, 0);
            topicDistributions.put((String)instance.getName(), Doubles.asList(topicDistribution));
        }

        return topicDistributions;

    }

    public static class Parameters{

        int numTopics = 10;

        double alpha = 0.1;

        double beta = 0.001;

        int numIterations = 1000;

        public Parameters() {
        }

        public int getNumTopics() {
            return numTopics;
        }

        public Parameters setNumTopics(int numTopics) {
            this.numTopics = numTopics;
            return this;
        }

        public double getAlpha() {
            return alpha;
        }

        public Parameters setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        public double getBeta() {
            return beta;
        }

        public Parameters setBeta(double beta) {
            this.beta = beta;
            return this;
        }

        public int getNumIterations() {
            return numIterations;
        }

        public Parameters setNumIterations(int numIterations) {
            this.numIterations = numIterations;
            return this;
        }

        @Override
        public String toString() {
            return "Parameters{" +
                    "numTopics=" + numTopics +
                    ", alpha=" + alpha +
                    ", beta=" + beta +
                    ", numIterations=" + numIterations +
                    '}';
        }
    }
}

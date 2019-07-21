package io.github.cbadenes.crosslingual.io;

import com.google.common.base.Strings;
import io.github.cbadenes.crosslingual.data.AnnotatedDocument;
import io.github.cbadenes.crosslingual.data.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SolrClient {

    private static final Logger LOG = LoggerFactory.getLogger(SolrClient.class);
    private HttpSolrClient solrClient;
    private AtomicInteger counter;
    private final String endpoint;


    public SolrClient(String endpoint) {
        this.endpoint = endpoint;
    }

    public void open(){
        this.solrClient = new HttpSolrClient.Builder(endpoint).build();
        this.counter = new AtomicInteger();
    }

    public Boolean update(String id, Map<String, Object> data) {
        Boolean saved = false;
        try{
            SolrInputDocument sd = new SolrInputDocument();
            sd.addField("id",id.replaceAll(" ",""));

            for(Map.Entry<String,Object> entry : data.entrySet()){
                String fieldName = entry.getKey();
                Object td = entry.getValue();
                Map<String,Object> updatedField = new HashMap<>();
                updatedField.put("set", td);
                sd.addField(fieldName, updatedField);
            }

            solrClient.add(sd);

            LOG.info("[" + counter.incrementAndGet() + "] Document '" + id + "' saved");

            if (counter.get() % 100 == 0){
                LOG.info("Committing partial annotations["+ this.counter.get() +"]");
                solrClient.commit();
            }

            saved = true;
        }catch (Exception e){
            LOG.error("Unexpected error annotating doc: " + id, e);
        }
        return saved;

    }

    public Boolean save(String id, Map<String, Object> data) {
        Boolean saved = false;
        try{
            SolrInputDocument sd = new SolrInputDocument();
            sd.addField("id",id.replaceAll(" ",""));

            for(String fieldName : data.keySet()){
                sd.addField(fieldName, data.get(fieldName));
            }

            solrClient.add(sd);

            LOG.info("[" + counter.incrementAndGet() + "] Document '" + id + "' saved");

            if (counter.get() % 100 == 0){
                LOG.info("Committing partial annotations["+ this.counter.get() +"]");
                solrClient.commit();
            }

            saved = true;
        }catch (Exception e){
            LOG.error("Unexpected error annotating doc: " + id, e);
        }
        return saved;

    }

    public boolean save(Document doc){

        try {
            SolrInputDocument document = new SolrInputDocument();
            document.addField("id",doc.getId());
            document.addField("name_s",doc.getName());
            document.addField("txt_t",doc.getContent());
            document.addField("size_i", Strings.isNullOrEmpty(doc.getContent())? 0 : doc.getContent().length());
            document.addField("labels_t",doc.getLabels().stream().map(r -> r.trim().replace(" ","_")).collect(Collectors.joining(" ")));
            document.addField("format_s",doc.getFormat());
            document.addField("lang_s",doc.getLanguage());
            document.addField("source_s",doc.getSource());
            document.addField("date_dt",doc.getDate());

            solrClient.add(document);

            if (counter.incrementAndGet() % 100 == 0) {
                LOG.info(counter.get() + " documents saved");
                solrClient.commit();
            }

        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            return false;
        }

        return true;
    }

    public boolean save(AnnotatedDocument doc){

        try {
            SolrInputDocument document = new SolrInputDocument();
            document.addField("id",doc.getId());
            document.addField("name_s",doc.getName());
            document.addField("labels_t",doc.getLabels());
            document.addField("lang_s",doc.getLang());
            document.addField("topics0_t",doc.getTopics0());
            document.addField("topics1_t",doc.getTopics1());
            document.addField("topics2_t",doc.getTopics2());
            document.addField("synset0_t",doc.getSynset0());
            document.addField("synset1_t",doc.getSynset1());
            document.addField("synset2_t",doc.getSynset2());
            solrClient.add(document);

            if (counter.incrementAndGet() % 100 == 0) {
                LOG.info(counter.get() + " documents saved");
                solrClient.commit();
            }

        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            return false;
        }

        return true;
    }
    
    public SolrIterator query(String query, List<String> fields, Integer maxSize) throws IOException {
        return new SolrIterator(query, fields, maxSize);
    }

    public Map<String,Object> get(String id, List<String> fields) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.addField("id");
        fields.forEach(f -> solrQuery.addField(f));
        solrQuery.setQuery("id:"+id);
        QueryResponse rsp = solrClient.query(solrQuery);
        SolrDocumentList resultList = rsp.getResults();
        Map<String,Object> data = new HashMap<>();
        if (resultList.isEmpty()) return data;
        SolrDocument result = resultList.get(0);
        fields.forEach(f -> data.put(f, result.getFieldValue(f)));
        return data;
    }

    public void close(){
        try {
            solrClient.commit();
        } catch (Exception e) {
            LOG.error("Unexpected error",e);
        }
    }


    public Map<String,Object> getMoreLikeThis(String docId, List<String> fields, Integer max) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setMoreLikeThis(true);
        query.set(MoreLikeThisParams.MATCH_INCLUDE, true);
        query.set(MoreLikeThisParams.MIN_DOC_FREQ, 1);
        query.set(MoreLikeThisParams.MIN_TERM_FREQ, 1);
        query.set(MoreLikeThisParams.MIN_WORD_LEN, 2);
        query.set(MoreLikeThisParams.BOOST, false);
        query.set(MoreLikeThisParams.MAX_QUERY_TERMS, 1000);
        query.set(MoreLikeThisParams.SIMILARITY_FIELDS, fields.stream().collect(Collectors.joining(",")));
        query.setQuery("id:" + docId);
        query.set("fl", "id,score");
        //query.addFilterQuery("field3:" + field3);
        query.set(MoreLikeThisParams.DOC_COUNT, max);
        QueryResponse rsp = solrClient.query(query);
        NamedList<SolrDocumentList> resultList = rsp.getMoreLikeThis();
        Map<String,Object> data = new HashMap<>();

        Iterator<Map.Entry<String, SolrDocumentList>> iterator = resultList.iterator();

        while(iterator.hasNext()){
            Map.Entry<String, SolrDocumentList> val = iterator.next();
            val.getValue().forEach( d -> data.put((String)d.getFieldValue("id"),d.getFieldValue("score")));
        }
        return data;
    }

    public Map<String, Double> getBooleanQuery(Map<String,List<String>> condition, Integer max, Boolean internalMandatory, Boolean combinedMandatory) throws IOException, SolrServerException {
        String internalOperator = internalMandatory? " AND " : " OR ";
        String combinedOperator = combinedMandatory? " AND " : " OR ";
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.addField("id");
        solrQuery.addField("score");
        solrQuery.setRows(max);

        //List<String> composeQuery = hierarchicalCondition(condition, internalOperator);
        List<String> composeQuery = flatCondition(condition, internalOperator);

        String composeQueryString = composeQuery.stream().collect(Collectors.joining(combinedOperator));

        solrQuery.setQuery(composeQueryString);
        QueryResponse rsp = solrClient.query(solrQuery);
        SolrDocumentList resultList = rsp.getResults();
        Map<String,Double> data = new HashMap<>();
        if (!resultList.isEmpty()){
            for(SolrDocument doc : resultList){

                String id       = (String) doc.get("id");
                Float score     = (Float) doc.get("score");
                data.put(id,new Double(score));
            }
        }

        return data;
    }


    private List<String> flatCondition(Map<String,List<String>> condition, String internalOperator){
        List<String> composeQuery = new ArrayList<>();

        List<String> vals = condition.entrySet().stream().flatMap(e -> e.getValue().stream()).distinct().collect(Collectors.toList());

        Set<String> keys = condition.keySet();
        for(String key: keys){
            vals.forEach( l -> composeQuery.add(key+":"+l));
        }
        return composeQuery.stream().limit(300).collect(Collectors.toList());
    }

    private List<String> hierarchicalCondition(Map<String,List<String>> condition, String internalOperator){
        int boost = condition.size();
        Map<Integer,List<String>> accCondition = new HashMap<>();
        List<String> accKeys = new ArrayList<>();
        List<String> composeQuery = new ArrayList<>();
        int totalCount = 0;
        Set<String> keys = condition.keySet();
        List<String> sortedKeys = keys.stream().sorted((a, b) -> a.compareTo(b)).collect(Collectors.toList());
        for(String key : sortedKeys){
            int boostValue = condition.size()*condition.size();
            List<String> params = condition.get(key);
            if ((totalCount + params.size()) >= 200) continue;
            totalCount += params.size();
            composeQuery.add(params.stream().map(a -> key+":"+a+"^"+boostValue).collect(Collectors.joining(internalOperator)));
            for(Map.Entry<Integer,List<String>> entry : accCondition.entrySet()){
                int nboostValue = entry.getKey() * boost;
                List<String> nparams = entry.getValue();
                if ((totalCount + nparams.size()) >= 200) continue;
                totalCount += nparams.size();
                composeQuery.add(nparams.stream().map(a -> key+":"+a+"^"+nboostValue).collect(Collectors.joining(internalOperator)));
            }
            accCondition.put(boost,params);
            boost--;
        }
        return composeQuery;
    }

    public class SolrIterator{

        private final Integer window = 500;
        private final SolrQuery solrQuery;
        private final Integer maxSize;
        private final List<String> fields;
        private String nextCursorMark;
        private String cursorMark;
        private SolrDocumentList solrDocList;
        private AtomicInteger index;

        public SolrIterator(String query, List<String> fields, Integer maxSize) throws IOException {
            this.maxSize = maxSize;
            this.fields = fields;
            this.solrQuery = new SolrQuery();
            solrQuery.setRows(window);
            solrQuery.addField("id");
            fields.forEach(f -> solrQuery.addField(f));
            solrQuery.setQuery(query);
            solrQuery.addSort("id", SolrQuery.ORDER.asc);
            this.nextCursorMark = CursorMarkParams.CURSOR_MARK_START;
            query();
        }

        private void query() throws IOException {
            try{
                this.cursorMark = nextCursorMark;
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
                QueryResponse rsp = solrClient.query(solrQuery);
                this.nextCursorMark = rsp.getNextCursorMark();
                this.solrDocList = rsp.getResults();
                this.index = new AtomicInteger();
            }catch (Exception e){
                throw new IOException(e);
            }
        }

        public Optional<Map<String,Object>> next(){
            try{
                if (index.get() >= solrDocList.size()) {
                    if (index.get() < window){
                        return Optional.empty();
                    }
                    query();
                }

                if (cursorMark.equals(nextCursorMark)) {
                    return Optional.empty();
                }

                if ((maxSize > 0) && (index.get() > maxSize)){
                    return Optional.empty();
                }

                SolrDocument solrDoc = solrDocList.get(index.getAndIncrement());

                Map<String,Object> data = new HashMap<>();

                data.put("id",solrDoc.getFieldValue("id"));
                fields.forEach(f -> data.put(f, solrDoc.getFieldValue(f)));

                return Optional.of(data);
            }catch (Exception e){
                LOG.error("Unexpected error on iterated list of solr docs",e);
                if (e instanceof IndexOutOfBoundsException) return Optional.empty();
                return Optional.of(new HashMap<>());
            }
        }
    }
}

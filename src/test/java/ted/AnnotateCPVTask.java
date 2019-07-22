package ted;

import com.google.common.collect.ImmutableMap;
import io.github.cbadenes.crosslingual.io.SolrClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AnnotateCPVTask {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotateCPVTask.class);



    @Test
    public void annotate(){

        SolrClient input    = new SolrClient("http://librairy.linkeddata.es/solr/documents");
        input.open();

        try {
            SolrClient.SolrIterator inputIterator = input.query("source_s:ted AND labels_t:[* TO *]", Arrays.asList("id", "labels_t"), 50000);

            Optional<Map<String, Object>> doc = Optional.empty();
            int counter = 1;
            while((doc = inputIterator.next()).isPresent()){

                Map<String, Object> values = doc.get();

                String id           = (String) values.get("id");
                String cpvList      = (String) values.get("labels_t");

                String cpvCodes         = Arrays.stream(cpvList.split(" ")).distinct().collect(Collectors.joining(" "));
                String cpvDivisions     = Arrays.stream(cpvList.split(" ")).filter(c -> c.length()>2).map(c -> c.substring(0, 2)).distinct().collect(Collectors.joining(" "));
                String cpvGroups        = Arrays.stream(cpvList.split(" ")).filter(c -> c.length()>3).map(c -> c.substring(0, 3)).distinct().collect(Collectors.joining(" "));
                String cpvClasses       = Arrays.stream(cpvList.split(" ")).filter(c -> c.length()>4).map(c -> c.substring(0, 4)).distinct().collect(Collectors.joining(" "));
                String cpvCategories    = Arrays.stream(cpvList.split(" ")).filter(c -> c.length()>5).map(c -> c.substring(0, 5)).distinct().collect(Collectors.joining(" "));
                
                input.update(id, ImmutableMap.of("divisions_t",cpvDivisions, "categories_t",cpvCategories, "groups_t", cpvGroups, "classes_t", cpvClasses));

                LOG.info("["+counter++ +"] - " + id);

            }

        } catch (IOException e) {
            LOG.error("Unexpected SOLR error",e);
        } finally {
            input.close();
        }


    }

}

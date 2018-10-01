package io.github.cbadenes.crosslingual.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cbadenes.crosslingual.data.Document;
import io.github.cbadenes.crosslingual.data.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ReaderUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderUtils.class);


    public static BufferedReader from(String path) throws IOException {

        InputStreamReader inputStreamReader;
        if (path.startsWith("http")){
            inputStreamReader = new InputStreamReader(new GZIPInputStream(new URL(path).openStream()));
        }else{
            inputStreamReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(path)));
        }

        return new BufferedReader(inputStreamReader);
    }


    public static List<Document> readDocuments(String path) throws IOException {

        LOG.info("Reading documents from '"+ path + "' ...");
        ObjectMapper jsonMapper = new ObjectMapper();

        BufferedReader reader = ReaderUtils.from(path);
        String line = null;
        List<Document> documents = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        while ((line = reader.readLine()) != null){

            Document document = jsonMapper.readValue(line, Document.class);
            documents.add(document);
            counter.incrementAndGet();

        }
        reader.close();
        LOG.info(counter.get() + " documents read");
        return documents;

    }

    public static Map<String, List<Relation>> readRelations(String path) throws IOException {

        LOG.info("Reading relations from '"+ path + "' ...");
        ObjectMapper jsonMapper = new ObjectMapper();

        BufferedReader reader = ReaderUtils.from(path);
        String line = null;
        Map<String,List<Relation>> relations = new HashMap<>();
        AtomicInteger counter = new AtomicInteger();
        while ((line = reader.readLine()) != null){

            Relation relation = jsonMapper.readValue(line, Relation.class);

            // Update X
            List<Relation> relX = new ArrayList<>();
            if (relations.containsKey(relation.getX())){
                relX = relations.get(relation.getX());
            }
            relX.add(new Relation(relation.getType(), relation.getX(), relation.getY(), relation.getScore()));
            relations.put(relation.getX(), relX);

            // Update Y
            List<Relation> relY = new ArrayList<>();
            if (relations.containsKey(relation.getY())){
                relY = relations.get(relation.getY());
            }
            relY.add(new Relation(relation.getType(), relation.getY(), relation.getX(), relation.getScore()));
            relations.put(relation.getY(), relY);
            counter.incrementAndGet();


        }
        reader.close();
        LOG.info(counter.get() + " relations read");
        return relations;

    }

}

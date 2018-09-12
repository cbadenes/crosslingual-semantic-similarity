package io.github.cbadenes.crosslingual.services;

import com.google.common.base.Strings;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class PersistentRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentRepository.class);


    private File index;
    private ConcurrentMap<String,String> map;
    private DB db;

    public PersistentRepository(String path) {
        try{
            index = new File(path);
            db = DBMaker.fileDB(index.getAbsolutePath()).checksumHeaderBypass().closeOnJvmShutdown().make();
            map = db.hashMap("map", Serializer.STRING, Serializer.STRING).createOrOpen();
        }catch (Exception e){
            LOG.error("Error initializing db",e);
        }
    }

    public void close(){
        db.commit();
        db.close();
    }

    public void add(String id, String text){
        map.put(id, text);
    }

    public boolean contains(String id){
        return map.containsKey(id);
    }


    public Optional<String> get(String id){
        String text = map.getOrDefault(id, "");

        if (Strings.isNullOrEmpty(text)) return Optional.empty();

        return Optional.of(text);
    }


}

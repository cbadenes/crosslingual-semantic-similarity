package io.github.cbadenes.crosslingual.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.librairy.service.learner.facade.rest.model.Document;
import org.librairy.service.learner.facade.rest.model.ModelParameters;
import org.librairy.service.modeler.facade.rest.model.Shape;
import org.librairy.service.modeler.facade.rest.model.ShapeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LibrairyService {

    private static final Logger LOG = LoggerFactory.getLogger(LibrairyService.class);

    static{
        Unirest.setDefaultHeader("Accept", "application/json");
        Unirest.setDefaultHeader("Content-Type", "application/json");

        com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
//        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        jacksonObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Unirest.setObjectMapper(new ObjectMapper() {

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private final String endpoint;
    private final String user;
    private final String pwd;


    public LibrairyService(String endpoint, String user, String pwd) {
        this.endpoint = endpoint;
        this.user = user;
        this.pwd = pwd;
    }

    public boolean save(Document document, Boolean multigrams, Boolean raw){
        try {
            HttpResponse<String> response = Unirest.post(endpoint + "/documents").queryString("multigrams",multigrams).queryString("raw",raw).basicAuth(user, pwd).body(document).asString();

            if (response.getStatus() != 200 && response.getStatus() != 201){
                return false;
            }

            return true;

        } catch (UnirestException e) {
            LOG.error("Unexpected error",e);
            return false;
        }
    }

    public Shape inference(ShapeRequest request){
        try {
            HttpResponse<Shape> response = Unirest.post(endpoint + "/shape").basicAuth(user, pwd).body(request).asObject(Shape.class);

            if (response.getStatus() != 200 && response.getStatus() != 201){
                throw new RuntimeException();
            }

            return response.getBody();

        } catch (UnirestException e) {
            LOG.error("Unexpected error",e);
            throw new RuntimeException(e);
        }
    }

    public boolean reset(){
        try {
            HttpResponse<String> response = Unirest.delete(endpoint + "/documents").basicAuth(user, pwd).asString();

            if (response.getStatus() != 200 && response.getStatus() != 202){
                return false;
            }

            return true;

        } catch (UnirestException e) {
            LOG.error("Unexpected error",e);
            return false;
        }
    }

    public boolean train(Map<String, String> parameters){
        try {

            ModelParameters modelParameters = new ModelParameters();
//            Map<String, String> parameters = ImmutableMap.of(
//                    "algorithm","llda",
//                    "language","en",
//                    "email","cbadenes@fi.upm.es"
//            );
            modelParameters.setParameters(parameters);

            HttpResponse<String> response = Unirest.post(endpoint + "/dimensions").basicAuth(user, pwd).body(modelParameters).asString();

            if (response.getStatus() != 200 && response.getStatus() != 201 && response.getStatus() != 202){
                return false;
            }

            return true;

        } catch (UnirestException e) {
            LOG.error("Unexpected error",e);
            return false;
        }
    }

    public boolean export(Map<String, String> parameters){
        try {


            /**
             * '{ "contactEmail": "string", "contactName": "string", "contactUrl": "string",  "credentials": { "email": "string",  "password": "string", "repository": "string", "username": "string"  },  "description": "string",  "licenseName": "Apache License Version 2.0",  "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0",  "title": "string"}'
             */

            JSONObject request = new JSONObject();
            Map<String,Map<String,String>> innerParams = new HashMap<>();
            for(String key: parameters.keySet()){

                if (key.contains(".")){
                    Map<String,String> partialParams = new HashMap<>();
                    String innerKey = StringUtils.substringBefore(key, ".");
                    if (innerParams.containsKey(innerKey)){
                        partialParams = innerParams.get(innerKey);
                    }
                    partialParams.put(StringUtils.substringAfter(key, "."),parameters.get(key));
                    innerParams.put(innerKey,partialParams);

                }else{
                    request.put(key,parameters.get(key));
                }
            }
            for(String in : innerParams.keySet()){
                request.put(in, innerParams.get(in));
            }

            HttpResponse<String> response = Unirest.post(endpoint + "/exports").basicAuth(user, pwd).body(request).asString();

            if (response.getStatus() != 200 && response.getStatus() != 201 && response.getStatus() != 202){
                return false;
            }

            return true;

        } catch (UnirestException e) {
            LOG.error("Unexpected error",e);
            return false;
        }
    }

    public boolean isCompleted(){
        try {

            HttpResponse<JsonNode> response = Unirest.get(endpoint + "/dimensions").basicAuth(user, pwd).asJson();

            if (response.getStatus() != 200 && response.getStatus() != 201 && response.getStatus() != 202){
                LOG.warn("Error on response: " + response.getStatus() + ":" + response.getStatusText());
                return true;
            }

            JSONArray dimensions = response.getBody().getObject().getJSONArray("dimensions");

            return dimensions.length() > 0;

        } catch (UnirestException e) {
            LOG.error("Unexpected error",e);
            return false;
        }
    }
}

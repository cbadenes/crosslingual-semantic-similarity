package io.github.cbadenes.crosslingual.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.github.cbadenes.crosslingual.data.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class librAIryClient {

    private static final Logger LOG = LoggerFactory.getLogger(librAIryClient.class);

    private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper;


    public librAIryClient() {

        jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);

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


        try {

            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

            } };



            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            Unirest.setHttpClient(httpclient);


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Optional<HierarchicalTopics> getTopics(String text, String endpoint){

        if (Strings.isNullOrEmpty(text)) return Optional.empty();

        Text txt = new Text();
        txt.setText(text);

        try {
            Map<String,String> headers = new HashMap<>();
            headers.put("Content-Type","application/json");
            headers.put("accept","application/json");
            HttpResponse<JsonNode> result = Unirest.post(endpoint+"/classes")
                    .headers(headers)
                    .body(txt)
                    .asJson();


            HierarchicalTopics topics = new HierarchicalTopics();

            Iterator<Object> iterator = result.getBody().getArray().iterator();
            while(iterator.hasNext()){
                JSONObject json = (JSONObject) iterator.next();
                topics.addToLevel(json.getInt("id"), json.getString("name"));
            }

            return Optional.of(topics);

        } catch (Exception e) {
            LOG.error("Unexpected API error: "+e.getMessage());
            return Optional.empty();
        }


    }

    public Map<String,List<String>> getTopics(String endpoint){

        try {
            Map<String,String> headers = new HashMap<>();
            headers.put("Content-Type","application/json");
            headers.put("accept","application/json");
            HttpResponse<JsonNode> result = Unirest.get(endpoint+"/topics")
                    .headers(headers)
                    .asJson();

            Map<String,List<String>> topics = new HashMap<>();

            Iterator<Object> iterator = result.getBody().getArray().iterator();
            while(iterator.hasNext()){
                JSONObject json = (JSONObject) iterator.next();
                int id          = json.getInt("id");
                String name     = json.getString("name");
                String description = json.getString("description");
                topics.put(name, Arrays.asList(description.split(",")));
            }

            return topics;

        } catch (Exception e) {
            LOG.error("Unexpected API error: "+ e.getMessage());
            return new HashMap<>();
        }


    }




    public List<Annotation> getAnnotations (AnnotationRequest request){
        try {

            if (Strings.isNullOrEmpty(request.getText())) return Collections.emptyList();

            List<Annotation> annotations = new ArrayList<>();
            Map<String,String> headers = new HashMap<>();
            headers.put("Content-Type","application/json");
            headers.put("accept","application/json");
            String endpoint = "http://librairy.linkeddata.es/nlp/annotations";
            //String endpoint = "http://localhost:8081/annotations";
            HttpResponse<JsonNode> result = Unirest.post(endpoint)
                    .headers(headers)
                    .body(request)
                    .asJson();

            Iterator<Object> iterator = result.getBody().getObject().getJSONArray("annotatedText").iterator();
            while(iterator.hasNext()){
                JSONObject json = (JSONObject) iterator.next();
                Annotation annotation = new Annotation();

                JSONObject tokenJson = json.getJSONObject("token");
                Token token = new Token();
                token.setLemma(tokenJson.getString("lemma"));
                token.setTarget(tokenJson.getString("target"));
                token.setPos(tokenJson.getString("pos"));
                annotation.setToken(token);

                annotation.setOffset(json.getInt("offset"));

                List<String> synset = new ArrayList<>();
                Iterator<Object> it = json.getJSONArray("synset").iterator();
                while(it.hasNext()){
                    String val = (String) it.next();
                    synset.add(val);
                }

                annotation.setSynset(synset);

                annotations.add(annotation);

            }

            return annotations;

        } catch (Exception e) {
            LOG.error("Unexpected API error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

}

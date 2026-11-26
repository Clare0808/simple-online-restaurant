package com.lesavor.restaurant;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class MongoStore {
    private static final String COLLECTION = "logins";

    private final MongoTemplate mongo;

    public MongoStore(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public Map<String, Object> findByEmail(String email) {
        Document document = mongo.findOne(
                new Query(Criteria.where("email").is(email)), Document.class, COLLECTION);
        return document == null ? null : convert(document);
    }

    public boolean existsEmail(String email) {
        return mongo.exists(
                new Query(Criteria.where("email").is(email)), Document.class, COLLECTION);
    }

    public Map<String, Object> save(Map<String, Object> user) {
        Document document = new Document(user);
        mongo.insert(document, COLLECTION);
        return convert(document);
    }

    private Map<String, Object> convert(Document document) {
        Map<String, Object> result = new LinkedHashMap<>();
        document.forEach((key, value) -> {
            if (value instanceof ObjectId objectId) {
                result.put(key, objectId.toHexString());
            } else {
                result.put(key, value);
            }
        });
        return result;
    }
}

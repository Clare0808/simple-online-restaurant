package com.lesavor.restaurant;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class MongoStore {
    private static final String COLLECTION = "logins";

    private final MongoTemplate mongo;

    public MongoStore(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public Map<String, Object> findByEmail(String email) {
        Document document = mongo.findOne(new Query(Criteria.where("email").is(email)), Document.class, COLLECTION);
        return document == null ? null : convert(document);
    }

    public List<Map<String, Object>> all(String collection) {
        return mongo.findAll(Document.class, collection).stream().map(this::convert).toList();
    }

    public List<Map<String, Object>> findAll(String collection, String field, Object value) {
        return mongo.find(new Query(Criteria.where(field).is(value)), Document.class, collection)
                .stream().map(this::convert).toList();
    }

    public boolean existsEmail(String email) {
        return mongo.exists(
                new Query(Criteria.where("email").is(email)), Document.class, COLLECTION);
    }

    public Map<String, Object> save(Map<String, Object> user) {
        return save(COLLECTION, user);
    }

    public Map<String, Object> save(String collection, Map<String, Object> value) {
        Document document = toDocument(value);
        mongo.insert(document, collection);
        return convert(document);
    }

    public void delete(String collection, List<?> ids) {
        if (ids == null) return;
        for (Object id : ids) {
            String value = Objects.toString(id, "");
            if (ObjectId.isValid(value)) {
                mongo.remove(new Query(Criteria.where("_id").is(new ObjectId(value))), collection);
            }
        }
    }

    public void deleteOwned(String collection, List<?> ids, String email) {
        for (Object id : ids) {
            String value = Objects.toString(id, "");
            if (ObjectId.isValid(value)) {
                Query query = new Query(Criteria.where("_id").is(new ObjectId(value)).and("email").is(email));
                mongo.remove(query, collection);
            }
        }
    }

    public boolean updateOrderAmount(String id, String email, int amount) {
        if (!ObjectId.isValid(id) || amount < 1 || amount > 99) return false;
        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)).and("email").is(email));
        Document order = mongo.findOne(query, Document.class, "orders");
        if (order == null || !(order.get("price") instanceof Number price)) return false;
        Update update = new Update()
                .set("amount", amount)
                .set("total", price.doubleValue() * amount);
        return mongo.updateFirst(query, update, "orders").getMatchedCount() > 0;
    }

    private Map<String, Object> convert(Document document) {
        Map<String, Object> result = new LinkedHashMap<>();
        document.forEach((key, value) -> result.put(key, normalize(value)));
        return result;
    }

    private Object normalize(Object value) {
        if (value instanceof ObjectId objectId) return objectId.toHexString();
        if (value instanceof Document document) return convert(document);
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(Objects.toString(key), normalize(item)));
            return result;
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>();
            list.forEach(item -> result.add(normalize(item)));
            return result;
        }
        return value;
    }

    private Document toDocument(Map<String, Object> map) {
        Document document = new Document();
        map.forEach((key, value) -> document.put(key, toBson(value)));
        return document;
    }

    private Object toBson(Object value) {
        if (value instanceof Map<?, ?> map) {
            Document document = new Document();
            map.forEach((key, item) -> document.put(Objects.toString(key), toBson(item)));
            return document;
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>();
            list.forEach(item -> result.add(toBson(item)));
            return result;
        }
        return value;
    }
}

package com.lesavor.restaurant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RecordsController {
    private final MongoStore mongoStore;

    public RecordsController(MongoStore mongoStore) {
        this.mongoStore = mongoStore;
    }

    @GetMapping("/get-orders")
    public ResponseEntity<?> orders(@RequestParam String email) {
        return emailRequired(email, () -> mongoStore.findAll("orders", "email", normalizeEmail(email)));
    }

    @GetMapping("/get-buy-orders")
    public ResponseEntity<?> purchases(@RequestParam String email) {
        return emailRequired(email, () -> mongoStore.findAll("buys", "email", normalizeEmail(email)));
    }

    @PostMapping("/send-orders")
    public ResponseEntity<?> addOrder(@RequestBody Map<String, Object> input) {
        String email = normalizeEmail(input.get("email"));
        if (email.isBlank()) return error("請先登入再加入購物車。");

        Map<String, Object> order = pick(input, "email", "name", "amount", "price", "image", "check", "total");
        order.put("email", email);
        Map<String, Object> saved = mongoStore.save("orders", order);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true, "message", "已加入購物車", "order", saved));
    }

    @PostMapping("/send-buy-orders")
    public ResponseEntity<?> addPurchase(@RequestBody Map<String, Object> input) {
        String email = normalizeEmail(input.get("email"));
        Object list = input.get("list");
        if (email.isBlank()) return error("請先登入再送出訂單。");
        if (!(list instanceof List<?> items) || items.isEmpty()) return error("訂單沒有餐點。");

        Map<String, Object> purchase = pick(input, "email", "list", "total", "code", "date");
        purchase.put("email", email);
        Map<String, Object> saved = mongoStore.save("buys", purchase);
        Object rawOrderIds = input.get("orderIds");
        if (rawOrderIds instanceof List<?> orderIds) {
            mongoStore.deleteOwned("orders", orderIds, email);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true, "message", "訂單已送出", "buy", saved));
    }

    @PostMapping("/update-order")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> input) {
        String email = normalizeEmail(input.get("email"));
        String id = Objects.toString(input.get("id"), "");
        int amount;
        try {
            amount = Integer.parseInt(Objects.toString(input.get("amount"), ""));
        } catch (NumberFormatException error) {
            return error("餐點數量格式錯誤。");
        }
        if (email.isBlank()) return error("請先登入。");
        if (!mongoStore.updateOrderAmount(id, email, amount)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "找不到購物車餐點，或數量超出範圍。"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "餐點數量已更新"));
    }

    @PostMapping("/remove-orders")
    public ResponseEntity<?> removeOrders(@RequestBody Map<String, Object> input) {
        return removeOwned("orders", input);
    }

    @PostMapping("/remove-buy-orders")
    public ResponseEntity<?> removePurchases(@RequestBody Map<String, Object> input) {
        return removeOwned("buys", input);
    }

    private ResponseEntity<?> removeOwned(String collection, Map<String, Object> input) {
        String email = normalizeEmail(input.get("email"));
        Object rawIds = input.get("list");
        if (email.isBlank()) return error("請先登入。");
        if (!(rawIds instanceof List<?> ids)) return error("刪除清單格式錯誤。");
        mongoStore.deleteOwned(collection, ids, email);
        return ResponseEntity.ok(Map.of("success", true, "message", "已刪除"));
    }

    private ResponseEntity<?> emailRequired(String email, java.util.function.Supplier<List<Map<String, Object>>> query) {
        if (email == null || email.isBlank()) return error("缺少使用者 E-mail。");
        return ResponseEntity.ok(query.get());
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", message));
    }

    private Map<String, Object> pick(Map<String, Object> input, String... fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : fields) {
            if (input.containsKey(field)) result.put(field, input.get(field));
        }
        return result;
    }

    private String normalizeEmail(Object value) {
        return Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
    }
}

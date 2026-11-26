package com.lesavor.restaurant;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);

    private final MongoStore mongoStore;
    private final String jwtSecret;

    public AccountController(MongoStore mongoStore, @Value("${app.jwt.secret}") String jwtSecret) {
        this.mongoStore = mongoStore;
        this.jwtSecret = jwtSecret;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, Object> input) {
        String email = text(input.get("email")).trim().toLowerCase(Locale.ROOT);
        String name = text(input.get("name")).trim();
        String number = text(input.get("number")).trim();
        String password = text(input.get("password"));

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return error(HttpStatus.BAD_REQUEST, "請輸入有效的 E-mail。");
        }
        if (name.isBlank() || number.isBlank() || password.isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "名稱、電話號碼與密碼不可空白。");
        }
        if (mongoStore.existsEmail(email)) {
            return error(HttpStatus.CONFLICT, "該 E-mail 已註冊。");
        }

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("email", email);
        user.put("name", name);
        user.put("number", number);
        user.put("password", PASSWORD_ENCODER.encode(password));
        user.put("role", "user");
        user.put("provider", "local");
        user.put("dishClicked", false);
        user.put("putInCartClicked", false);
        user.put("orderClicked", false);
        user.put("buyClicked", false);
        user.put("reviewClicked", false);
        user.put("serverClicked", false);

        Map<String, Object> savedUser = mongoStore.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "註冊成功",
                "user", publicUser(savedUser)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> input) {
        String email = text(input.get("email")).trim().toLowerCase(Locale.ROOT);
        String password = text(input.get("password"));
        Map<String, Object> user = mongoStore.findByEmail(email);

        if (user == null || !(user.get("password") instanceof String passwordHash)
                || !PASSWORD_ENCODER.matches(password, passwordHash)) {
            return error(HttpStatus.UNAUTHORIZED, "E-mail 或密碼錯誤。");
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "登入成功",
                "user", publicUser(user),
                "token", createToken(user)));
    }

    @GetMapping("/logout")
    public Map<String, Object> logout() {
        // Token 目前由前端儲存；登出時由前端移除，後續可再加入伺服器端 token 撤銷。
        return Map.of("success", true, "message", "已登出");
    }

    private String createToken(Map<String, Object> user) {
        return JWT.create()
                .withClaim("id", text(user.get("_id")))
                .withClaim("role", text(user.get("role")))
                .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    private Map<String, Object> publicUser(Map<String, Object> user) {
        Map<String, Object> safeUser = new LinkedHashMap<>();
        safeUser.put("id", user.get("_id"));
        safeUser.put("email", user.get("email"));
        safeUser.put("name", user.get("name"));
        safeUser.put("number", user.get("number"));
        safeUser.put("role", user.getOrDefault("role", "user"));
        return safeUser;
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("success", false, "message", message));
    }

    private String text(Object value) {
        return Objects.toString(value, "");
    }
}

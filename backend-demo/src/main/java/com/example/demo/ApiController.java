package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiController {

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestBody String body) {
        // In a real app, you'd parse and validate 'body'. Here we just return a token.
        String json = "{\n  \"token\": \"abc123\"\n}";
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("X-App", "TestLangDemo")
                .body(json);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<String> getUser(@PathVariable("id") long id) {
        String json = "{\n  \"id\": " + id + ",\n  \"username\": \"user" + id + "\"\n}";
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("X-App", "TestLangDemo")
                .body(json);
    }

    @PutMapping(path = "/users/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateUser(@PathVariable("id") long id, @RequestBody String body) {
        // For demo purposes, always set role ADMIN and updated true
        String json = "{\n  \"id\": " + id + ",\n  \"updated\": true,\n  \"role\": \"ADMIN\"\n}";
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("X-App", "TestLangDemo")
                .body(json);
    }
}


package com.guildlite.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/test")
public class TestController {

    @PostMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from test controller!");
    }

    @GetMapping("/hello")
    public ResponseEntity<String> helloGet() {
        return ResponseEntity.ok("Hello GET working!");
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}

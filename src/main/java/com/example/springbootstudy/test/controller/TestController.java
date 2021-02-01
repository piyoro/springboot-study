package com.example.springbootstudy.test.controller;

import com.example.springbootstudy.test.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class TestController {

    @Autowired
    TestService testService;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${app.data.profile}")
    private String dataProfile;

    @GetMapping
    public ResponseEntity<?> index(HttpServletRequest req, HttpServletResponse res) {
        return ResponseEntity.ok("index");
    }

    @GetMapping("/cnt")
    public ResponseEntity<?> cnt(HttpServletRequest req, HttpServletResponse res) {
        int cnt = testService.selectTestCnt();
        return ResponseEntity.ok(cnt);
    }

    @GetMapping("/profile_condition")
    public ResponseEntity<?> profile_condition(HttpServletRequest req, HttpServletResponse res) {
        return ResponseEntity.ok(activeProfile + " " + dataProfile);
    }
}

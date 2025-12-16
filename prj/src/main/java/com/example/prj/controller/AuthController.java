package com.example.prj.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.prj.dto.LoginRequest;
import com.example.prj.dto.SignupRequest;
import com.example.prj.service.AuthService;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest req) {
        return authService.signup(req);
    }

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest req) {

    String result = authService.login(req);

    if (result.equals("User Not Found") || result.equals("Incorrect Password")) {
        return ResponseEntity.status(401).body(result);
    }

   return ResponseEntity.ok(Map.of("token", result));

}

}

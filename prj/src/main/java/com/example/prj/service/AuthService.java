package com.example.prj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.prj.Repository.UserRepository;
import com.example.prj.dto.LoginRequest;
import com.example.prj.dto.SignupRequest;
import com.example.prj.entity.User;
import com.example.prj.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String signup(SignupRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            return "User already exists";
        }

        User u = new User();
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));

        userRepo.save(u);
        return "Signup Successful";
    }

    public String login(LoginRequest req) {

        try {
            User user = userRepo.findByEmail(req.getEmail())
                    .orElseThrow(() -> new RuntimeException("User Not Found"));

            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                throw new RuntimeException("Incorrect Password");
            }

            return jwtUtil.generateToken(user.getEmail());
            
        } catch (RuntimeException e) {
            // ❗ No exception thrown → Controller gets clean message
            return e.getMessage();   // "User Not Found" or "Incorrect Password"
        }
    }
}

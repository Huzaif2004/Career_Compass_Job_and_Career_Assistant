package com.example.prj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.prj.Repository.UserRepository;
import com.example.prj.entity.User;
@Service
public class UserService {
    @Autowired
    UserRepository userRepo;
    public User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("User not found");
        }
        
        return userRepo.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
     
    
}

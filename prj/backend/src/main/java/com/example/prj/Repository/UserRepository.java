package com.example.prj.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.prj.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User,Long>{
    Optional<User>findByEmail(String email);
}
